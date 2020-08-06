/*
 * Copyright 2010-2012, 2017 Sven Strickroth <email@cs-ware.de>
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

/**
 * @author Sven Strickroth
 */
public abstract class JavaFunctionTest extends JavaSyntaxTest {
	@Override
	final protected void performTestInTempDir(Test test, File basePath, File tempDir, TestExecutorTestResult testResult) throws Exception {
		compileJava(tempDir, null);

		File policyFile = null;
		try {
			// prepare policy file
			policyFile = File.createTempFile("special", ".policy");
			BufferedWriter policyFileWriter = new BufferedWriter(new FileWriter(policyFile));

			policyFileWriter.write("grant codeBase \"file:" + mkPath(basePath.getAbsolutePath() + System.getProperty("file.separator") + "junit.jar") + "\" {\n");
			policyFileWriter.write("	permission java.security.AllPermission;\n");
			policyFileWriter.write("};\n");
			policyFileWriter.write("\n");
			policyFileWriter.write("grant codeBase \"file:" + mkPath(basePath.getAbsolutePath() + System.getProperty("file.separator") + test.getTask().getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + test.getTask().getTaskid() + System.getProperty("file.separator") + "junittest" + test.getId() + ".jar") + "\" {\n");
			policyFileWriter.write("	permission java.lang.RuntimePermission \"setIO\";\n");
			policyFileWriter.write("	permission java.lang.RuntimePermission \"exitTheVM.*\";\n");
			policyFileWriter.write("	permission java.lang.reflect.ReflectPermission \"suppressAccessChecks\";\n");
			policyFileWriter.write("};\n");
			policyFileWriter.write("\n");
			policyFileWriter.write("grant {\n");
			policyFileWriter.write("	permission java.util.PropertyPermission \"*\", \"read\";\n");
			policyFileWriter.write("	permission java.io.FilePermission \"-\", \"read, write, delete\";\n"); // erlaube Zugriff auf alle Dateien im CWD
			policyFileWriter.write("	permission java.lang.RuntimePermission \"accessDeclaredMembers\";\n");
			policyFileWriter.write("};\n");
			policyFileWriter.close();

			List<String> additionalParams = new LinkedList<>();
			populateParameters(test, basePath, tempDir, additionalParams);

			// check what kind of test it is
			List<String> params = new LinkedList<>();
			params.add("java");
			// we have no frontend
			params.add("-Djava.awt.headless=true");
			// JOptionpane-Hack
			params.add("-Xbootclasspath/p:" + basePath.getAbsolutePath() + System.getProperty("file.separator") + "joptionpane.jar" + File.pathSeparator + basePath.getAbsolutePath() + System.getProperty("file.separator") + "NoExitSecurityManager.jar");
			// for security reasons, so that students cannot access the server
			params.add("-Djava.security.manager=secmgr.NoExitSecurityManager");
			params.add("-Djava.security.policy=" + policyFile.getAbsolutePath());
			params.addAll(additionalParams);

			ProcessBuilder pb = new ProcessBuilder(params);
			pb.directory(tempDir);
			Process process = pb.start();
			ProcessOutputGrabber outputGrapper = new ProcessOutputGrabber(process);
			TimeoutThread checkTread = new TimeoutThread(process, test.getTimeout());
			checkTread.start();
			int exitValue = -1;
			boolean aborted = false;
			try {
				exitValue = process.waitFor();
				aborted = checkTread.wasAborted();
			} catch (InterruptedException e) {
				aborted = true;
			}
			checkTread.interrupt();
			outputGrapper.waitFor();

			boolean exitedCleanly = (exitValue == 0);
			StringBuffer processOutput = outputGrapper.getStdOutBuffer();
			testResult.setTestPassed(calculateTestResult(test, exitedCleanly, processOutput));
			// append STDERR
			if (outputGrapper.getStdErrBuffer().length() > 0) {
				processOutput.append("\nFehlerausgabe (StdErr)\n");
				processOutput.append(outputGrapper.getStdErrBuffer());
			}
			if (aborted) {
				processOutput.insert(0, "Student-program aborted due to too long execution time.\n\n");
			}
			testResult.setTestOutput(processOutput.toString());
		} finally {
			if (policyFile != null) {
				policyFile.delete();
			}
		}
	}

	abstract protected boolean calculateTestResult(Test test, boolean exitedCleanly, StringBuffer processOutput);

	abstract void populateParameters(Test test, File basePath, File tempDir, List<String> params);
}
