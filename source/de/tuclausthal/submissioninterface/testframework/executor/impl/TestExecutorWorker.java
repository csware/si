package de.tuclausthal.submissioninterface.testframework.executor.impl;

import java.io.File;
import java.util.concurrent.Callable;

import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;

/**
 * @author Sven Strickroth
 */
public class TestExecutorWorker implements Callable<TestExecutorTestResult> {
	private File dataPath;
	private TestTask executionTask;

	public TestExecutorWorker(File dataPath, TestTask executionTask) {
		this.dataPath = dataPath;
		this.executionTask = executionTask;
	}

	@Override
	public TestExecutorTestResult call() throws Exception {
		TestExecutorTestResult testResult = new TestExecutorTestResult();
		if (executionTask.requiresTempDir()) {
			
		}
		executionTask.performTask(dataPath, testResult);
		if (executionTask.requiresTempDir()) {
			
		}
		return testResult;
	}
}
