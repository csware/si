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

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

public class JavaCompileTest {
	@Test
	void testJavaCompileTestOK(@TempDir final Path tempDir) throws Exception {
		try (Writer fw = Files.newBufferedWriter(tempDir.resolve("HelloWorld.java"))) {
			fw.write("public class HelloWorld {\n	public static void main(String[] args) {\n		System.out.println(\"Hello World!\");\n	}\n}\n");
		}

		TestExecutorTestResult result = new TestExecutorTestResult();
		assertTrue(JavaSyntaxTest.compileJava(tempDir, null, tempDir, result), result.getTestOutput());
		assertTrue(result.isTestPassed());
	}

	@Test
	void testJavaCompileTestFail(@TempDir final Path tempDir) throws Exception {
		try (Writer fw = Files.newBufferedWriter(tempDir.resolve("HelloWorld.java"))) {
			fw.write("public class HelloWorld {\n	public static void main(String[] args) {\n		System.out.println(\"Hello World!);\n	 }\n}\n");
		}

		TestExecutorTestResult result = new TestExecutorTestResult();
		assertFalse(JavaSyntaxTest.compileJava(tempDir, null, tempDir, result));
		assertFalse(result.isTestPassed());
		assertTrue(result.getTestOutput().contains("HelloWorld.java:3: error: unclosed string literal"), "Java-Syntaxtest erkennt syntaktisch falsche Lösung (\"HelloWorld.java:3: error: unclosed string literal\").");
	}
}
