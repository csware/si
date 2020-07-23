/*
 * Copyright 2009 - 2010, 2020 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.TestResultDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

/**
 * Data Access Object implementation for the TestResultDAOIf
 * @author Sven Strickroth
 */
public class TestResultDAO extends AbstractDAO implements TestResultDAOIf {
	public TestResultDAO(Session session) {
		super(session);
	}

	@Override
	public void storeTestResult(Test test, Submission submission, TestExecutorTestResult testExecutorTestResult) {
		Session session = getSession();
		session.buildLockRequest(LockOptions.UPGRADE).lock(test);
		TestResult testResult = getResultLocked(test, submission);
		if (testResult == null) {
			testResult = new TestResult();
			testResult.setSubmission(submission);
			testResult.setTest(test);
		}
		if (testExecutorTestResult != null) {
			testResult.setPassedTest(testExecutorTestResult.isTestPassed());
			testResult.setTestOutput(testExecutorTestResult.getTestOutput());
			session.saveOrUpdate(testResult);
		} else {
			session.delete(testResult);
		}
	}

	@Override
	public void saveTestResult(TestResult testResult) {
		Session session = getSession();
		session.update(testResult);
	}

	@Override
	public TestResult getResult(Test test, Submission submission) {
		return (TestResult) getSession().createCriteria(TestResult.class).add(Restrictions.eq("test", test)).add(Restrictions.eq("submission", submission)).uniqueResult();
	}

	@Override
	public TestResult getResultLocked(Test test, Submission submission) {
		return (TestResult) getSession().createCriteria(TestResult.class).add(Restrictions.eq("test", test)).add(Restrictions.eq("submission", submission)).setLockMode(LockMode.PESSIMISTIC_WRITE).uniqueResult();
	}
}
