/*
 * Copyright 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2025 Esat Avci <e.avci@campus.lmu.de>
 *
 * This file is part of the GATE.
 *
 * GATE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * GATE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GATE. If not, see <http://www.gnu.org/licenses/>.
 */
package de.tuclausthal.submissioninterface.servlets.view.fragments;

import java.io.PrintWriter;
import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import de.tuclausthal.submissioninterface.persistence.datamodel.HaskellSyntaxTest;
import de.tuclausthal.submissioninterface.util.Util;

public class ShowHaskellSyntaxTestResult {
	public static void printTestResults(PrintWriter out, HaskellSyntaxTest test, String testOutput, boolean isStudent, StringBuilder javaScript) {

		JsonObject json = Json.createReader(new StringReader(testOutput)).readObject();

		String stderr = json.getString("stderr", "");

		if (!stderr.isEmpty()) {
			out.println("<p><strong>Fehlerausgabe:</strong></p>");
			out.println("<pre class=\"haskellstderr\">" + Util.escapeHTML(stderr) + "</pre>");
		}

	}
}
