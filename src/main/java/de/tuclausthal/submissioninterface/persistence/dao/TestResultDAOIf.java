/*
 * Copyright 2009-2010, 2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import java.util.Map;

import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

/**
 * Data Access Object Interface for the TestResult-class
 * @author Sven Strickroth
 */
public interface TestResultDAOIf {
	/**
	 * Creates and stores a TestResult for a submission in the DB
	 * @param test the test
	 * @param submission the submission
	 * @param testExecutorTestResult 
	 */
	public void storeTestResult(Test test, Submission submission, TestExecutorTestResult testExecutorTestResult);

	/**
	 * Update/save a testresult
	 * @param testResult the testresult to update
	 */
	public void saveTestResult(TestResult testResult);

	public TestResult getResult(Test test, Submission submission);

	public TestResult getResultLocked(Test test, Submission submission);

	public Map<Integer, Map<Integer, Boolean>> getResults(Task task);
}
