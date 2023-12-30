/*
 * Copyright 2010-2012, 2017, 2020-2023 Sven Strickroth <email@cs-ware.de>
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * @author Sven Strickroth
 */
public abstract class JavaFunctionTest extends JavaSyntaxTest {
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	final static public String SECURITYMANAGER_JAR = "NoExitSecurityManager.jar";

	public JavaFunctionTest(Test test) {
		super(test);
	}

	@Override
	final protected void performTestInTempDir(File basePath, File javaSourceDir, TestExecutorTestResult testResult) throws Exception {
		File tempClassesDir = Util.createTemporaryDirectory("test");
		if (tempClassesDir == null) {
			throw new IOException("Failed to create tempdir!");
		}
		try {
			compileJava(javaSourceDir, null, tempClassesDir, null);
			List<File> classPath = new ArrayList<>();
			populateClassPathForRunningtests(basePath, classPath);
			classPath.add(tempClassesDir);
			runJava(basePath, javaSourceDir, classPath, testResult);
		} finally {
			Util.recursiveDelete(tempClassesDir);
		}
	}

	protected void runJava(File basePath, File cwd, List<File> classPath, TestExecutorTestResult testResult) throws Exception {
		File policyFile = null;
		try {
			// prepare policy file
			policyFile = File.createTempFile("special", ".policy");
			BufferedWriter policyFileWriter = new BufferedWriter(new FileWriter(policyFile));
			populateJavaPolicyFile(basePath, cwd, policyFileWriter);
			policyFileWriter.write("\n");
			policyFileWriter.write("grant {\n");
			policyFileWriter.write("	permission java.util.PropertyPermission \"*\", \"read\";\n");
			policyFileWriter.write("	permission java.io.FilePermission \"-\", \"read, write, delete\";\n"); // erlaube Zugriff auf alle Dateien im CWD
			policyFileWriter.write("	permission java.lang.RuntimePermission \"accessDeclaredMembers\";\n");
			policyFileWriter.write("};\n");
			policyFileWriter.close();

			List<String> additionalParams = new ArrayList<>();
			populateParameters(additionalParams);

			// check what kind of test it is
			List<String> params = new ArrayList<>();
			params.add("java");
			// we have no frontend
			params.add("-Djava.awt.headless=true");
			// limit memory usage
			params.add("-Xmx128m");
			// for security reasons, so that students cannot access the server
			params.add("-Xbootclasspath/a:" + basePath.getAbsolutePath() + System.getProperty("file.separator") + SECURITYMANAGER_JAR);
			params.add("-Djava.security.manager=secmgr.NoExitSecurityManager");
			params.add("-Djava.security.policy=" + policyFile.getAbsolutePath());
			if (!classPath.isEmpty()) {
				params.add("-cp");
				StringJoiner joiner = new StringJoiner(File.pathSeparator);
				classPath.stream().forEach(path -> joiner.add(path.getAbsolutePath()));
				params.add(joiner.toString());
			}
			params.addAll(additionalParams);

			ProcessBuilder pb = new ProcessBuilder(params);
			pb.directory(cwd);
			/* only forward explicitly specified environment variables to test processes */
			pb.environment().keySet().removeIf(key -> !("PATH".equalsIgnoreCase(key) || "USER".equalsIgnoreCase(key) || "JAVA_HOME".equalsIgnoreCase(key) || "LANG".equalsIgnoreCase(key)));
			LOG.debug("Executing external process: {} in {}", params, cwd);
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
			testResult.setTestPassed(calculateTestResult(exitedCleanly, outputGrapper.getStdOutBuffer(), outputGrapper.getStdErrBuffer(), aborted));
			testResult.setTestOutput(outputGrapper.getStdOutBuffer().toString());
		} finally {
			if (policyFile != null) {
				policyFile.delete();
			}
		}
	}

	abstract protected boolean calculateTestResult(boolean exitedCleanly, StringBuffer processOutput, StringBuffer stdErr, boolean aborted);

	static final protected void cleanupStdErr(StringBuffer stdErr) {
		/* Security Manager is deprecated with Java 17 and outputs a warning: https://openjdk.java.net/jeps/411 */
		int startsWith = startsWith(stdErr, "WARNING: A command line option has enabled the Security Manager\nWARNING: The Security Manager is deprecated and will be removed in a future release\n");
		if (startsWith > 0) {
			stdErr.delete(0, startsWith);
		}
	}

	public static int startsWith(StringBuffer stdErr, String prefix) {
		if (0 > stdErr.length() - prefix.length()) {
			return -1;
		}
		int pp = 0;
		int pc = 0;
		boolean escaped = false;
		while (pp < stdErr.length() && pc < prefix.length()) {
			if (stdErr.charAt(pp) != prefix.charAt(pc)) {
				if (prefix.charAt(pc) == '\n' && stdErr.charAt(pp) == '\r' && escaped == false) {
					++pp;
					escaped = true;
					continue;
				}
				return -1;
			}
			escaped = false;
			++pp;
			++pc;
		}
		return pp;
	}

	abstract void populateParameters(List<String> params);

	abstract void populateJavaPolicyFile(File basePath, File tempDir, BufferedWriter policyFileWriter) throws IOException;

	void populateClassPathForRunningtests(File basePath, List<File> classPath) {}
}
