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

package de.tuclausthal.submissioninterface.persistence.datamodel;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Lob;

@Embeddable
public class TestResult implements Serializable {
	private Boolean passedTest = null;
	private String testOutput = "not tested";

	/**
	 * @return the passedTest
	 */
	public Boolean getPassedTest() {
		return passedTest;
	}

	/**
	 * @param passedTest the passedTest to set
	 */
	public void setPassedTest(Boolean passedTest) {
		this.passedTest = passedTest;
	}

	/**
	 * @return the testOutput
	 */
	@Lob
	public String getTestOutput() {
		return testOutput;
	}

	/**
	 * @param testOutput the testOutput to set
	 */
	public void setTestOutput(String testOutput) {
		this.testOutput = testOutput;
	}
}
