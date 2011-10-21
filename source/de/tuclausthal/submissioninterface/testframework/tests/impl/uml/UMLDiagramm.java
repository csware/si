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
	public static UMLDiagramm getDiagramm(File file) throws IOException {
		FileReader freader = new FileReader(file);
		BufferedReader reader = new BufferedReader(freader);

		UMLDiagramm result = null;

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains("UML:ActivityGraph xmi.id =")) {
				result = new ActivityDiagramm(file.getAbsolutePath());
				break;
			} else if (line.contains("Class xmi.id =")) {
				result = new ClassDiagramm(file.getAbsolutePath());
				break;
			}
			
		}

		freader.close();
		return result;
	}
}
