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

package de.tuclausthal.submissioninterface.testframework.tests.impl.uml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.w3c.dom.Node;

/**
 * Diese Klasse liesst die XMI Datei mit Aktivitätsdiagramminhalt ein
 * und speichert die Elemente des Aktivitätsdiagramm
 * @author Joachim Schramm
 */
public class ActivityDiagramm extends UMLDiagramm {
	private int numberOfFinalStates;
	private int numberOfSignalEvents;
	private int numberOfCompositeStates;
	private int numberOfActionStates;
	private int numberOfUninterpretedActions;
	private int numberOfActionExpressions;
	private int numberOfTransitions;

	private int numberOfPseudoStates;
	private int numberOfInitials;
	private int numberOfForks;
	private int numberOfJoins;
	private int numberOfJunctions;
	private int numberOfRepeats;

	private int numberOfIncomingTransitionsInAnActionState;
	private int numberOfOutgoingTransitionsOfAnActionState;
	private String stateVertex = "";
	private boolean isActionState = false;

	public ActivityDiagramm(String name) {
		super(name);
	}

	public ActivityDiagramm(File file, Node xmiContentNode) {
		super(file, xmiContentNode);
	}

	//Startzustaende
	public int getNumberOfInitials() {
		return numberOfInitials;
	}

	public void setNumberOfInitials(String line) {
		if (line.contains("kind = 'initial'")) {
			numberOfInitials++;
		}
	}

	//Gabelungen
	public int getNumberOfForks() {
		return numberOfForks;
	}

	public void setNumberOfForks(String line) {
		if (line.contains("kind = 'fork'")) {
			numberOfForks++;
		}
	}

	//Vereinigungen
	public int getNumberOfJoins() {
		return numberOfJoins;
	}

	public void setNumberOfJoins(String line) {
		if (line.contains("kind = 'join'")) {
			numberOfJoins++;
		}
	}

	//Kreuzungen
	public int getNumberOfJunctions() {
		return numberOfJunctions;
	}

	public void setNumberOfJunctions(String line) {
		if (line.contains("kind = 'junction'")) {
			numberOfJunctions++;
		}
	}

	//Endzustände
	public int getNumberOfFinalStates() {
		return numberOfFinalStates;
	}

	public void setNumberOfFinalStates(String line) {
		if (line.contains("UML:FinalState xmi.id =")) {
			numberOfFinalStates++;
		}
	}

	//Kreuzungsbedingungen
	public int getNumberOfSignalEvents() {
		return numberOfSignalEvents;
	}

	public void setNumberOfSignalEvents(String line) {
		if (line.contains("UML:SignalEvent xmi.id =")) {
			numberOfSignalEvents++;
		}
	}

	public int getNumberOfCompositeStates() {
		return numberOfCompositeStates;
	}

	public void setNumberOfCompositeStates(String line) {
		if (line.contains("UML:CompositeState xmi.id =")) {
			numberOfCompositeStates++;
		}
	}

	public int getNumberOfPseudoStates() {
		return numberOfPseudoStates;
	}

	public void setNumberOfPseudoStates(String line) {
		if (line.contains("UML:Pseudostate xmi.id =")) {
			numberOfPseudoStates++;
		}
	}

	//Aktivitaet
	public int getNumberOfActionStates() {
		return numberOfActionStates;
	}

	public void setNumberOfActionStates(String line) {
		if (line.contains("<UML:ActionState xmi.id =")) {
			numberOfActionStates++;
			numberOfIncomingTransitionsInAnActionState = 0;
			numberOfOutgoingTransitionsOfAnActionState = 0;
			isActionState = true;
		}
	}

	public void isNotAnActionStates(String line) {
		if (line.contains("</UML:ActionState>")) {
			isActionState = false;
		}
	}

	public void setStateVertex(String line) {
		if (isActionState) {
			if (line.contains("<UML:StateVertex.outgoing")) {
				stateVertex = "out";
			} else if (line.contains("<UML:StateVertex.incoming")) {
				stateVertex = "in";
			} else if (line.contains("</UML:StateVertex.outgoing>")) {
				stateVertex = "";
			} else if (line.contains("</UML:StateVertex.incoming>")) {
				stateVertex = "";
			}
		}
	}

	public void setIncomingAndOutgoingTransitionOfAnActionState(String line) {
		if (line.contains("UML:Transition xmi.idref = ") && stateVertex.equals("out")) {
			numberOfOutgoingTransitionsOfAnActionState++;
		} else if (line.contains("UML:Transition xmi.idref = ") && stateVertex.equals("in")) {
			numberOfIncomingTransitionsInAnActionState++;
		}
		checkRepeat();
	}

	public void checkRepeat() {
		if (numberOfIncomingTransitionsInAnActionState >= 2 && numberOfOutgoingTransitionsOfAnActionState >= 1 && !stateVertex.isEmpty()) {
			setNumberOfRepeats();
			stateVertex = "";
			numberOfIncomingTransitionsInAnActionState = 0;
			numberOfOutgoingTransitionsOfAnActionState = 0;
		}
	}

	public int getNumberOfUninterpretedActions() {
		return numberOfUninterpretedActions;
	}

	public void setNumberOfUninterpretedActions(String line) {
		if (line.contains("UML:UninterpretedAction xmi.id =")) {
			numberOfUninterpretedActions++;
		}
	}

	public int getNumberOfActionExpressions() {
		return numberOfActionExpressions;
	}

	public void setNumberOfActionExpressions(String line) {
		if (line.contains("UML:ActionExpression xmi.id =")) {
			numberOfActionExpressions++;
		}
	}

	//Transitionen
	public int getNumberOfTransitions() {
		return numberOfTransitions;
	}

	public void setNumberOfTransitions(String line) {
		if (line.contains("UML:Transition xmi.id =")) {
			numberOfTransitions++;
		}
	}

	//Schleifen
	public void setNumberOfRepeats() {
		numberOfRepeats++;
	}

	public int getNumberOfRepeats() {
		return numberOfRepeats;
	}

	//lesen der XMI Datei und abspeichern der Werte
	public void read(ActivityDiagramm activitydiagramm) throws IOException {

		File file = new File(activitydiagramm.getName());
		FileReader freader = new FileReader(file);
		BufferedReader reader = new BufferedReader(freader);

		numberOfFinalStates = 0;
		numberOfSignalEvents = 0;
		numberOfCompositeStates = 0;
		numberOfPseudoStates = 0;
		numberOfActionStates = 0;
		numberOfUninterpretedActions = 0;
		numberOfActionExpressions = 0;
		numberOfTransitions = 0;
		numberOfInitials = 0;
		numberOfForks = 0;
		numberOfJoins = 0;
		numberOfJunctions = 0;
		numberOfRepeats = 0;

		String line;
		while ((line = reader.readLine()) != null) {
			setNumberOfActionExpressions(line);
			setNumberOfActionStates(line);
			setNumberOfCompositeStates(line);
			setNumberOfFinalStates(line);
			setNumberOfPseudoStates(line);
			setNumberOfSignalEvents(line);
			setNumberOfTransitions(line);
			setNumberOfUninterpretedActions(line);
			setNumberOfForks(line);
			setNumberOfJunctions(line);
			setNumberOfInitials(line);
			setNumberOfJoins(line);
			setStateVertex(line);
			setIncomingAndOutgoingTransitionOfAnActionState(line);
			isNotAnActionStates(line);
		}
		freader.close();
	}
}
