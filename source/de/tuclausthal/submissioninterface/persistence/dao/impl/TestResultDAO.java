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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.TestResultDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;

/**
 * Data Access Object implementation for the TestResultDAOIf
 * @author Sven Strickroth
 */
public class TestResultDAO implements TestResultDAOIf {
	@Override
	public TestResult createTestResult(Test test, Submission submission) {
		// Hibernate exception abfangen
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
		TestResult testResult = new TestResult();
		testResult.setSubmission(submission);
		testResult.setTest(test);
		session.save(submission);
		tx.commit();
		return testResult;
	}

	@Override
	public void saveTestResult(TestResult testResult) {
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
		session.update(testResult);
		tx.commit();
	}
}
