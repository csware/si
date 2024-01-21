/*
 * Copyright 2011, 2017, 2021-2022, 2024 Sven Strickroth <email@cs-ware.de>
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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tuclausthal.submissioninterface.persistence.datamodel.RegExpTest;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * @author Sven Strickroth
 */
public class JavaIORegexpTest extends JavaFunctionTest {
	private final RegExpTest regExpTest;

	public JavaIORegexpTest(RegExpTest regExpTest) {
		super(regExpTest);
		this.regExpTest = regExpTest;
	}

	@Override
	protected boolean calculateTestResult(boolean exitedCleanly, StringBuffer processOutput, StringBuffer stdErr, boolean aborted) {
		Pattern testPattern = Pattern.compile(regExpTest.getRegularExpression());
		Matcher testMatcher = testPattern.matcher(processOutput.toString().trim());
		if (!testMatcher.matches()) {
			processOutput.insert(0, "Ausgabe stimmt nicht mit erwarteter Ausgabe überein. Ausgabe folgt (StdIn zuerst):\n");
			exitedCleanly = false;
		}

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
		params.add(Util.escapeCommandlineArguments(regExpTest.getMainClass()));
		if (regExpTest.getCommandLineParameter() != null && !regExpTest.getCommandLineParameter().isEmpty()) {
			params.addAll(Arrays.asList(Util.escapeCommandlineArguments(regExpTest.getCommandLineParameter()).split(" ")));
		}
	}

	@Override
	void populateJavaPolicyFile(final Path basePath, final Path tempDir, final BufferedWriter policyFileWriter) throws IOException {}
}
