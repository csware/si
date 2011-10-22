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

import java.util.HashMap;
import java.util.Vector;

/**
 * Diese Klasse vergleicht die XMI Datei mit Klassendiagramminhalt
 * der Musterlösung mit der Studentenlösungen 
 */
public class ClassDiagrammConstraint {
	private ClassDiagramm cd1;
	private ClassDiagramm cd2;

	ClassDiagrammConstraint(ClassDiagramm cd1, ClassDiagramm cd2) {
		if (cd1 == null || cd2 == null) {
			throw new NullPointerException();
		}
		this.cd1 = cd1;
		this.cd2 = cd2;
	}

	public String checkNumberOfClassesPlusAttributes() {
		if (cd1.getNumberOfClasses() + cd1.getNumberOfAttributes() > cd2.getNumberOfClasses() + cd2.getNumberOfAttributes()) {
			return "Achtung! Es fehlt/fehlen " + (cd1.getNumberOfClasses() + cd1.getNumberOfAttributes() - cd2.getNumberOfClasses() - cd2.getNumberOfAttributes()) + " Klasse(n) oder Attribut(e)";
		} else if (cd1.getNumberOfClasses() + cd1.getNumberOfAttributes() < cd2.getNumberOfClasses() + cd2.getNumberOfAttributes()) {
			return "Es wurde(n) " + (cd2.getNumberOfClasses() + cd2.getNumberOfAttributes() - cd1.getNumberOfClasses() - cd1.getNumberOfAttributes()) + " Klasse(n) und/oder Attribut(e) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Klassen oder Attribute aus dem ArgoUML Strukturbaum gelöscht werden.";
		} else {
			if (cd1.getNumberOfClasses() == cd2.getNumberOfClasses()) {
				return "Anzahl der Klassen und Attribute okay";
			} else {
				return "Die Anzahl der Klassen und Attribute ist zufriedenstellend, aber es gibt eine bessere Modellierung.";
			}
		}
	}

	public String checkNumberOfInterfaces() {
		if (cd1.getNumberOfInterfaces() > cd2.getNumberOfInterfaces()) {
			return "Achtung! Es fehlt/fehlen " + (cd1.getNumberOfInterfaces() - cd2.getNumberOfInterfaces()) + " Schnittstelle(n)";
		} else if (cd1.getNumberOfInterfaces() == cd2.getNumberOfInterfaces()) {
			return "Anzahl Schnittstellen okay";
		} else {
			return "Es wurde(n) " + (cd2.getNumberOfInterfaces() - cd1.getNumberOfInterfaces()) + " Schnittstelle(n) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Schnittstellen aus dem ArgoUML Strukturbaum gelöscht werden.";
		}
	}

	public String checkNumberOfMethods() {
		if (cd1.getNumberOfMethods() > cd2.getNumberOfMethods()) {
			return "Achtung! Es fehlt/fehlen " + (cd1.getNumberOfMethods() - cd2.getNumberOfMethods()) + " Methode(n)";
		} else if (cd1.getNumberOfMethods() == cd2.getNumberOfMethods()) {
			return "Anzahl Methoden okay";
		} else {
			return "Es wurde(n) " + (cd2.getNumberOfMethods() - cd1.getNumberOfMethods()) + " Methode(n) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Methoden aus dem ArgoUML Strukturbaum gelöscht werden.";
		}
	}

	public String checkNumberOfAssociations() {
		if (cd2.getNumberOfAttributes() > cd1.getNumberOfAttributes()) {
			int difference = cd2.getNumberOfAttributes() - cd1.getNumberOfAttributes();
			if ((cd1.getNumberOfAssociations() - difference) == cd2.getNumberOfAssociations()) {
				return "Anzahl der Assoziationen zufriedenstellend, aber es gibt eine bessere Modellierung.";
			} else if ((cd1.getNumberOfAssociations() - difference) > cd2.getNumberOfAssociations()) {
				return "Achtung! Es fehlen Assoziationen";
			} else {
				return "Es wurden mehr Assoziationen modelliert als gefordert. Möglicherweise müssen überflüssige Assoziationen aus dem ArgoUML Strukturbaum gelöscht werden.";
			}
		} else if (cd2.getNumberOfAttributes() == cd1.getNumberOfAttributes()) {
			if ((cd1.getNumberOfAssociations()) == cd2.getNumberOfAssociations()) {
				return "Anzahl der Assoziationen okay.";
			} else if ((cd1.getNumberOfAssociations()) > cd2.getNumberOfAssociations()) {
				return "Achtung! Es fehlt/fehlen " + (cd1.getNumberOfAssociations() - cd2.getNumberOfAssociations()) + " Assoziation(en)";
			} else {
				return "Es wurde(n) " + (cd2.getNumberOfAssociations() - cd1.getNumberOfAssociations()) + " Assoziation(en) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Assoziationen aus dem ArgoUML Strukturbaum gelöscht werden.";
			}
		} else {
			if ((cd1.getNumberOfAssociations()) > cd2.getNumberOfAssociations()) {
				return "Achtung es fehlen Assoziationen.";
			} else {
				return "";
			}

		}
	}

	public String checkNumberOfGeneralizations() {
		if (cd1.getNumberOfGeneralizations() > cd2.getNumberOfGeneralizations()) {
			return "Achtung! Es fehlt/fehlen " + (cd1.getNumberOfGeneralizations() - cd2.getNumberOfGeneralizations()) + " Vererbungsbeziehung(en)";
		} else if (cd1.getNumberOfGeneralizations() == cd2.getNumberOfGeneralizations()) {
			return "Anzahl Generalisierungen okay";
		} else {
			return "Es wurde(n) " + (cd2.getNumberOfGeneralizations() - cd1.getNumberOfGeneralizations()) + " Vererbungsbeziehung(en) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Methoden aus dem ArgoUML Strukturbaum gelöscht werden.";
		}
	}

	public String checkNumberOfAbstractions() {
		if (cd1.getNumberOfAbstractions() > cd2.getNumberOfAbstractions()) {
			return "Achtung! Es fehlt/fehlen " + (cd1.getNumberOfAbstractions() - cd2.getNumberOfAbstractions()) + " Schnittstellenrealisierung(en)";
		} else if (cd1.getNumberOfAbstractions() == cd2.getNumberOfAbstractions()) {
			return "Anzahl Schnittstellenrealisierungen okay";
		} else {
			return "Es wurde(n) " + (cd2.getNumberOfAbstractions() - cd1.getNumberOfAbstractions()) + "  Schnittstellenrealisierung(en) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Schnittstellenrealisierungen aus dem ArgoUML Strukturbaum gelöscht werden.";
		}
	}

	public String checkNumberOfAggregates() {
		if (cd1.getNumberOfAggregates() > cd2.getNumberOfAggregates()) {
			return "Achtung! Es fehlt/fehlen " + (cd1.getNumberOfAggregates() - cd2.getNumberOfAggregates()) + " Aggregation(en)";
		} else if (cd1.getNumberOfAggregates() == cd2.getNumberOfAggregates()) {
			return "Anzahl Aggregationen okay";
		} else {
			return "Es wurde(n) " + (cd2.getNumberOfAggregates() - cd1.getNumberOfAggregates()) + " Aggregation(en) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Aggregationen aus dem ArgoUML Strukturbaum gelöscht werden.";
		}
	}

	public String checkNumberOfComposites() {
		if (cd1.getNumberOfComposites() > cd2.getNumberOfComposites()) {
			return "Achtung! Es fehlt/fehlen " + (cd1.getNumberOfComposites() - cd2.getNumberOfComposites()) + " Komposition(en)";
		} else if (cd1.getNumberOfComposites() == cd2.getNumberOfComposites()) {
			return "Anzahl Kompositionen okay";
		} else {
			return "Es wurde(n) " + (cd2.getNumberOfComposites() - cd1.getNumberOfComposites()) + " Komposition(en) mehr modelliert als gefordert. Möglicherweise müssen überflüssige Kompositionen aus dem ArgoUML Strukturbaum gelöscht werden.";
		}
	}

	public String checkNamesOfClassesAndAttributes() {
		Vector<String> result1 = new Vector<String>();
		Vector<String> result2 = new Vector<String>();
		for (int i = 0; i < cd1.getClassNames().size(); i++) {
			result1.add(cd1.getClassNames().elementAt(i));
		}
		for (int i = 0; i < cd1.getAttributeNames().size(); i++) {
			result1.add(cd1.getAttributeNames().elementAt(i));
		}
		for (int i = 0; i < cd2.getClassNames().size(); i++) {
			result2.add(cd2.getClassNames().elementAt(i));
		}
		for (int i = 0; i < cd2.getAttributeNames().size(); i++) {
			result2.add(cd2.getAttributeNames().elementAt(i));
		}
		if (result2.containsAll(result1)) {
			return "Klassennamen und Attributsnamen okay";
		} else {
			return "Überprüfe die Namen der Klassen und Attribute deiner Lösung noch einmal mit der in der Aufgabenstellung.";
		}
	}

	public String checkNamesOfMethods() {

		if (cd2.getMethodsNames().containsAll(cd1.getMethodsNames())) {
			return "Methodennamen okay";
		} else {

			return "Überprüfe die Namen der Methoden deiner Lösung noch einmal mit der in der Aufgabenstellung.";
		}
	}

	public String checkNamesOfInterfaces() {

		if (cd2.getInterfaceNames().containsAll(cd1.getInterfaceNames())) {
			return "Schnittstellennamen okay";
		} else {

			return "Überprüfe die Namen der Schnittstellen deiner Lösung noch einmal mit der in der Aufgabenstellung.";
		}
	}

	public String checkM2C() {

		if (cd1.getM2C().equals(cd2.getM2C())) {
			return "Zuordnung von Methoden zu Klassen okay";
		} else {

			return "Überprüfe die Zuordnung deiner Methoden zu Klassen in deiner Lösung.";
		}
	}

	public String checkA2C() {
		if (cd1.getNumberOfAttributes() == cd2.getNumberOfAttributes() && cd1.getNumberOfClasses() == cd2.getNumberOfClasses()) {
			if (cd1.getA2C().equals(cd2.getA2C())) {
				return "Zuordnung von Attributen zu Klassen okay";
			} else {
				return "Überprüfe die Zuordnung deiner Attribute zu Klassen in deiner Lösung.";
			}
		} else {
			return "";
		}
	}

	public String checkM2I() {
		if (cd1.getNumberOfMethods() == cd2.getNumberOfMethods() && cd1.getNumberOfInterfaces() == cd2.getNumberOfInterfaces()) {
			if (cd1.getM2I().equals(cd2.getM2I())) {
				return "Zuordnung von Methoden zu Interfaces okay";
			} else {
				return "Überprüfe die Zuordnung deiner Methoden zu Interfaces in deiner Lösung.";
			}
		} else {
			return "";
		}
	}

	public String checkPairsOfAssociations() {
		if (cd1.getNumberOfAbstractions() == cd2.getNumberOfAbstractions() && checkNumberOfAssociations().equals("Anzahl der Assoziationen okay.")) {
			Vector<String> v1 = cd1.getAllocationOfAssociation();
			Vector<String> v2 = cd2.getAllocationOfAssociation();
			boolean b = true;

			HashMap<Integer, String> h1 = new HashMap<Integer, String>();
			HashMap<Integer, String> h2a = new HashMap<Integer, String>();
			HashMap<Integer, String> h2b = new HashMap<Integer, String>();

			for (int i = 0; i < v1.size(); i = i + 2) {
				h1.put(i, v1.get(i) + " " + v1.get(i + 1));
			}

			for (int i = 0; i < v2.size(); i = i + 2) {
				h2a.put(i, v2.get(i) + " " + v2.get(i + 1));
				h2b.put(i, v2.get(i + 1) + " " + v2.get(i));
			}

			for (int i = 0; i <= cd1.getNumberOfAssociations() * 2 - 2; i = i + 2) {
				if (h2a.containsValue(h1.get(i)) || h2b.containsValue(h1.get(i))) {

				} else {
					b = false;
				}

			}

			if (b) {
				return "Zuordnung der Assoziationen okay";
			} else {
				return "Überprüfe die Zuordnung deiner Assoziationen in deiner Lösung.";
			}
		} else {
			return "";
		}
	}
}
