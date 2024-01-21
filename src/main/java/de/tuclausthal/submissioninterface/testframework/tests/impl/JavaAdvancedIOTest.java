/*
 * Copyright 2020-2022, 2024 Sven Strickroth <email@cs-ware.de>
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTestStep;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.Util;

public class JavaAdvancedIOTest extends JavaFunctionTest {
	private static final Random random = new Random();
	private static final String STUDENT_CODE_DIRNAME = "studentcode";
	private static final String TEST_CODE_DIRNAME = "testcode";
	private static final String STUDENT_CLASSES_DIRNAME = STUDENT_CODE_DIRNAME + "_classes";
	private static final String TEST_CLASSES_DIRNAME = TEST_CODE_DIRNAME + "_classes";
	private final String separator;
	private final de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest test;

	public enum FAILSTATE {
		SYNTAX_ERROR_STUDENT_SOLUTION,
		SYNTAX_ERROR_WITH_TEST_CODE,
	}
	private FAILSTATE errorState = null;

	public JavaAdvancedIOTest(de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest test) {
		super(test);
		separator = "#<GATE@" + random.nextLong() + "#@>#";
		this.test = test;
	}

	@Override
	public void performTest(final Path basePath, final Path submissionPath, final TestExecutorTestResult testResult) throws Exception {
		Path tempDir = null;
		try {
			tempDir = Util.createTemporaryDirectory("test");
			if (tempDir == null) {
				throw new IOException("Failed to create tempdir!");
			}
			final Path codeDir = tempDir.resolve(STUDENT_CODE_DIRNAME);
			Files.createDirectory(codeDir);
			final Path testerDir = tempDir.resolve(TEST_CODE_DIRNAME);
			Files.createDirectory(testerDir);
			final Path studentClassesDir = tempDir.resolve(STUDENT_CLASSES_DIRNAME);
			Files.createDirectory(studentClassesDir);
			final Path testClassesDir = tempDir.resolve(TEST_CLASSES_DIRNAME);
			Files.createDirectory(testClassesDir);

			Util.recursiveCopy(submissionPath, codeDir);
			TestExecutorTestResult compileTestResult = new TestExecutorTestResult();
			if (!compileJava(codeDir, null, studentClassesDir, compileTestResult)) {
				StringBuffer stdOut = new StringBuffer();
				errorState = FAILSTATE.SYNTAX_ERROR_STUDENT_SOLUTION;
				testResult.setTestPassed(calculateTestResult(false, stdOut, new StringBuffer(compileTestResult.getTestOutput()), false));
				testResult.setTestOutput(stdOut.toString());
				return;
			}

			StringBuffer testCode = new StringBuffer();
			boolean isFirst = true;
			for (JavaAdvancedIOTestStep testStep : test.getTestSteps()) {
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
			try (Writer fw = Files.newBufferedWriter(testerDir.resolve("Tester.java"))) {
				fw.write(teacherTemplate.toString());
			}

			TestExecutorTestResult compileTestResultFull = new TestExecutorTestResult();
			if (!compileJava(testerDir, Arrays.asList(studentClassesDir), testClassesDir, compileTestResultFull)) {
				StringBuffer stdOut = new StringBuffer();
				errorState = FAILSTATE.SYNTAX_ERROR_WITH_TEST_CODE;
				testResult.setTestPassed(calculateTestResult(false, stdOut, new StringBuffer(compileTestResultFull.getTestOutput()), false));
				testResult.setTestOutput(stdOut.toString());
				return;
			}
			runJava(basePath, codeDir, Arrays.asList(testClassesDir, studentClassesDir), testResult);
		} finally {
			if (tempDir != null) {
				Util.recursiveDelete(tempDir);
			}
		}
	}

	// similar code in DockerTest
	@Override
	protected boolean calculateTestResult(boolean exitedCleanly, StringBuffer processOutput, StringBuffer stdErr, boolean aborted) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("v", 1);
		builder.add("stdout", processOutput.toString());
		JavaFunctionTest.cleanupStdErr(stdErr);
		if (stdErr.length() > 0) {
			builder.add("stderr", stdErr.toString());
		}
		if (errorState == FAILSTATE.SYNTAX_ERROR_STUDENT_SOLUTION) {
			builder.add("syntaxerror", "student-code");
			processOutput.setLength(0);
			processOutput.append(builder.build().toString());
			return exitedCleanly;
		} else if (errorState == FAILSTATE.SYNTAX_ERROR_WITH_TEST_CODE) {
			builder.add("syntaxerror", "with-test-code");
			processOutput.setLength(0);
			processOutput.append(builder.build().toString());
			return exitedCleanly;
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
		for (i = 0; i < test.getTestSteps().size() && i < outputs.size(); ++i) {
			JsonObjectBuilder job = Json.createObjectBuilder();
			job.add("id", test.getTestSteps().get(i).getTeststepid());
			job.add("got", outputs.get(i));
			job.add("expected", test.getTestSteps().get(i).getExpect());
			if (!outputs.get(i).trim().equals(test.getTestSteps().get(i).getExpect().trim())) {
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

	@Override
	void populateParameters(List<String> params) {
		params.add("Tester");
	}

	@Override
	void populateJavaPolicyFile(final Path basePath, final Path tempDir, final BufferedWriter policyFileWriter) throws IOException {
		policyFileWriter.write("grant codeBase \"file:" + mkPath(tempDir.getParent().resolve(TEST_CLASSES_DIRNAME)) + "\" {\n");
		policyFileWriter.write("	permission java.lang.RuntimePermission \"setIO\";\n");
		policyFileWriter.write("	permission java.lang.reflect.ReflectPermission \"suppressAccessChecks\";\n");
		policyFileWriter.write("};\n");
	}
}
