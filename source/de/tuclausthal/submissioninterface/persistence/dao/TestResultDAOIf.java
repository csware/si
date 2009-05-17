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

package de.tuclausthal.submissioninterface.persistence.dao;

import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;

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
