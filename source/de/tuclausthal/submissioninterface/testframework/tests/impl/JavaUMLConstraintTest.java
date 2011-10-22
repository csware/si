/*
 * Copyright 2011 Joachim Schramm
 * Copyright 2011 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.uml.ActivityDiagramm;
import de.tuclausthal.submissioninterface.testframework.tests.impl.uml.ActivityDiagrammConstraint;
import de.tuclausthal.submissioninterface.testframework.tests.impl.uml.ClassDiagramm;
import de.tuclausthal.submissioninterface.testframework.tests.impl.uml.ClassDiagrammConstraint;
import de.tuclausthal.submissioninterface.testframework.tests.impl.uml.UMLDiagramm;

/**
 * Diese Klasse sorgt für die Ausgaben beim Feedback
 * @author Joachim Schramm
 */
public class JavaUMLConstraintTest extends AbstractTest {

	@Override
	public void performTest(Test test, Submission submission, File basePath, File submissionPath, TestExecutorTestResult testResult) throws Exception {

		//Feedbackoutput
		String output = "";

		File musterLoesung = new File(basePath.getAbsolutePath() + System.getProperty("file.separator") + test.getTask().getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + test.getTask().getTaskid() + System.getProperty("file.separator") + "musterloesung" + test.getId() + ".xmi");
		File studentenLoesung = new File(submissionPath, "loesung.xmi");

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
		output = output + "Musterlösung:"+"\n";
		output = output + ""+"\n";

		output = output + diagrammMusterLoesung();

		output = output + ""+"\n";
		*/
		output = output + "Studentenlösung:" + "\n";
		output = output + "" + "\n";

		output = output + diagramm.toString();

		output = output + "" + "\n";

		output = output + "Vergleichsergebnis:" + "\n";
		output = output + "" + "\n";
		output = output + diagrammMusterLoesung.compareTextResult(diagramm);

		testResult.setTestOutput(output);
		testResult.setTestPassed(true);
	}
}
