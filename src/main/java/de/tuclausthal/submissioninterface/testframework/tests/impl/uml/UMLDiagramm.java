/*
 * Copyright 2011 Joachim Schramm
 * Copyright 2011, 2017, 2021 Sven Strickroth <email@cs-ware.de>
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
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Diese Klasse checkt die XMI Datei hinsichtlich der Diagrammart
 * @author Joachim Schramm
 * @author Sven Strickroth
 */
public abstract class UMLDiagramm {

	private String name;
	private Node xmiContentNode;

	public UMLDiagramm(String name) {
		super();
		this.name = name;
	}

	public UMLDiagramm(File file, Node xmiContentNode) {
		this.name = file.getAbsolutePath();
		this.xmiContentNode = xmiContentNode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Node getXmiContentNode() {
		return xmiContentNode;
	}

	public boolean isSameType(UMLDiagramm diagramm) {
		return getType().equals(diagramm.getType());
	}

	public String compareTextResult(UMLDiagramm diagramm) {
		if (!isSameType(diagramm)) {
			return null;
		}
		return compareTextResultInternal(diagramm);
	}

	protected abstract String compareTextResultInternal(UMLDiagramm diagramm);

	abstract public String getType();

	//lesen der XMI Datei und einordnen der Diagrammart
	public static UMLDiagramm getDiagramm(File file) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document = null;
		try {
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			try (FileReader freader = new FileReader(file)) {
				document = builder.parse(new InputSource(freader));
			}
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}

		if (document == null) {
			return null;
		}

		Node xmiNode = document.getFirstChild();
		if (xmiNode == null || !xmiNode.getNodeName().equals("XMI")) {
			return null;
		}

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		xpath.setNamespaceContext(new UMLNameSpaceContext());

		XPathExpression expr;
		Node result = null;
		try {
			expr = xpath.compile("//UML:Namespace.ownedElement/node()[2]");
			result = (Node) expr.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
		}

		if (result == null || !result.getNodeName().startsWith("UML:")) {
			return null;
		}

		String diagrammType = result.getNodeName();

		if (diagrammType.equals(ActivityDiagramm.TYPE)) {
			return new ActivityDiagramm(file, result);
		}
		return new ClassDiagramm(file, result.getParentNode());
	}

	static protected class UMLNameSpaceContext implements NamespaceContext {
		@Override
		public Iterator<String> getPrefixes(String namespaceURI) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getPrefix(String namespaceURI) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix == null)
				throw new NullPointerException("Null prefix");
			else if ("UML".equals(prefix))
				return "org.omg.xmi.namespace.UML";
			else if ("xml".equals(prefix))
				return XMLConstants.XML_NS_URI;
			return XMLConstants.NULL_NS_URI;
		}
	}
}
