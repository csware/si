/*
 * Copyright 2010 Sven Strickroth <email@cs-ware.de>
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

/**
 * @author Sven Strickroth
 */
public class JavaSyntaxTest extends TempDirTest {
	@Override
	protected void performTestInTempDir(Test test, File basePath, File tempDir, TestExecutorTestResult testResult) throws Exception {
		compileJava(tempDir, testResult);
	}

	final protected boolean compileJava(File javaDir, TestExecutorTestResult testResult) throws Exception {
		// http://forums.java.net/jive/message.jspa?messageID=325269
		int compiles = 1;
		JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
		try {
			ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();

			List<String> javaFiles = new LinkedList<String>();
			getRecursivelyAllJavaFiles(javaDir, javaFiles);

			if (javaFiles.size() > 0) {
				compiles = jc.run(null, null, errorOutputStream, javaFiles.toArray(new String[] {}));
			}
			if (testResult != null) {
				testResult.setTestPassed(compiles == 0);
				testResult.setTestOutput(errorOutputStream.toString().replace(javaDir.getAbsolutePath() + System.getProperty("file.separator"), ""));
			}
		} catch (Exception e) {
			System.err.println("System.getProperty(\"java.home\") should point to a jre in a jdk directory and tools.jar must be in the classpath");
			throw e;
		}
		return (compiles == 0);
	}

	final protected void getRecursivelyAllJavaFiles(File path, List<String> javaFiles) {
		for (File file : path.listFiles()) {
			if (file.isFile()) {
				if (file.getName().toLowerCase().endsWith(".java")) {
					javaFiles.add(file.getAbsolutePath());
				}
			} else {
				getRecursivelyAllJavaFiles(file, javaFiles);
			}
		}
	}
}
