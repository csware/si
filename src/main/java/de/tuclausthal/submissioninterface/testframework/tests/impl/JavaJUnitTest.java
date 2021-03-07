/*
 * Copyright 2010 - 2012 Sven Strickroth <email@cs-ware.de>
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
import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * @author Sven Strickroth
 */
public class JavaJUnitTest extends JavaFunctionTest {
	@Override
	protected boolean calculateTestResult(Test test, boolean exitedCleanly, StringBuffer processOutput, StringBuffer stdErr, boolean aborted) {
		// append STDERR
		if (stdErr.length() > 0) {
			processOutput.append("\nFehlerausgabe (StdErr)\n");
			processOutput.append(stdErr);
		}
		if (aborted) {
			processOutput.insert(0, "Student-program aborted due to too long execution time.\n\n");
		}
		return exitedCleanly;
	}

	@Override
	void populateParameters(Test test, File basePath, File tempDir, List<String> params) {
		params.add("-cp");
		params.add(basePath.getAbsolutePath() + System.getProperty("file.separator") + test.getTask().getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + test.getTask().getTaskid() + System.getProperty("file.separator") + "junittest" + test.getId() + ".jar" + File.pathSeparator + basePath.getAbsolutePath() + System.getProperty("file.separator") + "junit.jar" + File.pathSeparator + tempDir.getAbsolutePath());
		params.add("junit.textui.TestRunner");
		params.add(Util.escapeCommandlineArguments(((JUnitTest)test).getMainClass()));
	}
}
