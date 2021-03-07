/*
 * Copyright 2010-2012, 2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.tests.impl;

import java.io.File;
import java.io.IOException;

import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * @author Sven Strickroth
 */
public abstract class TempDirTest extends AbstractTest {
	@Override
	public void performTest(Test test, File basePath, File submissionPath, TestExecutorTestResult testResult) throws Exception {
		// check if we are already in an tempdir, i.e., we're executing a student test as Tutor
		File checkPath = submissionPath;
		boolean isAlreadyTempDir = true;
		while (checkPath != null) {
			if (basePath.equals(checkPath)) {
				isAlreadyTempDir = false;
				break;
			}
			checkPath = checkPath.getParentFile();
		}
		if (isAlreadyTempDir) {
			performTestInTempDir(test, basePath, submissionPath, testResult);
		} else {
			File tempDir = null;
			try {
				tempDir = Util.createTemporaryDirectory("test");
				if (tempDir == null) {
					throw new IOException("Failed to create tempdir!");
				}

				// prepare tempdir
				Util.recursiveCopy(submissionPath, tempDir);

				performTestInTempDir(test, basePath, tempDir, testResult);
			} finally {
				if (tempDir != null) {
					Util.recursiveDelete(tempDir);
				}
			}
		}
	}

	abstract protected void performTestInTempDir(Test test, File basePath, File tempDir, TestExecutorTestResult testResult) throws Exception;
}
