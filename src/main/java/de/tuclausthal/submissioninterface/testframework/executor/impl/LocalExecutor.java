/*
 * Copyright 2009, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorIf;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;

/**
 * Local threaded ExecutionTaskExecutor implementation
 * @author Sven Strickroth
 */
public class LocalExecutor implements TestExecutorIf {
	private volatile static LocalExecutor instance = null;
	private ExecutorService executorService = Executors.newFixedThreadPool(CORES);
	public static int CORES = 1;
	public static File dataPath;

	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private LocalExecutor() {}

	/**
	 * Returns the instance (Singleton)
	 * @return the executer instance
	 */
	public static synchronized LocalExecutor getInstance() {
		if (instance == null) {
			instance = new LocalExecutor();
		}
		return instance;
	}

	@Override
	public Future<TestExecutorTestResult> executeTask(TestTask executionTask) {
		TestExecutorWorker worker = new TestExecutorWorker(dataPath, executionTask);
		return executorService.submit(worker);
	}

	public void shutdown() {
		executorService.shutdown();
		try {
			while (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
				LOG.debug("Waiting for all checks to finish: " + executorService.toString());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public boolean isRunning() {
		return executorService != null && !executorService.isShutdown() && !executorService.isTerminated();
	}
}
