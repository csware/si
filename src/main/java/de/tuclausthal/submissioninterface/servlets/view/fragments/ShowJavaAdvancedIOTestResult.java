/*
 * Copyright 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.view.fragments;

import java.io.PrintWriter;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;

import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.util.Util;

public class ShowJavaAdvancedIOTestResult {
	public static void printTestResults(PrintWriter out, JavaAdvancedIOTest jtt, String testOutput, boolean forStudent, StringBuffer javaScript) {
		JsonObject object = null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(testOutput))) {
			object = jsonReader.readObject();
		} catch (JsonParsingException ex) {
		}
		if (object == null) {
			if (forStudent) {
				out.println("<b>Ausgabe:</b><br><pre>" + Util.escapeHTML(testOutput) + "</pre>");
			} else {
				out.println("<textarea id=\"testresultjtt" + jtt.getId() + "\" cols=80 rows=15>" + Util.escapeHTML(testOutput) + "</textarea>");
				javaScript.append("testResultSetup('jtt" + jtt.getId() + "');");
			}
		} else if (object.containsKey("steps")) {
			JsonValue arr = object.get("steps");
			if (arr.getValueType().equals(JsonValue.ValueType.ARRAY)) {
				out.println("<table class=border>");
				out.println("<tr>");
				out.println("<th>Test</th>");
				out.println("<th>Erwartet</th>");
				out.println("<th>Erhalten</th>");
				out.println("<th>OK?</th>");
				out.println("</tr>");
				JsonArray array = arr.asJsonArray();
				for (int i = 0; i < array.size(); ++i) {
					// TODO make nicer!
					JsonObject stepObject = array.get(i).asJsonObject();
					int foundTest = -1;
					for (int j = 0; j < jtt.getTestSteps().size(); ++j) {
						if (jtt.getTestSteps().get(j).getTeststepid() == stepObject.getInt("id")) {
							foundTest = j;
							break;
						}
					}
					if (foundTest >= 0) {
						out.println("<tr>");
						out.println("<td>" + Util.escapeHTML(jtt.getTestSteps().get(foundTest).getTitle()) + "</td>");
						out.println("<td><pre id=\"exp" + jtt.getId() + "-" + i + "\">" + Util.escapeHTML(stepObject.getString("expected")) + "</pre></td>");
						out.println("<td><pre id=\"got" + jtt.getId() + "-" + i + "\">" + Util.escapeHTML(stepObject.getString("got")) + "</pre><pre id=\"diff" + jtt.getId() + "-" + i + "\" style=\"display:none;\"></pre></td>");
						out.println("<td>" + Util.boolToHTML(stepObject.getBoolean("ok")) + (stepObject.getBoolean("ok") ? "" : " (<a href=\"javascript:dodiff('" + jtt.getId() + "-" + i + "')\">Diff</a>)") + "</td>");
						out.println("</tr>");
					}
				}
				out.println("</table>");
				if (object.containsKey("missing-tests") && object.getBoolean("missing-tests") == true) {
					out.println("<p>Nicht alle Tests wurden durchlaufen.</p>");
				}
				if (object.containsKey("time-exceeded") && object.getBoolean("time-exceeded") == true) {
					out.println("<p>Der Test wurde zwangweise beendet, da er das Zeitlimit überschritten hat.</p>");
				}
				if (object.containsKey("exitedCleanly") && object.getBoolean("exitedCleanly") == false) {
					out.println("<p>Das Program wurde nicht ordentlich beendet.</p>");
				}
				if (object.containsKey("stderr")) {
					if (forStudent) { // TODO show stderr to students?
						out.println("<b>Laufzeitfehler:</b><br><pre>" + Util.escapeHTML(object.getString("stderr")) + "</pre>");
					} else {
						out.println("<textarea id=\"testresultajtt" + jtt.getId() + "\" cols=80 rows=15>" + Util.escapeHTML(object.getString("stderr")) + "</textarea>");
						javaScript.append("testResultSetup('ajtt" + jtt.getId() + "');");
					}
				}
			}
		}
	}
}
