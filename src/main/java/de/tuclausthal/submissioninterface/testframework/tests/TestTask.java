/*
 * Copyright 2009-2012, 2020 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.tests;

import java.io.File;
import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;

/**
 * Logic for tests
 * @author Sven Strickroth
 */
public class TestTask implements Serializable {
	private int testId;
	private int submissionid;
	private boolean saveTestResult = false;

	public TestTask(Test test, Submission submission) {
		this.testId = test.getId();
		this.submissionid = submission.getSubmissionid();
	}

	public TestTask(Test test, Submission submission, boolean saveTestResult) {
		this(test, submission);
		this.saveTestResult = saveTestResult;
	}

	public TestTask(Test test) {
		this.testId = test.getId();
	}

	/**
	 * Exetutes/performs the current task actions
	 * @param basePath the path to the submissions
	 * @param testResult 
	 */
	public void performTask(File basePath, TestExecutorTestResult testResult) {
		Session session = HibernateSessionHelper.getSessionFactory().openSession();
		Test test = DAOFactory.TestDAOIf(session).getTest(testId);
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(submissionid);
		if (test != null && submission != null) {
			Transaction tx = session.beginTransaction();
			//session.buildLockRequest(LockOptions.UPGRADE).lock(submission);
			Task task = submission.getTask();

			testResult.setTestID(testId);

			File path = new File(basePath.getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));

			performTaskInFolder(test, basePath, path, testResult);

			if (saveTestResult) {
				try {
					DAOFactory.TestResultDAOIf(session).storeTestResult(test, submission, testResult);
				} finally {
					try {
						tx.commit();
					} catch (Exception e) {
						LoggerFactory.getLogger(TestTask.class).error("Saving Testresult failed.", e);
						tx.rollback();
					}
				}
			} else {
				tx.commit(); // we did not do anything, just close the transaction
			}
		}
		session.close();
	}

	/**
	 * Exetutes/performs a specific test with the files located in path
	 * @param test the test to perform
	 * @param basePath the path to the submissions
	 * @param path the path of the folder where the testing data lies
	 * @param testResult 
	 */
	public void performTaskInFolder(Test test, File basePath, File path, TestExecutorTestResult testResult) {
		if (path.exists() == false) {
			path.mkdirs();
		}

		AbstractTest testImpl = test.getTestImpl();
		try {
			testImpl.performTest(test, basePath, path, testResult);
		} catch (Exception e) {
			LoggerFactory.getLogger(TestTask.class).error("Performing test failed.", e);
			testResult.setTestOutput(e.getMessage());
		}
	}
}
