/*
 * Copyright 2021-2022 Sven Strickroth <email@cs-ware.de>
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTestStep;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * @author Sven Strickroth
 */
public class DockerTest extends TempDirTest {
	//final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	final static public String SAFE_DOCKER_SCRIPT = "/usr/local/bin/safe-docker";

	private static final Random random = new Random();
	private final String separator;
	private final de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest test;
	private File tempDir;

	public DockerTest(de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest test) {
		super(test);
		separator = "#<GATE@" + random.nextLong() + "#@>#";
		this.test = test;
	}

	@Override
	public void performTest(File basePath, File submissionPath, TestExecutorTestResult testResult) throws Exception {
		try {
			tempDir = Util.createTemporaryDirectory("test");
			//Configuration.getInstance().getDataPath()
			if (tempDir == null) {
				throw new IOException("Failed to create tempdir!");
			}

			File administrativeDir = new File(tempDir, "administrative");
			administrativeDir.mkdirs();

			File studentDir = new File(tempDir, "student");
			studentDir.mkdirs();

			Util.recursiveCopy(submissionPath, studentDir);

			StringBuilder testCode = new StringBuilder();
			testCode.append("#!/bin/bash\n");
			testCode.append("set -e\n");
			testCode.append("separator='");
			testCode.append(separator);
			testCode.append("'\n\n");

			testCode.append(test.getPreparationShellCode());
			testCode.append("\n");

			for (DockerTestStep testStep : test.getTestSteps()) {
				testCode.append("echo $separator\n");
				testCode.append("echo $separator >&2\n");
				testCode.append("set -e\n");
				testCode.append(testStep.getTestcode());
				testCode.append("\n");
			}

			FileWriter fw = new FileWriter(new File(administrativeDir, "test.sh"));
			fw.write(testCode.toString());
			fw.close();

			List<String> params = new ArrayList<>();
			params.add("sudo");
			params.add(SAFE_DOCKER_SCRIPT);
			params.add("--timeout=" + test.getTimeout());
			params.add("--dir=" + Util.escapeCommandlineArguments(administrativeDir.getAbsolutePath()));
			params.add("--");
			params.add("bash");
			params.add(Util.escapeCommandlineArguments(administrativeDir.getAbsolutePath()) + "/test.sh");

			ProcessBuilder pb = new ProcessBuilder(params);
			pb.directory(studentDir);
			/* only forward explicitly specified environment variables to test processes */
			pb.environment().keySet().removeIf(key -> !("PATH".equalsIgnoreCase(key) || "USER".equalsIgnoreCase(key) || "LANG".equalsIgnoreCase(key)));
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
			testResult.setTestPassed(calculateTestResult(test, exitedCleanly, outputGrapper.getStdOutBuffer(), outputGrapper.getStdErrBuffer(), exitValue, aborted));
			testResult.setTestOutput(outputGrapper.getStdOutBuffer().toString());
		} finally {
			if (tempDir != null) {
				Util.recursiveDelete(tempDir);
			}
		}
	}

	// similar code in JavaAdvancedIOTest
	protected boolean calculateTestResult(Test test, boolean exitedCleanly, StringBuffer processOutput, StringBuffer stdErr, int exitCode, boolean aborted) {
		de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest dt = (de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest) test;

		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("stdout", processOutput.toString());
		if (stdErr.length() > 0) {
			builder.add("stderr", stdErr.toString());
		}
		builder.add("separator", separator + "\n");
		if (tempDir != null) {
			builder.add("tmpdir", tempDir.getAbsolutePath());
		}
		builder.add("exitCode", exitCode);
		builder.add("exitedCleanly", exitedCleanly);
		if (aborted) {
			builder.add("time-exceeded", aborted);
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
		for (i = 0; i < dt.getTestSteps().size() && i + 1 < outputs.size(); ++i) {
			JsonObjectBuilder job = Json.createObjectBuilder();
			job.add("id", dt.getTestSteps().get(i).getTeststepid());
			job.add("got", outputs.get(i + 1));
			job.add("expected", dt.getTestSteps().get(i).getExpect());
			if (!outputs.get(i + 1).trim().equals(dt.getTestSteps().get(i).getExpect().trim())) {
				exitedCleanly = false;
				job.add("ok", false);
			} else {
				job.add("ok", true);
			}
			arrb.add(job);
		}
		builder.add("steps", arrb);
		if (i < dt.getTestSteps().size()) {
			builder.add("missing-tests", true);
			exitedCleanly = false;
		}

		processOutput.setLength(0);
		processOutput.append(builder.build().toString());
		return exitedCleanly;
	}

	@Override
	protected void performTestInTempDir(File basePath, File tempDir, TestExecutorTestResult testResult) throws Exception {}
}
