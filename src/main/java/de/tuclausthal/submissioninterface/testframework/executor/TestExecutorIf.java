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

package de.tuclausthal.submissioninterface.testframework.executor;

import java.util.concurrent.Future;

import de.tuclausthal.submissioninterface.testframework.tests.TestTask;

/**
 * ExecutionTaskExecuter interface
 * Class which executes a concrete executiontask
 * @author Sven Strickroth
 */
public interface TestExecutorIf {
	/**
	 * Executes the given executionTask
	 * @param executionTask the executiontask to execute
	 * @return a future-reference
	 */
	public Future<TestExecutorTestResult> executeTask(TestTask executionTask);
}
