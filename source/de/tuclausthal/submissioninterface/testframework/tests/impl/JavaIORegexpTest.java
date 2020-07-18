/*
 * Copyright 2011, 2017 Sven Strickroth <email@cs-ware.de>
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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tuclausthal.submissioninterface.persistence.datamodel.RegExpTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * @author Sven Strickroth
 */
public class JavaIORegexpTest extends JavaFunctionTest {
	@Override
	protected boolean calculateTestResult(Test test, boolean exitedCleanly, StringBuffer processOutput) {
		Pattern testPattern = Pattern.compile(((RegExpTest) test).getRegularExpression());
		Matcher testMatcher = testPattern.matcher(processOutput.toString().trim());
		if (!testMatcher.matches()) {
			processOutput.insert(0, "Ausgabe stimmt nicht mit erwarteter Ausgabe überein. Ausgabe folgt (StdIn zuerst):\n");
			return false;
		}
		return exitedCleanly;
	}

	@Override
	void populateParameters(Test test, File basePath, File tempDir, List<String> params) {
		RegExpTest regExpTest = (RegExpTest) test;
		params.add(Util.escapeCommandlineArguments(regExpTest.getMainClass()));
		if (regExpTest.getCommandLineParameter() != null && !regExpTest.getCommandLineParameter().isEmpty()) {
			params.addAll(Arrays.asList(Util.escapeCommandlineArguments(regExpTest.getCommandLineParameter()).split(" ")));
		}
	}
}
