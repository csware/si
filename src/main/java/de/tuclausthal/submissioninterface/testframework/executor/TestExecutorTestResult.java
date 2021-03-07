/*
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.executor;

/**
 * @author Sven Strickroth
 */
public class TestExecutorTestResult {
	private boolean testPassed = false;
	private String testOutput = "";
	private int testID;

	/**
	 * @return the testPassed
	 */
	public boolean isTestPassed() {
		return testPassed;
	}

	/**
	 * @param testPassed the testPassed to set
	 */
	public void setTestPassed(boolean testPassed) {
		this.testPassed = testPassed;
	}

	/**
	 * @return the testOutput
	 */
	public String getTestOutput() {
		return testOutput;
	}

	/**
	 * @param testOutput the testOutput to set
	 */
	public void setTestOutput(String testOutput) {
		this.testOutput = testOutput;
	}

	/**
	 * @return the testID
	 */
	public int getTestID() {
		return testID;
	}

	/**
	 * @param testID the testID to set
	 */
	public void setTestID(int testID) {
		this.testID = testID;
	}
}
