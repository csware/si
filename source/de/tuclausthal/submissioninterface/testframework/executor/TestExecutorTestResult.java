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
