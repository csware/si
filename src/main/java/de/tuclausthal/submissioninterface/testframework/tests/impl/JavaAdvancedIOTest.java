/*
 * Copyright 2020-2021 Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of the SubmissionInterface.
 * 
 * SubmissionInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * 
 * SubmissionInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SubmissionInterface. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.testframework.tests.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTestStep;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.Util;

public class JavaAdvancedIOTest extends JavaFunctionTest {
	private static final Random random = new Random();
	private final String separator;

	public JavaAdvancedIOTest() {
		separator = "#<GATE@" + random.nextLong() + "#@>#";
	}

	@Override
	public void performTest(Test test, File basePath, File submissionPath, TestExecutorTestResult testResult) throws Exception {
		File tempDir = null;
		try {
			tempDir = Util.createTemporaryDirectory("test");
			if (tempDir == null) {
				throw new IOException("Failed to create tempdir!");
			}

			Util.recursiveCopy(submissionPath, tempDir);
			if (!compileJava(tempDir, null)) { // TODO student solution has syntax error, provide more info?
				StringBuffer stdOut = new StringBuffer();
				StringBuffer stdErr = new StringBuffer();
				testResult.setTestPassed(calculateTestResult(test, false, stdOut, stdErr, false));
				testResult.setTestOutput(stdOut.toString());
				return;
			}

			de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest jtt = (de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest) test;
			StringBuffer testCode = new StringBuffer();
			boolean isFirst = true;
			for (JavaAdvancedIOTestStep testStep : jtt.getTestSteps()) {
				if (!isFirst) {
					testCode.append("System.out.println(\"" + separator + "\");");
					testCode.append("System.err.println(\"" + separator + "\");");
				}
				testCode.append("{\n");
				testCode.append(testStep.getTestcode());
				testCode.append("}\n");
				isFirst = false;
			}

			StringBuffer teacherTemplate = new StringBuffer();
			teacherTemplate.append("public class Tester {\n	public static void main(String[] args) {\n		{{TESTCODE}}\n	 }\n}\n");
			int insertionPoint = teacherTemplate.indexOf("{{TESTCODE}}");
			if (insertionPoint >= 0) {
				teacherTemplate.replace(insertionPoint, insertionPoint + "{{TESTCODE}}".length(), testCode.toString());
			}
			FileWriter fw = new FileWriter(new File(tempDir, "Tester.java"));
			fw.write(teacherTemplate.toString());
			fw.close();

			if (!compileJava(tempDir, null)) { // TODO test code has syntax error or cannot call student solution, provide more info?
				StringBuffer stdOut = new StringBuffer();
				StringBuffer stdErr = new StringBuffer();
				testResult.setTestPassed(calculateTestResult(test, false, stdOut, stdErr, false));
				testResult.setTestOutput(stdOut.toString());
				return;
			}
			runJava(test, basePath, tempDir, testResult);
		} finally {
			if (tempDir != null) {
				Util.recursiveDelete(tempDir);
			}
		}
	}

	// similar code in DockerTest
	@Override
	protected boolean calculateTestResult(Test test, boolean exitedCleanly, StringBuffer processOutput, StringBuffer stdErr, boolean aborted) {
		de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest jtt = (de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest) test;

		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("stdout", processOutput.toString());
		if (stdErr.length() > 0) {
			builder.add("stderr", stdErr.toString());
		}
		builder.add("separator", separator + "\n");
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
		int i = 0;
		JsonArrayBuilder arrb = Json.createArrayBuilder();
		for (i = 0; i < jtt.getTestSteps().size() && i < outputs.size(); ++i) {
			JsonObjectBuilder job = Json.createObjectBuilder();
			job.add("id", jtt.getTestSteps().get(i).getTeststepid());
			job.add("got", outputs.get(i));
			job.add("expected", jtt.getTestSteps().get(i).getExpect());
			if (!outputs.get(i).trim().equals(jtt.getTestSteps().get(i).getExpect().trim())) {
				exitedCleanly = false;
				job.add("ok", false);
			} else {
				job.add("ok", true);
			}
			arrb.add(job);
		}
		builder.add("steps", arrb);
		if (i < jtt.getTestSteps().size()) {
			builder.add("missing-tests", true);
			exitedCleanly = false;
		}
		processOutput.setLength(0);
		processOutput.append(builder.build().toString());
		return exitedCleanly;
	}

	@Override
	void populateParameters(Test test, File basePath, File tempDir, List<String> params) {
		params.add("Tester");
	}
}
