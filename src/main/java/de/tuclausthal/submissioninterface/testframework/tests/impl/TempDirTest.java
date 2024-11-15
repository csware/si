/*
 * Copyright 2010-2012, 2021, 2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.tests.impl;

import java.io.IOException;
import java.nio.file.Path;

import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * @author Sven Strickroth
 * @param <T> 
 */
public abstract class TempDirTest<T extends Test> extends AbstractTest<T> {
	public TempDirTest(final T test) {
		super(test);
	}

	@Override
	public void performTest(final Path basePath, final Path submissionPath, final TestExecutorTestResult testResult) throws Exception {
		// check if we are already in an tempdir, i.e., we're executing a student test as Tutor
		Path checkPath = submissionPath;
		boolean isAlreadyTempDir = true;
		while (checkPath != null) {
			if (basePath.equals(checkPath)) {
				isAlreadyTempDir = false;
				break;
			}
			checkPath = checkPath.getParent();
		}
		if (isAlreadyTempDir) {
			performTestInTempDir(basePath, submissionPath, testResult);
		} else {
			Path tempDir = null;
			try {
				tempDir = Util.createTemporaryDirectory("test");
				if (tempDir == null) {
					throw new IOException("Failed to create tempdir!");
				}

				// prepare tempdir
				Util.recursiveCopy(submissionPath, tempDir);

				performTestInTempDir(basePath, tempDir, testResult);
			} finally {
				if (tempDir != null) {
					Util.recursiveDelete(tempDir);
				}
			}
		}
	}

	abstract protected void performTestInTempDir(final Path basePath, final Path tempDir, final TestExecutorTestResult testResult) throws Exception;
}
