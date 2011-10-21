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
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.w3c.dom.Node;

/**
 * Diese Klasse liesst die XMI Datei mit Klassendiagramminhalt ein
 * und speichert die Elemente des Klassendiagramms
 */
public class ClassDiagramm extends UMLDiagramm {
	public static String TYPE = "UML:Class";

	public ClassDiagramm(File file, Node xmiContentNode) {
		super(file, xmiContentNode);
	}

	public String lastLineMethodOrAttribute = "";
	public String lastLineClassOrInterface = "";
	public String rootClass = "";
	public boolean isAsso = false;
	public String ID = "";

	private int numberOfClasses;
	private int numberOfAttributes;
	private int numberOfMethods;
	private int numberOfAssociation;
	private int numberOfComposites;
	private int numberOfAggregates;
	private int numberOfGeneralizations;
	private int numberOfAbstractions;
	private int numberOfInterfaces;

	private Vector<String> classes = new Vector<String>();
	private Vector<String> attributes = new Vector<String>();
	private Vector<String> methods = new Vector<String>();
	private Vector<String> associations = new Vector<String>();
	private Vector<String> interfaces = new Vector<String>();

	private Vector<String> allocationOfAssociations = new Vector<String>();

	private HashMap<String, Vector<String>> m2c = new HashMap<String, Vector<String>>();
	private HashMap<String, Vector<String>> a2c = new HashMap<String, Vector<String>>();
	private HashMap<String, Vector<String>> m2i = new HashMap<String, Vector<String>>();
	private String as2c;

	private HashMap<String, String> IDs = new HashMap<String, String>();

	public void setNumberOfClasses(String line) {
		if (line.contains("UML:Class xmi.id =")) {
			ID = line.substring(line.indexOf("'") + 1, line.length() - 1);
			lastLineClassOrInterface = "class";
			numberOfClasses++;
		}
	}

	public int getNumberOfClasses() {
		return numberOfClasses;
	}

	public void setIDs(String object, String id) {
		IDs.put(object, id);
	}

	public HashMap<String, String> getIDs() {
		return IDs;
	}

	public void setNumberOfInterfaces(String line) {
		if (line.contains("UML:Interface xmi.id =")) {
			ID = line.substring(line.indexOf("'") + 1, line.length() - 1);
			lastLineClassOrInterface = "interface";
			numberOfInterfaces++;
		}
	}

	public int getNumberOfInterfaces() {
		return numberOfInterfaces;
	}

	public void setNumberOfAttributes(String line) {
		if (line.contains("UML:Attribute xmi.id =")) {
			lastLineMethodOrAttribute = "attribute";
			numberOfAttributes++;
		}
	}

	public int getNumberOfAttributes() {
		return numberOfAttributes;
	}

	public void setNumberOfMethods(String line) {
		if (line.contains("UML:Operation xmi.id = ")) {
			lastLineMethodOrAttribute = "method";
			numberOfMethods++;
		}
	}

	public int getNumberOfMethods() {
		return numberOfMethods;
	}

	public void setNumberOfAssociations(String line) {
		if (line.contains("UML:Association xmi.id = ")) {
			isAsso = true;
			numberOfAssociation++;
		}
	}

	public int getNumberOfAssociations() {
		return numberOfAssociation;

	}

	public void setNumberOfAggregates(String line) {
		if (line.contains("aggregation = 'aggregate'")) {
			numberOfAggregates++;
		}
	}

	public int getNumberOfAggregates() {
		return numberOfAggregates;

	}

	public void setNumberOfComposites(String line) {
		if (line.contains("aggregation = 'composite'")) {
			numberOfComposites++;
		}
	}

	public int getNumberOfComposites() {
		return numberOfComposites;

	}

	public void setNumberOfGeneralizations(String line) {
		if (line.contains("UML:Generalization xmi.id =")) {
			numberOfGeneralizations++;
		}
	}

	public int getNumberOfGeneralizations() {
		return numberOfGeneralizations;
	}

	public void setNumberAbstractions(String line) {
		if (line.contains("UML:Abstraction xmi.id =")) {
			numberOfAbstractions++;
		}
	}

	public int getNumberOfAbstractions() {
		return numberOfAbstractions;
	}

	public void setClassNames(String line) {
		if (line.contains("name") && line.contains("visibility") && !line.contains("ownerScope") && line.contains("isSpecification") && lastLineClassOrInterface.equals("class")) {
			rootClass = line.substring(line.indexOf("'") + 1, line.indexOf("' visibility"));
			classes.add(rootClass);
			setIDs(ID, rootClass);
		}
	}

	public Vector<String> getClassNames() {
		return classes;
	}

	public void setInterfaceNames(String line) {
		if (line.contains("name") && line.contains("visibility") && !line.contains("ownerScope") && line.contains("isSpecification") && lastLineClassOrInterface.equals("interface")) {
			rootClass = line.substring(line.indexOf("'") + 1, line.indexOf("' visibility"));
			interfaces.add(rootClass);
			setIDs(ID, rootClass);
		}
	}

	public Vector<String> getInterfaceNames() {
		return interfaces;
	}

	public void setAttributeNames(String line) {
		if (line.contains("name") && line.contains("visibility") && line.contains("isSpecification") && line.contains("ownerScope") && lastLineMethodOrAttribute.equals("attribute")) {
			line = line.substring(line.indexOf("'") + 1, line.indexOf("' visibility"));
			attributes.add(line);
			allocateAttribute(line);
		}
	}

	public Vector<String> getAttributeNames() {
		return attributes;
	}

	public void setMethodsNames(String line) {
		if (line.contains("name") && line.contains("visibility") && line.contains("isSpecification") && line.contains("ownerScope") && lastLineMethodOrAttribute.equals("method")) {
			line = line.substring(line.indexOf("'") + 1, line.indexOf("' visibility"));
			methods.add(line);
			allocateMethod(line);
		}
	}

	public Vector<String> getMethodsNames() {
		return methods;
	}

	public void setAssociationNames(String line) {
		if (line.contains("name") && line.contains("isLeaf") && line.contains("isSpecification") && line.contains("isRoot") && isAsso) {
			line = line.substring(line.indexOf("'") + 1, line.indexOf("' isSpecification"));
			associations.add(line);
		}
	}

	public Vector<String> getAssociationNames() {
		return associations;
	}

	public void allocateAttribute(String attribute) {
		if (a2c.containsKey(rootClass)) {
			Vector<String> otherattr = a2c.get(rootClass);
			otherattr.add(attribute);
			Collections.sort(otherattr);
			a2c.put(rootClass, otherattr);
		} else {
			Vector<String> attributes = new Vector<String>();
			attributes.add(attribute);
			a2c.put(rootClass, attributes);
		}
	}

	public void allocateMethod(String method) {
		if (lastLineClassOrInterface.equals("class")) {
			if (m2c.containsKey(rootClass)) {
				Vector<String> otherMethods = m2c.get(rootClass);
				otherMethods.add(method);
				Collections.sort(otherMethods);
				m2c.put(rootClass, otherMethods);
			} else {
				Vector<String> methods = new Vector<String>();
				methods.add(method);
				m2c.put(rootClass, methods);

			}
		} else if (lastLineClassOrInterface.equals("interface")) {
			if (m2i.containsKey(rootClass)) {
				Vector<String> otherMethods = m2i.get(rootClass);
				otherMethods.add(method);
				Collections.sort(otherMethods);
				m2i.put(rootClass, otherMethods);
			} else {
				Vector<String> methods = new Vector<String>();
				methods.add(method);
				m2i.put(rootClass, methods);

			}
		} else {

		}
	}

	public void allocateAssociation(String line) {
		if (isAsso && line.contains("UML:Class xmi.idref =") || line.contains("UML:Interface xmi.idref =")) {
			String endID = line.substring(line.indexOf("'") + 1, line.length() - 3);
			allocationOfAssociations.add(IDs.get(endID));
		}
	}

	public Vector<String> getAllocationOfAssociation() {
		return allocationOfAssociations;
	}

	public HashMap<String, Vector<String>> getM2C() {
		return m2c;
	}

	public HashMap<String, Vector<String>> getA2C() {
		return a2c;
	}

	public HashMap<String, Vector<String>> getM2I() {
		return m2i;
	}

	public String getAs2C() {
		return as2c;
	}

	public void setAs2C() {
		for (int i = 0; i < allocationOfAssociations.size() - 1; i = i + 2) {
			as2c = as2c + " " + allocationOfAssociations.get(i) + " verbunden mit " + allocationOfAssociations.get(i + 1) + ",";
		}
	}

	//lesen der XMI Datei und abspeichern der Werte
	public void read(ClassDiagramm classdiagramm) throws IOException {

		File file = new File(classdiagramm.getName());
		FileReader freader = new FileReader(file);
		BufferedReader reader = new BufferedReader(freader);

		numberOfClasses = 0;
		numberOfAttributes = 0;
		numberOfMethods = 0;
		numberOfAssociation = 0;
		numberOfGeneralizations = 0;
		numberOfComposites = 0;
		numberOfAggregates = 0;
		numberOfInterfaces = 0;
		numberOfAbstractions = 0;

		classes.clear();
		attributes.clear();
		methods.clear();
		associations.clear();
		interfaces.clear();
		a2c.clear();
		m2c.clear();
		m2i.clear();
		as2c = "";

		String line;
		while ((line = reader.readLine()) != null) {
			setNumberOfClasses(line);
			setClassNames(line);
			setNumberOfAttributes(line);
			setAttributeNames(line);
			setNumberOfMethods(line);
			setMethodsNames(line);
			setNumberOfAssociations(line);
			setAssociationNames(line);
			setNumberOfAggregates(line);
			setNumberOfComposites(line);
			setNumberOfGeneralizations(line);
			setNumberAbstractions(line);
			setNumberOfInterfaces(line);
			setInterfaceNames(line);
			allocateAssociation(line);
		}
		setAs2C();
		freader.close();
	}
}
