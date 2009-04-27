package de.tuclausthal.abgabesystem.persistence.dao;

import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.TestResult;

public interface TestResultDAOIf {
	public TestResult createTestResult(Submission submission);
	public void saveTestResult(TestResult testResult);
}
