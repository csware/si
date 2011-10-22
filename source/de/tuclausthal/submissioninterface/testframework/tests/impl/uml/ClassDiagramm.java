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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Diese Klasse liesst die XMI Datei mit Klassendiagramminhalt ein
 * und speichert die Elemente des Klassendiagramms
 * @author Joachim Schramm
 * @author Sven Strickroth
 */
public class ClassDiagramm extends UMLDiagramm {
	public static String TYPE = "UML:Class";

	public ClassDiagramm(File file, Node xmiContentNode) {
		super(file, xmiContentNode);
		parse();
	}

	private int numberOfClasses = 0;
	private int numberOfAttributes = 0;
	private int numberOfMethods = 0;
	private int numberOfAssociation = 0;
	private int numberOfComposites = 0;
	private int numberOfAggregates = 0;
	private int numberOfGeneralizations = 0;
	private int numberOfAbstractions = 0;
	private int numberOfInterfaces = 0;

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

	public int getNumberOfClasses() {
		return numberOfClasses;
	}

	public void setIDs(String object, String id) {
		IDs.put(object, id);
	}

	public HashMap<String, String> getIDs() {
		return IDs;
	}

	public int getNumberOfInterfaces() {
		return numberOfInterfaces;
	}

	public int getNumberOfAttributes() {
		return numberOfAttributes;
	}

	public int getNumberOfMethods() {
		return numberOfMethods;
	}

	public int getNumberOfAssociations() {
		return numberOfAssociation;

	}

	public int getNumberOfAggregates() {
		return numberOfAggregates;

	}

	public int getNumberOfComposites() {
		return numberOfComposites;

	}

	public int getNumberOfGeneralizations() {
		return numberOfGeneralizations;
	}

	public int getNumberOfAbstractions() {
		return numberOfAbstractions;
	}

	public Vector<String> getClassNames() {
		return classes;
	}

	public Vector<String> getInterfaceNames() {
		return interfaces;
	}

	public Vector<String> getAttributeNames() {
		return attributes;
	}

	public Vector<String> getMethodsNames() {
		return methods;
	}

	public Vector<String> getAssociationNames() {
		return associations;
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
	private void parse() {
		Node xmiContentNode = getXmiContentNode();
		NodeList childNodes = xmiContentNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			parseClass(node);
			parseInterface(node);
			parseAssociation(node);
			parseGeneralization(node);
			parseAbstraction(node);
		}
		setAs2C();
	}

	private void parseAbstraction(Node node) {
		if ("UML:Abstraction".equals(node.getNodeName())) {
			numberOfAbstractions++;
		}
	}

	private void parseGeneralization(Node node) {
		if ("UML:Generalization".equals(node.getNodeName())) {
			numberOfGeneralizations++;
		}
	}

	private void parseAssociation(Node node) {
		if ("UML:Association".equals(node.getNodeName())) {
			numberOfAssociation++;
			Node name = node.getAttributes().getNamedItem("name");
			if (!name.getNodeValue().isEmpty()) {
				associations.add(name.getNodeValue());
			}

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			xpath.setNamespaceContext(new UMLNameSpaceContext());

			XPathExpression expr;
			NodeList associationEnds = null;
			try {
				expr = xpath.compile("*/UML:AssociationEnd");
				associationEnds = (NodeList) expr.evaluate(node, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
			}
			if (associationEnds != null) {
				for (int i = 0; i < associationEnds.getLength(); i++) {
					Node accociationEndNode = associationEnds.item(i);
					Node aggregationType = accociationEndNode.getAttributes().getNamedItem("aggregation");
					if ("aggregate".equals(aggregationType)) {
						numberOfAggregates++;
					} else if ("composite".equals(aggregationType)) {
						numberOfComposites++;
					}
					NodeList accociationEndParticipants = null;
					try {
						expr = xpath.compile("*/UML:AssociationEnd.participant/node()");
						accociationEndParticipants = (NodeList) expr.evaluate(accociationEndNode, XPathConstants.NODESET);
					} catch (XPathExpressionException e) {
					}
					for (int j = 0; accociationEndParticipants != null && j < accociationEndParticipants.getLength(); j++) {
						Node participationNode = accociationEndParticipants.item(j);
						allocationOfAssociations.add(IDs.get(participationNode.getAttributes().getNamedItem("xmi.idref").getNodeValue()));
					}
				}
			}
		}
	}

	private void parseClass(Node node) {
		if ("UML:Class".equals(node.getNodeName())) {
			numberOfClasses++;
			String ID = node.getAttributes().getNamedItem("xmi.id").getNodeValue();
			Node name = node.getAttributes().getNamedItem("name");
			if (!name.getNodeValue().isEmpty()) {
				classes.add(name.getNodeValue());
				setIDs(ID, name.getNodeValue());
			}
			AttributesMethods attributesMethods = extractAttributesMethods(node);
			if (!name.getNodeValue().isEmpty()) {
				a2c.put(name.getNodeValue(), new Vector<String>(attributesMethods.attributes));
				m2c.put(name.getNodeValue(), new Vector<String>(attributesMethods.methods));
			}
		}
	}

	private void parseInterface(Node node) {
		if ("UML:Interface".equals(node.getNodeName())) {
			numberOfInterfaces++;
			String ID = node.getAttributes().getNamedItem("xmi.id").getNodeValue();
			Node name = node.getAttributes().getNamedItem("name");
			if (!name.getNodeValue().isEmpty()) {
				interfaces.add(name.getNodeValue());
				setIDs(ID, name.getNodeValue());
			}
			AttributesMethods attributesMethods = extractAttributesMethods(node);
			if (!name.getNodeValue().isEmpty()) {
				m2i.put(name.getNodeValue(), new Vector<String>(attributesMethods.methods));
			}
		}
	}

	private AttributesMethods extractAttributesMethods(Node rootNode) {
		AttributesMethods result = new AttributesMethods();

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		xpath.setNamespaceContext(new UMLNameSpaceContext());

		XPathExpression expr;
		NodeList attributes = null;
		NodeList methods = null;
		try {
			expr = xpath.compile("*/UML:Attribute");
			attributes = (NodeList) expr.evaluate(rootNode, XPathConstants.NODESET);
			expr = xpath.compile("*/UML:Operation");
			methods = (NodeList) expr.evaluate(rootNode, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
		}

		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				Node node = attributes.item(i);
				numberOfAttributes++;
				Node name = node.getAttributes().getNamedItem("name");
				if (!name.getNodeValue().isEmpty()) {
					result.attributes.add(name.getNodeValue());
				}
			}
			Collections.sort(result.attributes);
		}
		if (methods != null) {
			for (int i = 0; i < methods.getLength(); i++) {
				Node node = methods.item(i);
				numberOfMethods++;
				Node name = node.getAttributes().getNamedItem("name");
				if (!name.getNodeValue().isEmpty()) {
					result.methods.add(name.getNodeValue());
				}
			}
			Collections.sort(result.methods);
		}

		return result;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	private static class AttributesMethods {
		public List<String> attributes = new LinkedList<String>();
		public List<String> methods = new LinkedList<String>();
	}

	public ClassDiagrammConstraint compare(ClassDiagramm diagramm) {
		return new ClassDiagrammConstraint(this, diagramm);
	}

	@Override
	public String compareTextResultInternal(UMLDiagramm diagramm) {
		ClassDiagrammConstraint cdc = compare((ClassDiagramm) diagramm);
		String output = "";

		output = output + cdc.checkNumberOfClassesPlusAttributes() + "\n";
		output = output + cdc.checkNumberOfInterfaces() + "\n";
		output = output + cdc.checkNumberOfMethods() + "\n";
		output = output + cdc.checkNumberOfAssociations() + "\n";
		output = output + cdc.checkNumberOfGeneralizations() + "\n";
		output = output + cdc.checkNumberOfAbstractions() + "\n";
		output = output + cdc.checkNumberOfAggregates() + "\n";
		output = output + cdc.checkNumberOfComposites() + "\n";

		output = output + cdc.checkNamesOfClassesAndAttributes() + "\n";
		output = output + cdc.checkNamesOfMethods() + "\n";
		output = output + cdc.checkNamesOfInterfaces() + "\n";

		output = output + cdc.checkA2C() + "\n";
		output = output + cdc.checkM2C() + "\n";
		output = output + cdc.checkM2I() + "\n";

		output = output + cdc.checkPairsOfAssociations() + "\n";

		return output;
	}

	@Override
	public String toString() {
		String output = "";
		output = output + "Klassen: " + getNumberOfClasses() + "\n";
		output = output + getClassNames() + "\n";
		output = output + "Schnittstellen: " + getNumberOfInterfaces() + "\n";
		output = output + getInterfaceNames() + "\n";
		output = output + "Attribute: " + getNumberOfAttributes() + "\n";
		output = output + getAttributeNames() + "\n";
		output = output + "Methoden: " + getNumberOfMethods() + "\n";
		output = output + getMethodsNames() + "\n";
		output = output + "Assoziationen: " + getNumberOfAssociations() + "\n";
		output = output + getAssociationNames() + "\n";
		output = output + "Aggregationen: " + getNumberOfAggregates() + "\n";
		output = output + "Kompositionen: " + getNumberOfComposites() + "\n";
		output = output + "Generalisierungen: " + getNumberOfGeneralizations() + "\n";
		output = output + "Schnittstellenrealisierungen: " + getNumberOfAbstractions() + "\n";

		output = output + "Zuordnung von Methoden zu Klassen: " + getM2C() + "\n";
		output = output + "Zuordnung von Attributen zu Klassen: " + getA2C() + "\n";
		output = output + "Zuordnung von Methoden zu Interfaces: " + getM2I() + "\n";
		output = output + "Zuordnung von Assoziationen zu Klassen: " + getAs2C() + "\n";
		//output = output + getAllocationOfAssociation()+"\n";
		return output;
	}
}
