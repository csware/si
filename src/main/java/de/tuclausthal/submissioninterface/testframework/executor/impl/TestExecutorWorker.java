/*
 * Copyright 2009-2010, 2023 Sven Strickroth <email@cs-ware.de>
 *
 * This file is part of the GATE.
 *
 * GATE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * GATE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GATE. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.testframework.executor.impl;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;

/**
 * @author Sven Strickroth
 */
public class TestExecutorWorker implements Callable<TestExecutorTestResult> {
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private File dataPath;
	private TestTask executionTask;

	public TestExecutorWorker(File dataPath, TestTask executionTask) {
		this.dataPath = dataPath;
		this.executionTask = executionTask;
	}

	@Override
	public TestExecutorTestResult call() throws Exception {
		LOG.debug("Performing test {} on submissionid {}", executionTask.getTestId(), executionTask.getSubmissionId());
		TestExecutorTestResult testResult = new TestExecutorTestResult();
		executionTask.performTask(dataPath, testResult);
		LOG.debug("Finished performing test {} on submissionid {}, passed: {}", executionTask.getTestId(), executionTask.getSubmissionId(), testResult.isTestPassed());
		return testResult;
	}
}
