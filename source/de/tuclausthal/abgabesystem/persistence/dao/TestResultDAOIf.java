package de.tuclausthal.abgabesystem.persistence.dao;

import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.TestResult;

/**
 * Data Access Object Interface for the TestResult-class
 * @author Sven Strickroth
 */
public interface TestResultDAOIf {
	/**
	 * Creates and stores a TestResult for a submission in the DB
	 * @param submission the submission
	 * @return the created/updated testresult
	 */
	public TestResult createTestResult(Submission submission);

	/**
	 * Update/save a testresult
	 * @param testResult the testresult to update
	 */
	public void saveTestResult(TestResult testResult);
}
