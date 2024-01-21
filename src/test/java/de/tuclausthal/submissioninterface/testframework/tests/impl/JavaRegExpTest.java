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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tuclausthal.submissioninterface.persistence.datamodel.RegExpTest;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

public class JavaRegExpTest {

	@TempDir
	private Path tempDir;
	private Path sourceDir;
	private Path dataDir;
	private RegExpTest regexpTest;
	private JavaIORegexpTest javaFunctionTest;

	@BeforeEach
	void setUpTest() throws IOException {
		sourceDir = tempDir.resolve("source");
		Files.createDirectory(sourceDir);
		dataDir = tempDir.resolve("data");
		Files.createDirectory(dataDir);
		Files.copy(Path.of("SecurityManager/NoExitSecurityManager.jar"), dataDir.resolve("NoExitSecurityManager.jar"));

		regexpTest = new RegExpTest();
		regexpTest.setMainClass("HelloWorld");
		regexpTest.setCommandLineParameter("");
		regexpTest.setRegularExpression("^Hello World!$");
		javaFunctionTest = new JavaIORegexpTest(regexpTest);
	}

	@Test
	void testJavaRegExpTestOk() throws Exception {
		try (Writer fw = Files.newBufferedWriter(sourceDir.resolve("HelloWorld.java"))) {
			fw.write("public class HelloWorld {\n	public static void main(String[] args) {\n		System.out.println(\"Hello World!\");\n	}\n}\n");
		}

		TestExecutorTestResult result = new TestExecutorTestResult();
		javaFunctionTest.performTest(dataDir, sourceDir, result);
		assertTrue(result.isTestPassed(), result.getTestOutput());
	}

	@Test
	void testJavaRegExpTestFail() throws Exception {
		try (Writer fw = Files.newBufferedWriter(sourceDir.resolve("HelloWorld.java"))) {
			fw.write("public class HelloWorld {\n	public static void main(String[] args) {\n		System.out.println(\"Hello World!\");\n	}\n}\n");
		}

		regexpTest.setRegularExpression("No Match");
		TestExecutorTestResult result = new TestExecutorTestResult();
		javaFunctionTest.performTest(dataDir, sourceDir, result);
		assertFalse(result.isTestPassed(), result.getTestOutput());
	}

	@Test
	void testJavaRegExpTestTimeout() throws Exception {
		TestExecutorTestResult result = new TestExecutorTestResult();
		regexpTest.setTimeout(2);
		try (Writer fw = Files.newBufferedWriter(sourceDir.resolve("HelloWorld.java"))) {
			fw.write("public class HelloWorld {\n	public static void main(String[] args) {\n		while (true) {\n			try {\n				Thread.sleep(1000);\n			} catch (InterruptedException e) {\n				System.out.println(\"Ignore InterruptedException\");\n			}\n		}\n	}\n}\n");
		}
		javaFunctionTest.performTest(dataDir, sourceDir, result);
		assertFalse(result.isTestPassed());
		assertTrue(result.getTestOutput().contains("aborted due to too long execution time"), "Java test correctly enforces timeout.");
	}
}
