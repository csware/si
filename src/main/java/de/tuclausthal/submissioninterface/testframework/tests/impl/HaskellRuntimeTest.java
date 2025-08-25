/*
 * Copyright 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2025 Christian Wagner <christian.wagner@campus.lmu.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTestStep;

/**
 * @author Christian Wagner
 */
public class HaskellRuntimeTest extends DockerTest {
	public HaskellRuntimeTest(final de.tuclausthal.submissioninterface.persistence.datamodel.HaskellRuntimeTest test) {
		super(test);
	}

	@Override
	protected String generateTestShellScript() {
		// Difference to DockerTest.generateTestShellScript(): continue executing testcases, even if a previous case failed
		// Reason: subsequent cases might be correct again, which is relevant for clustering the submissions correctly.
		StringBuilder testCode = new StringBuilder();
		testCode.append("#!/bin/bash\n");
		testCode.append("set -e\n");
		testCode.append(test.getPreparationShellCode());
		testCode.append("\n");

		for (DockerTestStep testStep : test.getTestSteps()) {
			testCode.append("echo '").append(getSeparator()).append("'\n");
			testCode.append("echo '").append(getSeparator()).append("' >&2\n");
			testCode.append("{\n");
			testCode.append("set +e\n");
			testCode.append(testStep.getTestcode());
			testCode.append("\n");
			testCode.append("} || echo \"ERROR: syntax error or missing function\"\n");
		}

		return testCode.toString();
	}
}
