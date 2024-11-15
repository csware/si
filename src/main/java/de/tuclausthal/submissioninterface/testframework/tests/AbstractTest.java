/*
 * Copyright 2010-2012, 2017, 2021, 2023-2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.tests;

import java.nio.file.Path;

import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

/**
 * @author Sven Strickroth
 * @param <T> Test type
 */
public abstract class AbstractTest<T extends Test> {
	final protected T test;

	/**
	 * @param test
	 */
	public AbstractTest(final T test) {
		this.test = test;
	}

	/**
	 * @param basePath
	 * @param submissionPath
	 * @param testResult
	 * @throws Exception
	 */
	public abstract void performTest(final Path basePath, final Path submissionPath, final TestExecutorTestResult testResult) throws Exception;

	/**
	 * Prepares a windows path (windows needs double backslash)
	 * @param absolutePath the original path
	 * @return an escaped path
	 */
	final static protected String mkPath(String absolutePath) {
		if (System.getProperty("file.separator").equals("\\")) {
			return absolutePath.replace("\\", "\\\\");
		}
		return absolutePath;
	}

	final static protected String mkPath(final Path path) {
		return mkPath(path.toAbsolutePath().toString());
	}
}
