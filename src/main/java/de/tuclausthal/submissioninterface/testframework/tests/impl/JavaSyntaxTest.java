/*
 * Copyright 2010, 2017, 2020-2021, 2024 Sven Strickroth <email@cs-ware.de>
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

/**
 * @author Sven Strickroth
 * @param <T> 
 */
public class JavaSyntaxTest<T extends Test> extends TempDirTest<T> {
	public JavaSyntaxTest(final T test) {
		super(test);
	}

	final private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	protected void performTestInTempDir(final Path basePath, final Path tempDir, final TestExecutorTestResult testResult) throws Exception {
		compileJava(tempDir, null, tempDir, testResult);
	}

	static final public boolean compileJava(final Path javaSourcesDir, final List<Path> additionalClassPath, final Path destDir, final TestExecutorTestResult testResult) throws Exception {
		// http://forums.java.net/jive/message.jspa?messageID=325269
		int compiles = 1;
		JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
		try {
			ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();

			List<String> javaFiles = new ArrayList<>();
			getRecursivelyAllJavaFiles(javaSourcesDir, javaFiles);

			if (!javaFiles.isEmpty()) {
				ArrayList<String> parameters = new ArrayList<>();
				if (additionalClassPath != null && !additionalClassPath.isEmpty()) {
					parameters.add("-cp");
					StringJoiner joiner = new StringJoiner(File.pathSeparator);
					additionalClassPath.stream().forEach(path -> joiner.add(path.toAbsolutePath().toString()));
					parameters.add(joiner.toString());
				}
				parameters.add("-d");
				parameters.add(destDir.toAbsolutePath().toString());
				parameters.addAll(javaFiles);
				compiles = jc.run(null, null, errorOutputStream, parameters.toArray(new String[0]));
			}
			if (testResult != null) {
				testResult.setTestPassed(compiles == 0);
				testResult.setTestOutput(errorOutputStream.toString().replace(javaSourcesDir.toAbsolutePath().toString() + System.getProperty("file.separator"), ""));
			}
		} catch (Exception e) {
			LOG.error("System.getProperty(\"java.home\") should point to a jre in a jdk directory and tools.jar must be in the classpath", e);
			throw e;
		}
		return (compiles == 0);
	}

	static final public void getRecursivelyAllJavaFiles(final Path path, final List<String> javaFiles) throws IOException {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
			for (final Path file : directoryStream) {
				if (Files.isRegularFile(file)) {
					if (file.getFileName().toString().toLowerCase().endsWith(".java")) {
						javaFiles.add(file.toAbsolutePath().toString());
					}
				} else {
					getRecursivelyAllJavaFiles(file, javaFiles);
				}
			}
		}
	}
}
