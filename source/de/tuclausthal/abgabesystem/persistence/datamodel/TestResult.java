package de.tuclausthal.abgabesystem.persistence.datamodel;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class TestResult implements Serializable {
	private Boolean passedTest = false;
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
