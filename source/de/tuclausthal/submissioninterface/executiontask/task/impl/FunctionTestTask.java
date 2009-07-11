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

package de.tuclausthal.submissioninterface.executiontask.task.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tuclausthal.submissioninterface.executiontask.task.ExecutionTask;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestResultDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.RegExpTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Functiontest for a submission
 * @author Sven Strickroth
 */
public class FunctionTestTask extends ExecutionTask {
	private int submissionid;

	public FunctionTestTask(Submission submission) {
		this.submissionid = submission.getSubmissionid();
	}

	@Override
	public void performTask(File basePath) {
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf();
		Submission submission = submissionDAO.getSubmission(submissionid);
		if (submission != null) {
			Task task = submission.getTask();

			File path = new File(basePath.getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
			if (path.exists() == false) {
				path.mkdirs();
			}

			// Test exists
			if (task.getTest() != null) {
				try {
					File tempDir = Util.createTemporaryDirectory("test", null);
					if (tempDir == null) {
						throw new IOException("Failed to create tempdir!");
					}

					// prepare tempdir
					Util.recursiveCopy(path, tempDir);

					// prepare policy file
					File policyFile = File.createTempFile("special", ".policy");
					BufferedWriter policyFileWriter = new BufferedWriter(new FileWriter(policyFile));

					policyFileWriter.write("grant codeBase \"file:" + mkPath(basePath.getAbsolutePath() + System.getProperty("file.separator") + "junit.jar") + "\" {\n");
					policyFileWriter.write("	permission java.security.AllPermission;\n");
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
					// for security reasons, so that students cannot access the server
					params.add("-Djava.security.manager");
					params.add("-Djava.security.policy=" + policyFile.getAbsolutePath());
					if (task.getTest() instanceof JUnitTest) {
						params.add("-cp");
						params.add(basePath.getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + "junittest.jar" + File.pathSeparator + basePath.getAbsolutePath() + System.getProperty("file.separator") + "junit.jar" + File.pathSeparator + tempDir.getAbsolutePath());
						params.add("junit.textui.TestRunner");
						params.add("AllTests");
					} else if (task.getTest() instanceof RegExpTest) {
						RegExpTest regExpTest = (RegExpTest) task.getTest();
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
					TimeoutThread checkTread = new TimeoutThread(process, task.getTest().getTimeout());
					checkTread.start();
					int exitValue = -1;
					try {
						exitValue = process.waitFor();
					} catch (InterruptedException e) {
					}
					BufferedReader testInputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
					TestResultDAOIf testResultDAO = DAOFactory.TestResultDAOIf();
					TestResult testResult = testResultDAO.createTestResult(submission);
					testResult.setPassedTest((exitValue == 0));
					String testError = "";
					String line;
					while ((line = testInputStream.readLine()) != null) {
						testError = testError.concat(line + "\n");
					}
					testInputStream.close();
					if (task.getTest() instanceof RegExpTest) {
						Pattern testPattern = Pattern.compile(((RegExpTest) task.getTest()).getRegularExpression());
						Matcher testMatcher = testPattern.matcher(testError.trim());
						if (!testMatcher.matches()) {
							testError = "regexp doesn't match. Output follows (STDIN first):\n" + testError;
							testResult.setPassedTest(false);
						}
					}
					// append STDERR
					testError = testError.concat("\nSTDERR\n");
					BufferedReader testErrorInputStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					while ((line = testErrorInputStream.readLine()) != null) {
						testError = testError.concat(line + "\n");
					}
					testErrorInputStream.close();

					testResult.setTestOutput(testError);
					submissionDAO.saveSubmission(submission);
					Util.recursiveDelete(tempDir);
					policyFile.delete();
				} catch (Exception e) {
					// Error
					e.printStackTrace();
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
