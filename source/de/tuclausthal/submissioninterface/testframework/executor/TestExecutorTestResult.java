package de.tuclausthal.submissioninterface.testframework.executor;

/**
 * @author Sven Strickroth
 */
public class TestExecutorTestResult {
	private boolean testPassed = false;
	private String testOutput = "";

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
}
