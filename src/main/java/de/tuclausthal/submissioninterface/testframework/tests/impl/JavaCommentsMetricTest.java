/*
 * Copyright 2010-2012, 2021 Sven Strickroth <email@cs-ware.de>
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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.SpacesTabsNewlinesNormalizer;
import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.StripCodeNormalizer;
import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.StripCommentsNormalizer;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommentsMetricTest;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * @author Sven Strickroth
 */
public class JavaCommentsMetricTest extends AbstractTest {
	final private CommentsMetricTest test;

	public JavaCommentsMetricTest(CommentsMetricTest test) {
		super(test);
		this.test = test;
	}

	@Override
	public void performTest(File basePath, File submissionPath, TestExecutorTestResult testResult) throws Exception {
		List<String> excludedFileNames = Arrays.asList(test.getExcludedFiles().split(","));

		long charsOfCode = 0;
		long charsOfComment = 0;

		StripCommentsNormalizer scn = new StripCommentsNormalizer();
		SpacesTabsNewlinesNormalizer fulln = new SpacesTabsNewlinesNormalizer();
		StripCodeNormalizer scoden = new StripCodeNormalizer();
		for (String file : Util.listFilesAsRelativeStringList(submissionPath, excludedFileNames)) {
			if (file.endsWith(".java")) {
				StringBuffer fileContents = Util.loadFile(new File(submissionPath, file));

				StringBuffer code = new StringBuffer(fileContents);
				code = fulln.normalize(scn.normalize(code));
				charsOfCode += code.length();

				fileContents = fulln.normalize(scoden.normalize(fileContents));
				charsOfComment += fileContents.length();
			}
		}

		Long ratio = 0l;
		if (charsOfCode > 0) {
			ratio = Math.round(100.0d * charsOfComment / charsOfCode);
		}

		testResult.setTestPassed(ratio >= test.getMinProzent());
		testResult.setTestOutput("Code: " + charsOfCode + "\nComment: " + charsOfComment + "\nRatio: " + ratio + "%");
	}
}
