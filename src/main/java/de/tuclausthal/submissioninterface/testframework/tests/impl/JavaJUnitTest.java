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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * @author Sven Strickroth
 */
public class JavaJUnitTest extends JavaFunctionTest {
	final static public String JUNIT_JAR = "junit.jar";

	public JavaJUnitTest(Test test) {
		super(test);
	}

	@Override
	protected boolean calculateTestResult(boolean exitedCleanly, StringBuffer processOutput, StringBuffer stdErr, boolean aborted) {
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
	void populateParameters(List<String> params) {
		params.add("junit.textui.TestRunner");
		params.add(Util.escapeCommandlineArguments(((JUnitTest) test).getMainClass()));
	}

	@Override
	void populateJavaPolicyFile(File basePath, File tempDir, BufferedWriter policyFileWriter) throws IOException {
		policyFileWriter.write("grant codeBase \"file:" + mkPath(basePath.getAbsolutePath() + System.getProperty("file.separator") + JavaJUnitTest.JUNIT_JAR) + "\" {\n");
		policyFileWriter.write("	permission java.security.AllPermission;\n");
		policyFileWriter.write("};\n");
		policyFileWriter.write("\n");
		policyFileWriter.write("grant codeBase \"file:" + mkPath(basePath.getAbsolutePath() + System.getProperty("file.separator") + test.getTask().getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + test.getTask().getTaskid() + System.getProperty("file.separator") + "junittest" + test.getId() + ".jar") + "\" {\n");
		policyFileWriter.write("	permission java.lang.RuntimePermission \"setIO\";\n");
		policyFileWriter.write("	permission java.lang.RuntimePermission \"exitTheVM.*\";\n");
		policyFileWriter.write("	permission java.lang.reflect.ReflectPermission \"suppressAccessChecks\";\n");
		policyFileWriter.write("};\n");
	}

	@Override
	void populateClassPathForRunningtests(File basePath, List<File> classPath) {
		classPath.add(new File(basePath, JavaJUnitTest.JUNIT_JAR));
		classPath.add(new File(basePath, test.getTask().getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + test.getTask().getTaskid() + System.getProperty("file.separator") + "junittest" + test.getId() + ".jar"));
	}
}
