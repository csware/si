/*
 * Copyright 2009-2012, 2020-2021, 2023-2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.tests;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Logic for tests
 * @author Sven Strickroth
 */
public class TestTask {
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

	final public int getTestId() {
		return testId;
	}

	final public int getSubmissionId() {
		return submissionid;
	}

	/**
	 * Exetutes/performs the current task actions
	 * @param basePath the path to the submissions
	 * @param testResult 
	 */
	public void performTask(final Path basePath, final TestExecutorTestResult testResult) {
		Session session = HibernateSessionHelper.getSessionFactory().openSession();
		Test test = DAOFactory.TestDAOIf(session).getTest(testId);
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(submissionid);
		if (test != null && submission != null) {
			Transaction tx = session.beginTransaction();
			//session.buildLockRequest(LockOptions.UPGRADE).lock(submission);

			testResult.setTestID(testId);

			final Path path = Util.constructPath(basePath, submission);

			performTaskInFolder(test, basePath, path, testResult);

			if (saveTestResult) {
				try {
					DAOFactory.TestResultDAOIf(session).storeTestResult(test, submission, testResult);
				} finally {
					try {
						tx.commit();
					} catch (Exception e) {
						LoggerFactory.getLogger(MethodHandles.lookup().lookupClass()).error("Saving Testresult failed.", e);
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
	public void performTaskInFolder(final Test test, final Path basePath, final Path path, final TestExecutorTestResult testResult) {
		AbstractTest testImpl = test.getTestImpl();
		try {
			Util.ensurePathExists(path);
			testImpl.performTest(basePath, path, testResult);
		} catch (Exception e) {
			LoggerFactory.getLogger(MethodHandles.lookup().lookupClass()).error("Performing test failed.", e);
			testResult.setTestOutput(e.getMessage());
		}
	}
}
