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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;

/**
 * Compiles/Compiletest for a submission
 * @author Sven Strickroth
 */
public class CompileTestTask extends TestTask {
	private int submissionid;

	public CompileTestTask(Submission submission) {
		this.submissionid = submission.getSubmissionid();
	}

	@Override
	public void performTask(File basePath, TestExecutorTestResult testResult) {
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf();
		Submission submission = submissionDAO.getSubmission(submissionid);
		if (submission != null) {
			Task task = submission.getTask();

			File path = new File(basePath.getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
			if (path.exists() == false) {
				path.mkdirs();
			}

			// compile test
			// http://forums.java.net/jive/message.jspa?messageID=325269
			JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
			try {
				ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();

				List<String> javaFiles = new LinkedList<String>();
				for (File javaFile : path.listFiles()) {
					if (javaFile.getName().endsWith(".java")) {
						javaFiles.add(javaFile.getAbsolutePath());
					}
				}
				int compiles = 1;
				if (javaFiles.size() > 0) {
					compiles = jc.run(null, null, errorOutputStream, javaFiles.toArray(new String[] {}));
				}
				testResult.setTestPassed(compiles == 0);
				testResult.setTestOutput(errorOutputStream.toString().replace(path.getAbsolutePath() + System.getProperty("file.separator"), ""));
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("System.getProperty(\"java.home\") should point to a jre in a jdk directory");
				return;
			}
		}
	}

	@Override
	public boolean requiresTempDir() {
		return false;
	}
}
