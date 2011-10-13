/*
 * Copyright 2011 Joachim Schramm
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

		String musterLoesung = basePath.getAbsolutePath() + System.getProperty("file.separator") + test.getTask().getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + test.getTask().getTaskid() + System.getProperty("file.separator") + "musterloesung" + test.getId() + ".xmi";
		String studentenLoesung = new File(submissionPath, "loesung.xmi").getAbsolutePath();

		UMLDiagramm diagramm = new UMLDiagramm(musterLoesung);

		//Klassen oder Aktivitätsdiagramm
		int diagrammType = diagramm.read(diagramm);

		//Ausgaben, falls Aktivitätsdiagramm
		if (diagrammType == 1) {
			ActivityDiagramm activitydiagramm = new ActivityDiagramm(musterLoesung);
			ActivityDiagramm activitydiagramm2 = new ActivityDiagramm(studentenLoesung);
			activitydiagramm.read(activitydiagramm);
			activitydiagramm2.read(activitydiagramm2);

			ActivityDiagrammConstraint adc = new ActivityDiagrammConstraint();
			/*
			output = output + "Musterlösung:"+"\n";
			output = output + ""+"\n";
			
			output = output + "Endzustände: "+ activitydiagramm.getNumberOfFinalStates()+"\n";
			output = output + "Transitionen: "+ activitydiagramm.getNumberOfTransitions()+"\n";
			output = output + "Aktivitäten: "+ activitydiagramm.getNumberOfActionStates()+"\n";
			output = output + "Startzustände: "+ activitydiagramm.getNumberOfInitials()+"\n";
			output = output + "Vereinigungen: "+ activitydiagramm.getNumberOfJoins()+"\n";
			output = output + "Gabelungen: "+ activitydiagramm.getNumberOfForks()+"\n";
			output = output + "Kreuzungen: "+ activitydiagramm.getNumberOfJunctions()+"\n";
			output = output + "Bedingungen: "+ activitydiagramm.getNumberOfSignalEvents()+"\n";
			output = output + "Schleifen: "+ activitydiagramm.getNumberOfRepeats()+"\n";
			
			output = output + ""+"\n";
			*/
			output = output + "Studentenlösung:" + "\n";
			output = output + "" + "\n";

			output = output + "Endzustände: " + activitydiagramm2.getNumberOfFinalStates() + "\n";
			output = output + "Transitionen: " + activitydiagramm2.getNumberOfTransitions() + "\n";
			output = output + "Aktivitäten: " + activitydiagramm2.getNumberOfActionStates() + "\n";
			output = output + "Startzustände: " + activitydiagramm2.getNumberOfInitials() + "\n";
			output = output + "Vereinigungen: " + activitydiagramm2.getNumberOfJoins() + "\n";
			output = output + "Gabelungen: " + activitydiagramm2.getNumberOfForks() + "\n";
			output = output + "Kreuzungen: " + activitydiagramm2.getNumberOfJunctions() + "\n";
			output = output + "Bedingungen: " + activitydiagramm2.getNumberOfSignalEvents() + "\n";
			output = output + "Schleifen: " + activitydiagramm2.getNumberOfRepeats() + "\n";

			output = output + "" + "\n";

			output = output + "Vergleichsergebnis:" + "\n";
			output = output + "" + "\n";

			output = output + adc.checkNumberOfInitials(activitydiagramm, activitydiagramm2) + "\n";
			output = output + adc.checkNumberOfFinals(activitydiagramm, activitydiagramm2) + "\n";
			output = output + adc.checkNumberOfStates(activitydiagramm, activitydiagramm2) + "\n";
			output = output + adc.checkNumberOfTransitions(activitydiagramm, activitydiagramm2) + "\n";
			output = output + adc.checkNumberOfForks(activitydiagramm, activitydiagramm2) + "\n";
			output = output + adc.checkNumberOfJoins(activitydiagramm, activitydiagramm2) + "\n";
			output = output + adc.checkNumberOfJunctions(activitydiagramm, activitydiagramm2) + "\n";
			output = output + adc.checkNumberOfSignalEvents(activitydiagramm, activitydiagramm2) + "\n";
			output = output + adc.checkNumberOfRepeats(activitydiagramm, activitydiagramm2) + "\n";

		}
		//Ausgaben, falls Klassendiagramm
		else if (diagrammType == 0) {
			//Instanz für Musterloesung erzeugen
			ClassDiagramm classdiagramm = new ClassDiagramm(musterLoesung);
			//Instanz für Studentenloesung erzeugen
			ClassDiagramm classdiagramm2 = new ClassDiagramm(studentenLoesung);
			//Instanzen einlesen
			classdiagramm.read(classdiagramm);
			classdiagramm2.read(classdiagramm2);

			ClassDiagrammConstraint cdc = new ClassDiagrammConstraint();

			/*
			output = output + "Musterlösung:"+"\n";
			output = output + ""+"\n";

			output = output + "Klassen: "+classdiagramm.getNumberOfClasses()+"\n";
			output = output + classdiagramm.getClassNames()+"\n";
			output = output + "Interfaces: "+ classdiagramm.getNumberOfInterfaces()+"\n";
			output = output + classdiagramm.getInterfaceNames()+"\n";
			output = output + "Attribute: "+classdiagramm.getNumberOfAttributes()+"\n";
			output = output + classdiagramm.getAttributeNames()+"\n";
			output = output + "Methoden: "+classdiagramm.getNumberOfMethods()+"\n";
			output = output + classdiagramm.getMethodsNames()+"\n";
			output = output + "Assoziationen: "+classdiagramm.getNumberOfAssociations()+"\n";
			output = output + classdiagramm.getAssociationNames()+"\n";
			output = output + "Aggregationen: "+classdiagramm.getNumberOfAggregates()+"\n";
			output = output + "Kompositionen: "+classdiagramm.getNumberOfComposites()+"\n";
			output = output + "Generalisierungen: "+classdiagramm.getNumberOfGeneralizations()+"\n";
			output = output + "Interfacerealisierungen: "+classdiagramm.getNumberOfAbstractions()+"\n";
			
			
			output = output + "Zuordnung von Methoden zu Klassen: "+classdiagramm.getM2C()+"\n";
			output = output + "Zuordnung von Attributen zu Klassen: "+classdiagramm.getA2C()+"\n";
			output = output + "Zuordnung von Methoden zu Interfaces: "+classdiagramm.getM2I()+"\n";
			output = output + "Zuordnung von Assoziationen zu Klassen: "+classdiagramm.getAs2C()+"\n";
			//output = output + classdiagramm.getAllocationOfAssociation()+"\n";
			
			output = output + ""+"\n";
			*/
			output = output + "Studentenlösung:" + "\n";
			output = output + "" + "\n";

			output = output + "Klassen: " + classdiagramm2.getNumberOfClasses() + "\n";
			output = output + classdiagramm2.getClassNames() + "\n";
			output = output + "Schnittstellen: " + classdiagramm2.getNumberOfInterfaces() + "\n";
			output = output + classdiagramm2.getInterfaceNames() + "\n";
			output = output + "Attribute: " + classdiagramm2.getNumberOfAttributes() + "\n";
			output = output + classdiagramm2.getAttributeNames() + "\n";
			output = output + "Methoden: " + classdiagramm2.getNumberOfMethods() + "\n";
			output = output + classdiagramm2.getMethodsNames() + "\n";
			output = output + "Assoziationen: " + classdiagramm2.getNumberOfAssociations() + "\n";
			output = output + classdiagramm2.getAssociationNames() + "\n";
			output = output + "Aggregationen: " + classdiagramm2.getNumberOfAggregates() + "\n";
			output = output + "Kompositionen: " + classdiagramm2.getNumberOfComposites() + "\n";
			output = output + "Generalisierungen: " + classdiagramm2.getNumberOfGeneralizations() + "\n";
			output = output + "Schnittstellenrealisierungen: " + classdiagramm2.getNumberOfAbstractions() + "\n";

			output = output + "Zuordnung von Methoden zu Klassen: " + classdiagramm2.getM2C() + "\n";
			output = output + "Zuordnung von Attributen zu Klassen: " + classdiagramm2.getA2C() + "\n";
			output = output + "Zuordnung von Methoden zu Interfaces: " + classdiagramm2.getM2I() + "\n";
			output = output + "Zuordnung von Assoziationen zu Klassen: " + classdiagramm2.getAs2C() + "\n";
			//output = output + classdiagramm2.getAllocationOfAssociation()+"\n";

			output = output + "" + "\n";
			output = output + "Vergleichsergebnis:" + "\n";
			output = output + "" + "\n";

			output = output + cdc.checkNumberOfClassesPlusAttributes(classdiagramm, classdiagramm2) + "\n";
			output = output + cdc.checkNumberOfInterfaces(classdiagramm, classdiagramm2) + "\n";
			output = output + cdc.checkNumberOfMethods(classdiagramm, classdiagramm2) + "\n";
			output = output + cdc.checkNumberOfAssociations(classdiagramm, classdiagramm2) + "\n";
			output = output + cdc.checkNumberOfGeneralizations(classdiagramm, classdiagramm2) + "\n";
			output = output + cdc.checkNumberOfAbstractions(classdiagramm, classdiagramm2) + "\n";
			output = output + cdc.checkNumberOfAggregates(classdiagramm, classdiagramm2) + "\n";
			output = output + cdc.checkNumberOfComposites(classdiagramm, classdiagramm2) + "\n";

			output = output + cdc.checkNamesOfClassesAndAttributes(classdiagramm, classdiagramm2) + "\n";
			output = output + cdc.checkNamesOfMethods(classdiagramm, classdiagramm2) + "\n";
			output = output + cdc.checkNamesOfInterfaces(classdiagramm, classdiagramm2) + "\n";

			output = output + cdc.checkA2C(classdiagramm, classdiagramm2) + "\n";
			output = output + cdc.checkM2C(classdiagramm, classdiagramm2) + "\n";
			output = output + cdc.checkM2I(classdiagramm, classdiagramm2) + "\n";

			output = output + cdc.checkPairsOfAssociations(classdiagramm, classdiagramm2) + "\n";

		} else {

			output = output + "Unbekannter Diagrammtyp";
		}

		testResult.setTestOutput(output);
		testResult.setTestPassed(true);

	}

}
