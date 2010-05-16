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
		executionTask.performTask(dataPath, testResult);
		return testResult;
	}
}
