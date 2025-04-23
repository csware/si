/*
 * Copyright 2021-2024 Sven Strickroth <email@cs-ware.de>
 *
 * This file is part of the GATE.
 *
 * GATE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * GATE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GATE. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.testframework.tests.impl;

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTestStep;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.Util;

public class DockerTest extends TempDirTest<de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest> {
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	final static public String SAFE_DOCKER_SCRIPT = "/usr/local/bin/safe-docker";
	private static final Random random = new Random();
	protected final String separator;
	protected Path tempDir;

	public DockerTest(de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest test) {
		super(test);
		separator = "#<GATE@" + random.nextLong() + "#@>#";
	}

	@Override
	public final void performTest(final Path basePath, final Path submissionPath, final TestExecutorTestResult testResult) throws Exception {
		try {
			tempDir = Util.createTemporaryDirectory("test");
			if (tempDir == null) {
				throw new IOException("Failed to create tempdir!");
			}

			final Path adminDir = tempDir.resolve("administrative");
			final Path studentDir = tempDir.resolve("student");
			Files.createDirectories(adminDir);
			Files.createDirectories(studentDir);
			Util.recursiveCopy(submissionPath, studentDir);

			String testCode = generateTestShellScript();
			Path testScript = adminDir.resolve("test.sh");
			try (Writer fw = Files.newBufferedWriter(testScript)) {
				fw.write(testCode);
			}


			List<String> params = new ArrayList<>();
			params.add("sudo");
			params.add(SAFE_DOCKER_SCRIPT);
			params.add("--timeout=" + test.getTimeout());
			params.add("--dir=" + Util.escapeCommandlineArguments(adminDir.toAbsolutePath().toString()));
			params.add("--");
			params.add("bash");
			params.add(Util.escapeCommandlineArguments(testScript.toAbsolutePath().toString()));

			ProcessBuilder pb = new ProcessBuilder(params);
			pb.directory(studentDir.toFile());
			pb.environment().keySet().removeIf(key -> !("PATH".equalsIgnoreCase(key) || "USER".equalsIgnoreCase(key) || "LANG".equalsIgnoreCase(key)));
			LOG.debug("Executing {} docker process: {}", this.getClass().getSimpleName(), params);

			Process process = pb.start();
			ProcessOutputGrabber outputGrabber = new ProcessOutputGrabber(process);


			int exitCode = -1;
			boolean aborted = false;
			try {
				exitCode = process.waitFor();
			} catch (InterruptedException e) {
				aborted = true;
			}
			outputGrabber.waitFor();
			if (exitCode == 23 || exitCode == 24) aborted = true;

			boolean success = isSuccessful(exitCode, outputGrabber.getStdErrBuffer());
			String outPutJSON = generateJsonResult(outputGrabber.getStdOutBuffer(), outputGrabber.getStdErrBuffer(), exitCode, success, aborted);
			testResult.setTestPassed(success);
			testResult.setTestOutput(outPutJSON);
		} finally {
			if (tempDir != null) {
				Util.recursiveDelete(tempDir);
			}
		}
	}

	protected ProcessOutputGrabber executeDockerContainer (Path adminDir, Path studentDir, Path testScript) throws Exception {
		List<String> params = new ArrayList<>();
		params.add("sudo");
		params.add(SAFE_DOCKER_SCRIPT);
		params.add("--timeout=" + test.getTimeout());
		params.add("--dir=" + Util.escapeCommandlineArguments(adminDir.toAbsolutePath().toString()));
		params.add("--");
		params.add("bash");
		params.add(Util.escapeCommandlineArguments(testScript.toAbsolutePath().toString()));

		ProcessBuilder pb = new ProcessBuilder(params);
		pb.directory(studentDir.toFile());
		pb.environment().keySet().removeIf(key -> !("PATH".equalsIgnoreCase(key) || "USER".equalsIgnoreCase(key) || "LANG".equalsIgnoreCase(key)));
		LOG.debug("Executing {} docker process: {}", this.getClass().getSimpleName(), params);
		Process process = pb.start();
		ProcessOutputGrabber outputGrabber = new ProcessOutputGrabber(process);
		return outputGrabber;
	}

	protected boolean isSuccessful(int exitCode, StringBuffer stderr) {
		return exitCode == 0;
	}

	protected String generateTestShellScript() {
		StringBuilder testCode = new StringBuilder();
		testCode.append("#!/bin/bash\n");
		testCode.append("set -e\n");
		testCode.append(test.getPreparationShellCode());
		testCode.append("\n");
		for (DockerTestStep testStep : test.getTestSteps()) {
			testCode.append("echo '" + separator + "'\n");
			testCode.append("echo '" + separator + "' >&2\n");
			testCode.append("{\n");
			testCode.append("set -e\n");
			testCode.append(testStep.getTestcode());
			testCode.append("\n");
			testCode.append("}\n");
		}
		return testCode.toString();
	}

	protected String generateJsonResult(StringBuffer processOutput, StringBuffer stdErr, int exitCode, boolean exitedCleanly, boolean aborted) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("stdout", processOutput.toString());
		if (stdErr.length() > 0) {
			builder.add("stderr", stdErr.toString());
		}
		builder.add("separator", separator + "\n");
		if (tempDir != null) {
			builder.add("tmpdir", tempDir.toAbsolutePath().toString());
		}
		builder.add("exitCode", exitCode);
		builder.add("exitedCleanly", exitedCleanly);
		if (aborted) {
			builder.add("time-exceeded", true);
		}

		int start = 0;
		int splitterPos;
		ArrayList<String> outputs = new ArrayList<>();
		while ((splitterPos = processOutput.indexOf(separator + "\n", start)) >= 0) {
			outputs.add(processOutput.substring(start, splitterPos));
			start = splitterPos + (separator + "\n").length();
		}
		outputs.add(processOutput.substring(start));

		int i;
		JsonArrayBuilder arrb = Json.createArrayBuilder();
		for (i = 0; i < test.getTestSteps().size() && i + 1 < outputs.size(); ++i) {
			JsonObjectBuilder job = Json.createObjectBuilder();
			job.add("id", test.getTestSteps().get(i).getTeststepid());
			job.add("got", outputs.get(i + 1));
			job.add("expected", test.getTestSteps().get(i).getExpect());
			job.add("ok", outputs.get(i + 1).trim().equals(test.getTestSteps().get(i).getExpect().trim()));
			arrb.add(job);
		}
		builder.add("steps", arrb);
		if (i < test.getTestSteps().size()) {
			builder.add("missing-tests", true);
		}

		processOutput.setLength(0);
		processOutput.append(builder.build().toString());
		return builder.build().toString();
	}

	@Override
	protected void performTestInTempDir(Path basePath, Path pTempDir, TestExecutorTestResult testResult) throws Exception {}
}
