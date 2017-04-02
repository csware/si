/*
 * Copyright 2010-2012, 2017 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.tests;

import java.io.File;

import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

/**
 * @author Sven Strickroth
 */
public abstract class AbstractTest {
	/**
	 * @param test
	 * @param basePath
	 * @param submissionPath
	 * @param testResult
	 * @throws Exception
	 */
	public abstract void performTest(Test test, File basePath, File submissionPath, TestExecutorTestResult testResult) throws Exception;

	/**
	 * Prepares a windows path (windows needs double backslash)
	 * @param absolutePath the original path
	 * @return an escaped path
	 */
	final protected String mkPath(String absolutePath) {
		if (System.getProperty("file.separator").equals("\\")) {
			return absolutePath.replace("\\", "\\\\");
		} else {
			return absolutePath;
		}
	}

}
