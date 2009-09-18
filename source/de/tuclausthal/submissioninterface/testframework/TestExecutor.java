package de.tuclausthal.submissioninterface.testframework;

import java.util.concurrent.Future;

import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorIf;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.executor.impl.LocalExecutor;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;

/**
 * @author Sven Strickroth
 *
 */
public class TestExecutor implements TestExecutorIf {
	@Override
	public Future<TestExecutorTestResult> executeTask(TestTask executionTask) {
		return LocalExecutor.getInstance().executeTask(executionTask);
	}
}
