/*
 * Copyright 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2025 Esat Avci <e.avci@campus.lmu.de>
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

import java.util.Random;

import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

public class HaskellSyntaxTest extends DockerTest {

	private static final Random random = new Random();
	private final String separator;

	public HaskellSyntaxTest(de.tuclausthal.submissioninterface.persistence.datamodel.HaskellSyntaxTest test) {
		super(test);
		separator = "#<GATE@" + random.nextLong() + "#@>#";
	}

	@Override
	protected final void analyzeAndSetResult(final boolean exitedCleanly, final StringBuffer stdout, final StringBuffer stderr, final int exitCode, final boolean aborted, final TestExecutorTestResult result) {
		boolean success = exitedCleanly && !stderr.toString().toLowerCase().contains("error:");
		result.setTestPassed(success);
		result.setTestOutput(createJsonBuilder(success, stdout, stderr, exitCode, aborted).build().toString());
	}

	@Override
	protected final String generateTestShellScript() {
		return """
				#!/bin/bash
				set -e
				echo '%s'
				for file in *.hs; do
				  ghci -ignore-dot-ghci -v0 -ferror-spans -fdiagnostics-color=never -Wall -e ":load $file" -e ":quit"
				done
				""".formatted(separator);
	}
}
