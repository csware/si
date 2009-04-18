package de.tuclausthal.abgabesystem.persistence.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TestResult implements Serializable {
	public Boolean passedTest = false;
	public String testOutput;

	/**
	 * @return the passedTest
	 */
	@Column(nullable = false)
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
	@Column(nullable = false)
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
