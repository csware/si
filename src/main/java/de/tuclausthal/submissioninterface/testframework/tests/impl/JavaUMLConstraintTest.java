/*
 * Copyright 2011-2012, 2021, 2023-2024 Sven Strickroth <email@cs-ware.de>
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

import java.nio.file.Path;

import de.tuclausthal.submissioninterface.persistence.datamodel.UMLConstraintTest;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.uml.UMLDiagramm;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Provides feedback for UML diagrams
 * @author Sven Strickroth
 */
public class JavaUMLConstraintTest extends AbstractTest {
	public JavaUMLConstraintTest(UMLConstraintTest umlConstraintTest) {
		super(umlConstraintTest);
	}

	@Override
	public void performTest(final Path basePath, final Path submissionPath, final TestExecutorTestResult testResult) throws Exception {
		String output = "";

		final Path musterLoesung = Util.constructPath(basePath, test.getTask()).resolve("musterloesung" + test.getId() + ".xmi");
		final Path studentenLoesung = submissionPath.resolve("loesung.xmi");

		UMLDiagramm diagramm = UMLDiagramm.getDiagramm(studentenLoesung);
		UMLDiagramm diagrammMusterLoesung = UMLDiagramm.getDiagramm(musterLoesung);

		if (diagrammMusterLoesung == null) {
			testResult.setTestOutput("Musterlösung defekt.");
			testResult.setTestPassed(false);
			return;
		}

		if (diagramm == null) {
			testResult.setTestOutput("Unbekannter Diagrammtyp.");
			testResult.setTestPassed(false);
			return;
		}

		if (!diagrammMusterLoesung.isSameType(diagramm)) {
			testResult.setTestOutput("Erwarteter Diagrammtyp: " + diagrammMusterLoesung.getType() + ", Gefundener Diagramm-Typ: " + diagramm.getType());
			testResult.setTestPassed(false);
			return;
		}

		/*
		output += "Musterlösung:"+"\n";
		output += "\n";

		output += diagrammMusterLoesung();

		output += "\n";
		*/
		output += "Studierendenlösung:\n";
		output += "\n";

		output += diagramm.toString();

		output += "\n";

		output += "Vergleichsergebnis:\n";
		output += "\n";
		output += diagrammMusterLoesung.compareTextResult(diagramm);

		testResult.setTestOutput(output);
		testResult.setTestPassed(true);
	}
}
