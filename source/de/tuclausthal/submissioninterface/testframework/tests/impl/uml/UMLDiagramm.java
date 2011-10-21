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
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Diese Klasse checkt die XMI Datei hinsichtlich der Diagrammart
 */
public abstract class UMLDiagramm {

	private String name;

	public UMLDiagramm(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	//lesen der XMI Datei und einordnen der Diagrammart
	public static UMLDiagramm getDiagramm(File file) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			FileReader freader = new FileReader(file);
			document = builder.parse(new InputSource(freader));
			freader.close();
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}

		if (document == null) {
			return null;
		}

		Node xmiNode = document.getFirstChild();
		if (xmiNode == null || !xmiNode.getNodeName().equals("XMI")){
			return null;
		}

		Node xmiContentNode = xmiNode.getFirstChild();
		if (xmiContentNode == null || !xmiContentNode.getNodeName().equals("XMI.content")){
			return null;
		}

		Node umlContentNode = xmiContentNode.getFirstChild();
		if (umlContentNode == null || !umlContentNode.getNodeName().equals("UML:Model")){
			return null;
		}

		Node umlModelNode = umlContentNode.getFirstChild();
		if (umlModelNode == null || !umlModelNode.getNodeName().equals("UML:Namespace.ownedElement")){
			return null;
		}

		Node umlOwnedElementNode = umlModelNode.getFirstChild();
		if (umlOwnedElementNode == null || !umlOwnedElementNode.getNodeName().startsWith("UML:")){
			return null;
		}

		String diagrammType = umlOwnedElementNode.getNodeName();

		if (diagrammType.equals("UML:ActivityGraph")) {
			return new ActivityDiagramm(file.getAbsolutePath());
		} else if (diagrammType.equals("UML:Class")) {
			return new ClassDiagramm(file.getAbsolutePath());
		}

		return null;
	}
}
