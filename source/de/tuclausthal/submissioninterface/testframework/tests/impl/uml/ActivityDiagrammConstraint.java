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

package de.tuclausthal.submissioninterface.testframework.tests.impl.uml;

/**
 * Diese Klasse vergleicht die XMI Datei mit Aktivitätsdiagramminhalt
 * der Musterlösung mit der Studentenlösungen 
 * @author Joachim Schramm
 */
public class ActivityDiagrammConstraint {
	private ActivityDiagramm ad1;
	private ActivityDiagramm ad2;

	ActivityDiagrammConstraint(ActivityDiagramm ad1, ActivityDiagramm ad2) {
		if (ad1 == null || ad2 == null) {
			throw new NullPointerException();
		}
		this.ad1 = ad1;
		this.ad2 = ad2;
	}

	public String checkNumberOfStates() {
		if (ad1.getNumberOfActionStates() > ad2.getNumberOfActionStates()) {
			return "Achtung! Es fehlt/fehlen " + (ad1.getNumberOfActionStates() - ad2.getNumberOfActionStates()) + " Aktion(en)";
		} else if (ad1.getNumberOfActionStates() < ad2.getNumberOfActionStates()) {
			return "Es wurde(n) " + (ad2.getNumberOfActionStates() - ad1.getNumberOfActionStates()) + "  Aktion(en) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Aktionen aus dem ArgoUML Strukturbaum gelöscht werden.";
		} else {
			return "Anzahl Aktionen okay";
		}
	}

	public String checkNumberOfTransitions() {
		if (ad1.getNumberOfTransitions() > ad2.getNumberOfTransitions()) {
			return "Achtung! Es fehlt/fehlen " + (ad1.getNumberOfTransitions() - ad2.getNumberOfTransitions()) + " Transition(en)";
		} else if (ad1.getNumberOfTransitions() < ad2.getNumberOfTransitions()) {
			return "Es wurde(n) " + (ad2.getNumberOfTransitions() - ad1.getNumberOfTransitions()) + "  Transition(en) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Transitionen aus dem ArgoUML Strukturbaum gelöscht werden.";
		} else {
			return "Anzahl Transitionen okay";
		}
	}

	public String checkNumberOfInitials() {
		if (ad1.getNumberOfInitials() > ad2.getNumberOfInitials()) {
			return "Achtung! Es fehlt/fehlen " + (ad1.getNumberOfInitials() - ad2.getNumberOfInitials()) + " Startzustand/Startzustände";
		} else if (ad1.getNumberOfInitials() < ad2.getNumberOfInitials()) {
			return "Es wurde(n) " + (ad2.getNumberOfInitials() - ad1.getNumberOfInitials()) + "  Startzustand/Startzustände mehr modelliert als gefordert. Möglicherweise müssen überflüssige Startzustände aus dem ArgoUML Strukturbaum gelöscht werden.";
		} else {
			return "Anzahl Startzustände okay";
		}
	}

	public String checkNumberOfFinals() {
		if (ad1.getNumberOfFinalStates() > ad2.getNumberOfFinalStates()) {
			return "Achtung! Es fehlt/fehlen " + (ad1.getNumberOfFinalStates() - ad2.getNumberOfFinalStates()) + " Endzustand/Endzustände";
		} else if (ad1.getNumberOfFinalStates() < ad2.getNumberOfFinalStates()) {
			return "Es wurde(n) " + (ad2.getNumberOfFinalStates() - ad1.getNumberOfFinalStates()) + " Endzustand/Endzustände mehr modelliert als gefordert. Möglicherweise müssen überflüssige Endzustände aus dem ArgoUML Strukturbaum gelöscht werden.";
		} else {
			return "Anzahl Endzustände okay";
		}
	}

	public String checkNumberOfForks() {
		if (ad1.getNumberOfForks() > ad2.getNumberOfForks()) {
			return "Achtung! Es fehlt/fehlen " + (ad1.getNumberOfForks() - ad2.getNumberOfForks()) + " Gabelung(en)";
		} else if (ad1.getNumberOfForks() < ad2.getNumberOfForks()) {
			return "Es wurde(n) " + (ad2.getNumberOfForks() - ad1.getNumberOfForks()) + "  Gabelung(en) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Gabelungen aus dem ArgoUML Strukturbaum gelöscht werden.";
		} else {
			return "Anzahl Gabelungen okay";
		}
	}

	public String checkNumberOfJoins() {
		if (ad1.getNumberOfJoins() > ad2.getNumberOfJoins()) {
			return "Achtung! Es fehlt/fehlen " + (ad1.getNumberOfJoins() - ad2.getNumberOfJoins()) + " Vereinigung(en)";
		} else if (ad1.getNumberOfJoins() < ad2.getNumberOfJoins()) {
			return "Es wurde(n) " + (ad2.getNumberOfJoins() - ad1.getNumberOfJoins()) + "  Vereinigung(en) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Vereinigungen aus dem ArgoUML Strukturbaum gelöscht werden.";
		} else {
			return "Anzahl Vereinigungen okay";
		}
	}

	public String checkNumberOfJunctions() {
		if (ad1.getNumberOfJunctions() > ad2.getNumberOfJunctions()) {
			return "Achtung! Es fehlt/fehlen " + (ad1.getNumberOfJunctions() - ad2.getNumberOfJunctions()) + " Kreuzung(en)";
		} else if (ad1.getNumberOfJunctions() < ad2.getNumberOfJunctions()) {
			return "Es wurde(n) " + (ad2.getNumberOfJunctions() - ad1.getNumberOfJunctions()) + " Kreuzung(en) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Kreuzungen aus dem ArgoUML Strukturbaum gelöscht werden.";
		} else {
			return "Anzahl Kreuzungen okay";
		}
	}

	public String checkNumberOfSignalEvents() {
		if (ad1.getNumberOfSignalEvents() > ad2.getNumberOfSignalEvents()) {
			return "Achtung! Es fehlt/fehlen " + (ad1.getNumberOfSignalEvents() - ad2.getNumberOfSignalEvents()) + " Bedingung(en)";
		} else if (ad1.getNumberOfSignalEvents() < ad2.getNumberOfSignalEvents()) {
			return "Es wurde(n) " + (ad2.getNumberOfSignalEvents() - ad1.getNumberOfSignalEvents()) + " Bedingung(en) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Bedingungen aus dem ArgoUML Strukturbaum gelöscht werden.";
		} else {
			return "Anzahl Bedingungen okay";
		}
	}

	public String checkNumberOfRepeats() {
		if (ad1.getNumberOfRepeats() > ad2.getNumberOfRepeats()) {
			return "Achtung! Es fehlt/fehlen " + (ad1.getNumberOfRepeats() - ad2.getNumberOfRepeats()) + " Schleife(n)";
		} else if (ad1.getNumberOfRepeats() < ad2.getNumberOfRepeats()) {
			return "Es wurde(n) " + (ad2.getNumberOfRepeats() - ad1.getNumberOfRepeats()) + " Schleife(n) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Schleifen aus dem ArgoUML Strukturbaum gelöscht werden.";
		} else {
			return "Anzahl Schleifen okay";
		}
	}
}
