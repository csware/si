/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.CompileTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.RegExpTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Logic for tests
 * @author Sven Strickroth
 */
public class TestLogicImpl extends TestTask {
	private int testId;
	private int submissionid;
	private boolean saveTestResult = false;

	public TestLogicImpl(Test test, Submission submission) {
		this.testId = test.getId();
		this.submissionid = submission.getSubmissionid();
	}

	public TestLogicImpl(Test test, Submission submission, boolean saveTestResult) {
		this.testId = test.getId();
		this.submissionid = submission.getSubmissionid();
		this.saveTestResult = saveTestResult;
	}

	@Override
	public void performTask(File basePath, TestExecutorTestResult testResult) {
		Session session = HibernateSessionHelper.getSessionFactory().openSession();
		Test test = DAOFactory.TestDAOIf(session).getTest(testId);
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(submissionid);
		if (test != null && submission != null) {
			Transaction tx = session.beginTransaction();
			//session.lock(submission, LockMode.UPGRADE);
			Task task = submission.getTask();

			testResult.setTestID(testId);

			File path = new File(basePath.getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
			if (path.exists() == false) {
				path.mkdirs();
			}

			File policyFile = null;
			File tempDir = null;
			try {
				tempDir = Util.createTemporaryDirectory("test", null);
				if (tempDir == null) {
					throw new IOException("Failed to create tempdir!");
				}

				// prepare tempdir
				Util.recursiveCopy(path, tempDir);

				// TODO: optimize architecture, atm: hopefully it works ;)

				// compile test
				// http://forums.java.net/jive/message.jspa?messageID=325269
				JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
				try {
					ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();

					List<String> javaFiles = new LinkedList<String>();
					for (File javaFile : tempDir.listFiles()) {
						if (javaFile.getName().endsWith(".java")) {
							javaFiles.add(javaFile.getAbsolutePath());
						}
					}
					int compiles = 1;
					if (javaFiles.size() > 0) {
						compiles = jc.run(null, null, errorOutputStream, javaFiles.toArray(new String[] {}));
					}
					if (test instanceof CompileTest) {
						testResult.setTestPassed(compiles == 0);
						testResult.setTestOutput(errorOutputStream.toString().replace(tempDir.getAbsolutePath() + System.getProperty("file.separator"), ""));
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("System.getProperty(\"java.home\") should point to a jre in a jdk directory");
					throw e;
				}

				if (!(test instanceof CompileTest)) {
					// prepare policy file
					policyFile = File.createTempFile("special", ".policy");
					BufferedWriter policyFileWriter = new BufferedWriter(new FileWriter(policyFile));

					policyFileWriter.write("grant codeBase \"file:" + mkPath(basePath.getAbsolutePath() + System.getProperty("file.separator") + "junit.jar") + "\" {\n");
					policyFileWriter.write("	permission java.security.AllPermission;\n");
					policyFileWriter.write("};\n");
					policyFileWriter.write("\n");
					policyFileWriter.write("grant codeBase \"file:" + mkPath(basePath.getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + "junittest" + test.getId() + ".jar") + "\" {\n");
					policyFileWriter.write("	permission java.lang.RuntimePermission \"setIO\";\n");
					policyFileWriter.write("};\n");
					policyFileWriter.write("\n");
					policyFileWriter.write("grant {\n");
					policyFileWriter.write("	permission java.util.PropertyPermission \"*\", \"read\";\n");
					policyFileWriter.write("	permission java.io.FilePermission \"file:" + mkPath(tempDir.getAbsolutePath()) + "\", \"read, write, delete\";\n");
					policyFileWriter.write("	permission java.lang.RuntimePermission \"accessDeclaredMembers\";\n");
					policyFileWriter.write("};\n");
					policyFileWriter.close();

					// check what kind of test it is
					List<String> params = new LinkedList<String>();
					params.add("java");
					params.add("-Djava.awt.headless=true");
					// for security reasons, so that students cannot access the server
					params.add("-Djava.security.manager");
					params.add("-Djava.security.policy=" + policyFile.getAbsolutePath());
					if (test instanceof JUnitTest) {
						params.add("-cp");
						params.add(basePath.getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + "junittest" + test.getId() + ".jar" + File.pathSeparator + basePath.getAbsolutePath() + System.getProperty("file.separator") + "junit.jar" + File.pathSeparator + tempDir.getAbsolutePath());
						params.add("junit.textui.TestRunner");
						params.add("AllTests");
					} else if (test instanceof RegExpTest) {
						RegExpTest regExpTest = (RegExpTest) test;
						params.add(regExpTest.getMainClass());
						if (regExpTest.getCommandLineParameter() != null && !regExpTest.getCommandLineParameter().isEmpty()) {
							params.addAll(Arrays.asList(Util.mksafecmdargs(regExpTest.getCommandLineParameter()).split(" ")));
						}
					} else {
						throw new RuntimeException("Testtype unknown!");
					}
					ProcessBuilder pb = new ProcessBuilder(params);
					pb.directory(tempDir);
					Process process = pb.start();
					ReadOutputThread readOutputThread = new ReadOutputThread(process);
					readOutputThread.start();
					TimeoutThread checkTread = new TimeoutThread(process, test.getTimeout());
					checkTread.start();
					int exitValue = -1;
					try {
						exitValue = process.waitFor();
					} catch (InterruptedException e) {
					}
					checkTread.interrupt();
					readOutputThread.interrupt();
					testResult.setTestPassed(exitValue == 0);
					String testError = readOutputThread.getStdOut();
					if (test instanceof RegExpTest) {
						Pattern testPattern = Pattern.compile(((RegExpTest) test).getRegularExpression());
						Matcher testMatcher = testPattern.matcher(testError.trim());
						if (!testMatcher.matches()) {
							testError = "Ausgabe stimmt nicht mit erwarteter Ausgabe überein. Ausgabe folgt (StdIn zuerst):\n" + testError;
							testResult.setTestPassed(false);
						}
					}
					// append STDERR
					if (!readOutputThread.getStdErr().isEmpty()) {
						testError = testError.concat("\nFehlerausgabe (StdErr)\n" + readOutputThread.getStdErr());
					}
					testResult.setTestOutput(testError);
				}
			} catch (Exception e) {
				// Error
				testResult.setTestOutput(e.getMessage());
				tx.rollback();
				e.printStackTrace();
			} finally {
				if (policyFile != null) {
					policyFile.delete();
				}
				if (tempDir != null) {
					Util.recursiveDelete(tempDir);
				}
				
			}
			if (saveTestResult) {
				try {
					DAOFactory.TestResultDAOIf(session).createTestResult(test, submission, testResult);
				} finally {
					tx.commit();
				}
			}
		}
	}

	/**
	 * Prepares a windows path (windows needs double backslash)
	 * @param absolutePath the original path
	 * @return an escaped path
	 */
	private String mkPath(String absolutePath) {
		if (System.getProperty("file.separator").equals("\\")) {
			return absolutePath.replace("\\", "\\\\");
		} else {
			return absolutePath;
		}
	}
}
