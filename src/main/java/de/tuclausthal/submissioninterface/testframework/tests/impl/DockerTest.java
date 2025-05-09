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

/**
 * @author Sven Strickroth
 */
public class DockerTest extends TempDirTest<de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest> {
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	final static public String SAFE_DOCKER_SCRIPT = "/usr/local/bin/safe-docker";

	final private static Random random = new Random();
	final private String separator;
	private  Path tempDir;

	public DockerTest(final de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest test) {
		super(test);
		separator = "#<GATE@" + random.nextLong() + "#@>#";
	}

	@Override
	public final void performTest(final Path basePath, final Path submissionPath, final TestExecutorTestResult testResult) throws Exception {
		try {
			tempDir = Util.createTemporaryDirectory("test");
			//Configuration.getInstance().getDataPath()
			if (tempDir == null) {
				throw new IOException("Failed to create tempdir!");
			}

			final Path administrativeDir = tempDir.resolve("administrative");
			Files.createDirectories(administrativeDir);

			final Path studentDir = tempDir.resolve("student");
			Files.createDirectories(studentDir);

			Util.recursiveCopy(submissionPath, studentDir);

			String testCode = generateTestShellScript();

			final Path testDriver = administrativeDir.resolve("test.sh");
			try (Writer fw = Files.newBufferedWriter(testDriver)) {
				fw.write(testCode.toString());
			}

			List<String> params = new ArrayList<>();
			params.add("sudo");
			params.add(SAFE_DOCKER_SCRIPT);
			params.add("--timeout=" + test.getTimeout());
			params.add("--dir=" + Util.escapeCommandlineArguments(administrativeDir.toAbsolutePath().toString()));
			params.add("--");
			params.add("bash");
			params.add(Util.escapeCommandlineArguments(testDriver.toAbsolutePath().toString()));

			ProcessBuilder pb = new ProcessBuilder(params);
			pb.directory(studentDir.toFile());
			/* only forward explicitly specified environment variables to test processes */
			pb.environment().keySet().removeIf(key -> !("PATH".equalsIgnoreCase(key) || "USER".equalsIgnoreCase(key) || "LANG".equalsIgnoreCase(key)));

			debugLog(params, studentDir);

			Process process = pb.start();
			ProcessOutputGrabber outputGrapper = new ProcessOutputGrabber(process);
			// no need to check for timeout, we fully rely on the safe-docker script here
			int exitValue = -1;
			boolean aborted = false;
			try {
				exitValue = process.waitFor();
			} catch (InterruptedException e) {
				aborted = true;
			}
			outputGrapper.waitFor();
			if (exitValue == 23 || exitValue == 24) { // magic value of the safe-docker script (23=timeout, 24=oom)
				aborted = true;
			}

			boolean exitedCleanly = (exitValue == 0);

			// for modularization and flexibility in child classes
			analyzeAndSetResult(exitedCleanly, outputGrapper.getStdOutBuffer(), outputGrapper.getStdErrBuffer(), exitValue, aborted, testResult);
		} finally {
			if (tempDir != null) {
				Util.recursiveDelete(tempDir);
			}
		}
	}

	protected void analyzeAndSetResult(boolean exitedCleanly, StringBuffer stdout, StringBuffer stderr, int exitCode, boolean aborted, TestExecutorTestResult result) {
		boolean passed = calculateTestResult(exitedCleanly, stdout, stderr, exitCode, aborted);
		result.setTestPassed(passed);
		result.setTestOutput(stdout.toString());
	}


	// similar code in JavaAdvancedIOTest
	protected boolean calculateTestResult(boolean exitedCleanly, final StringBuffer processOutput, final StringBuffer stdErr, final int exitCode, final boolean aborted) {
		JsonObjectBuilder builder = createJsonBuilder(exitedCleanly, processOutput, stdErr, exitCode, aborted);

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
			if (!outputs.get(i + 1).trim().equals(test.getTestSteps().get(i).getExpect().trim())) {
				exitedCleanly = false;
				job.add("ok", false);
			} else {
				job.add("ok", true);
			}
			arrb.add(job);
		}
		builder.add("steps", arrb);
		if (i < test.getTestSteps().size()) {
			builder.add("missing-tests", true);
			exitedCleanly = false;
		}

		processOutput.setLength(0);
		processOutput.append(builder.build().toString());
		return exitedCleanly;
	}

	protected JsonObjectBuilder createJsonBuilder(boolean exitedCleanly, final StringBuffer processOutput, final StringBuffer stdErr, final int exitCode, final boolean aborted) {
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
			builder.add("time-exceeded", aborted);
		}

		return builder;

	}

	protected String generateTestShellScript(){
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

	protected final void debugLog(List<String> params, Path studentDir){
		LOG.debug("Executing external process: {} in {}", params, studentDir);
	}


	@Override
	protected void performTestInTempDir(Path basePath, Path pTempDir, TestExecutorTestResult testResult) throws Exception {}
}
