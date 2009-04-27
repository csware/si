package de.tuclausthal.abgabesystem.persistence.dao.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.persistence.dao.TestResultDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.TestResult;

/**
 * Data Access Object implementation for the TestResultDAOIf
 * @author Sven Strickroth
 */
public class TestResultDAO implements TestResultDAOIf {
	@Override
	public TestResult createTestResult(Submission submission) {
		// Hibernate exception abfangen
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		TestResult testResult = new TestResult();
		submission.setTestResult(testResult);
		session.save(submission);
		tx.commit();
		return testResult;
	}

	@Override
	public void saveTestResult(TestResult testResult) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		session.update(testResult);
		tx.commit();
	}
}
