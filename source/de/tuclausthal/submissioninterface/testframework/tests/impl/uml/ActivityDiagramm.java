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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Diese Klasse liesst die XMI Datei mit Aktivitätsdiagramminhalt ein
 * und speichert die Elemente des Aktivitätsdiagramm
 * @author Joachim Schramm
 */
public class ActivityDiagramm extends UMLDiagramm {
	public static String TYPE = "UML:ActivityGraph";

	private int numberOfFinalStates = 0;
	private int numberOfSignalEvents = 0;
	private int numberOfCompositeStates = 0;
	private int numberOfActionStates = 0;
	private int numberOfUninterpretedActions = 0;
	private int numberOfActionExpressions = 0;
	private int numberOfTransitions = 0;

	private int numberOfPseudoStates = 0;
	private int numberOfInitials = 0;
	private int numberOfForks = 0;
	private int numberOfJoins = 0;
	private int numberOfJunctions = 0;
	private int numberOfRepeats = 0;

	public ActivityDiagramm(File file, Node xmiContentNode) {
		super(file, xmiContentNode);
		parse();
	}

	//Startzustaende
	public int getNumberOfInitials() {
		return numberOfInitials;
	}

	//Gabelungen
	public int getNumberOfForks() {
		return numberOfForks;
	}

	//Vereinigungen
	public int getNumberOfJoins() {
		return numberOfJoins;
	}

	//Kreuzungen
	public int getNumberOfJunctions() {
		return numberOfJunctions;
	}

	//Endzustände
	public int getNumberOfFinalStates() {
		return numberOfFinalStates;
	}

	//Kreuzungsbedingungen
	public int getNumberOfSignalEvents() {
		return numberOfSignalEvents;
	}

	private void setNumberOfSignalEvents(Node node) {
		numberOfSignalEvents = countNodes(node, "UML:SignalEvent");
	}

	public int getNumberOfCompositeStates() {
		return numberOfCompositeStates;
	}

	public int getNumberOfPseudoStates() {
		return numberOfPseudoStates;
	}

	//Aktivitaet
	public int getNumberOfActionStates() {
		return numberOfActionStates;
	}

	public int getNumberOfUninterpretedActions() {
		return numberOfUninterpretedActions;
	}

	private void setNumberOfUninterpretedActions(Node node) {
		numberOfUninterpretedActions = countNodes(node, "UML:UninterpretedAction");
	}

	public int getNumberOfActionExpressions() {
		return numberOfActionExpressions;
	}

	private void setNumberOfActionExpressions(Node node) {
		numberOfActionExpressions = countNodes(node, "UML:ActionExpression");
	}

	//Transitionen
	public int getNumberOfTransitions() {
		return numberOfTransitions;
	}

	private void setNumberOfTransitions(Node node) {
		numberOfTransitions = countNodes(node, "UML:Transition");
	}

	public int getNumberOfRepeats() {
		return numberOfRepeats;
	}

	//lesen der XMI Datei und abspeichern der Werte
	private void parse() {
		Node xmiContentNode = getXmiContentNode();
		NodeList childNodes = xmiContentNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			parseTOP(node);
		}

		setNumberOfTransitions(xmiContentNode);
		setNumberOfActionExpressions(xmiContentNode);
		setNumberOfSignalEvents(xmiContentNode);
		setNumberOfUninterpretedActions(xmiContentNode);
	}

	private void parseTOP(Node node) {
		if ("UML:StateMachine.top".equals(node.getNodeName())) {
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				parseCompositeState(node.getChildNodes().item(i));
			}
		}
	}

	private void parseCompositeState(Node node) {
		if ("UML:CompositeState".equals(node.getNodeName())) {
			numberOfCompositeStates++;
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				parseCompositeStateSubVertex(node.getChildNodes().item(i));
			}
		}
	}

	private void parseCompositeStateSubVertex(Node node) {
		if ("UML:CompositeState.subvertex".equals(node.getNodeName())) {
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				parsePseudoState(node.getChildNodes().item(i));
				parseFinalState(node.getChildNodes().item(i));
				parseActionState(node.getChildNodes().item(i));
			}
		}
	}

	private void parseActionState(Node node) {
		if ("UML:ActionState".equals(node.getNodeName())) {
			numberOfActionStates++;

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();

			xpath.setNamespaceContext(new UMLNameSpaceContext());

			XPathExpression expr;
			int numberOfIncomingTransitions = 0;
			int numberOfOutgoingTransitions = 0;
			try {
				expr = xpath.compile("count(UML:StateVertex.incoming/UML:Transition)");
				numberOfIncomingTransitions = ((Double) expr.evaluate(node, XPathConstants.NUMBER)).intValue();
				expr = xpath.compile("count(UML:StateVertex.outgoing/UML:Transition)");
				numberOfOutgoingTransitions = ((Double) expr.evaluate(node, XPathConstants.NUMBER)).intValue();
			} catch (XPathExpressionException e) {
			}

			if (numberOfIncomingTransitions >= 2 && numberOfOutgoingTransitions >= 1) {
				numberOfRepeats++;
			}
		}
	}

	private void parsePseudoState(Node node) {
		if ("UML:Pseudostate".equals(node.getNodeName())) {
			numberOfPseudoStates++;
			Node kind = node.getAttributes().getNamedItem("kind");
			if ("initial".equals(kind.getNodeValue())) {
				numberOfInitials++;
			} else if ("join".equals(kind.getNodeValue())) {
				numberOfJoins++;
			} else if ("fork".equals(kind.getNodeValue())) {
				numberOfForks++;
			} else if ("junction".equals(kind.getNodeValue())) {
				numberOfJunctions++;
			}
		}
	}

	private void parseFinalState(Node node) {
		if ("UML:FinalState".equals(node.getNodeName())) {
			numberOfFinalStates++;
		}
	}

	private int countNodes(Node node, String nodeName) {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		xpath.setNamespaceContext(new UMLNameSpaceContext());
		XPathExpression expr;

		try {
			expr = xpath.compile("count(//" + nodeName + "[xmi.id])");
			return ((Double) expr.evaluate(node, XPathConstants.NUMBER)).intValue();
		} catch (XPathExpressionException e) {
		}

		return 0;
	}

	@Override
	public String getType() {
		return TYPE;
	}
}
