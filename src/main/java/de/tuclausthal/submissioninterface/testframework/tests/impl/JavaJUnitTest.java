/*
 * Copyright 2010-2012, 2021-2024 Sven Strickroth <email@cs-ware.de>
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * @author Sven Strickroth
 */
public class JavaJUnitTest extends JavaFunctionTest {
	final private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	final static public String JUNIT_JAR = "junit.jar";
	final static public String FILENAME_PREFIX = "junittest";
	final static public String FILENAME_PATTERN = FILENAME_PREFIX + "%d.jar";

	public JavaJUnitTest(Test test) {
		super(test);
	}

	@Override
	protected boolean calculateTestResult(boolean exitedCleanly, StringBuffer processOutput, StringBuffer stdErr, boolean aborted) {
		JavaFunctionTest.cleanupStdErr(stdErr);
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
		String mainClass = ((JUnitTest) test).getMainClass();
		if (!JUnitTest.CANONICAL_CLASS_NAME.matcher(mainClass).matches()) {
			LOG.warn("illegal main-class for JUnitTest found: testid: {}, mainclass: \"{}\"", test.getId(), mainClass);
			mainClass = "AllTests";
		}
		params.add(mainClass);
	}

	@Override
	void populateJavaPolicyFile(final Path basePath, final Path tempDir, final BufferedWriter policyFileWriter) throws IOException {
		policyFileWriter.write("grant codeBase \"file:" + mkPath(basePath.resolve(JavaJUnitTest.JUNIT_JAR)) + "\" {\n");
		policyFileWriter.write("	permission java.security.AllPermission;\n");
		policyFileWriter.write("};\n");
		policyFileWriter.write("\n");
		policyFileWriter.write("grant codeBase \"file:" + mkPath(Util.constructPath(basePath, test.getTask()).resolve(String.format(FILENAME_PATTERN, test.getId()))) + "\" {\n");
		policyFileWriter.write("	permission java.lang.RuntimePermission \"setIO\";\n");
		policyFileWriter.write("	permission java.lang.RuntimePermission \"exitTheVM.*\";\n");
		policyFileWriter.write("	permission java.lang.reflect.ReflectPermission \"suppressAccessChecks\";\n");
		policyFileWriter.write("};\n");
	}

	@Override
	void populateClassPathForRunningtests(final Path basePath, final List<Path> classPath) {
		classPath.add(basePath.resolve(JavaJUnitTest.JUNIT_JAR));
		classPath.add(Util.constructPath(basePath, test.getTask()).resolve(String.format(FILENAME_PATTERN, test.getId())));
	}
}
