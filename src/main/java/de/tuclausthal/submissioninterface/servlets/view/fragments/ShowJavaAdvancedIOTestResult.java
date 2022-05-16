/*
 * Copyright 2020-2022 Sven Strickroth <email@cs-ware.de>
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
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;

import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.util.Util;

public class ShowJavaAdvancedIOTestResult { // similar code in ShowDockerTestResult
	public static void printTestResults(PrintWriter out, JavaAdvancedIOTest jtt, String testOutput, boolean forStudent, StringBuilder javaScript) {
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
			}
		} else if (object.containsKey("syntaxerror")) {
			out.println("<p>");
			if ("student-code".equals(object.getString("syntaxerror"))) {
				out.println("<b>Der zu testende Code ist syntaktisch nicht korrekt und kann daher nicht getestet werden.</b><br>");
			} else {
				out.println("<b>Der Test konnte nicht erfolgreich ausgeführt werden.</b><br>Es fehlt eine erforderliche Datei oder der abgegebene Code entspricht nicht der Spezifikation. Mögliche Ursachen sind z. B.:<ul><li>falsches Package</li><li>geforderte Methode oder Instanzvariable<ul><li>fehlt</li><li>ist falsch geschrieben</li><li>hat falschen (Rückgabe-)Typ</li><li>hat die falschen Parameter</li><li>besitzt die falsche Sichtbarkeit</li></ul></ul>");
			}
		} else if (object.containsKey("steps")) {
			JsonValue arr = object.get("steps");
			if (arr.getValueType().equals(JsonValue.ValueType.ARRAY)) {
				out.println("<table>");
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
				boolean wasError = false;
				if (object.containsKey("missing-tests") && object.getBoolean("missing-tests") == true) {
					out.println("<p>Nicht alle Tests wurden durchlaufen.</p>");
					wasError = true;
				}
				if (object.containsKey("time-exceeded") && object.getBoolean("time-exceeded") == true) {
					out.println("<p>Der Test wurde zwangweise beendet, da er das Zeitlimit überschritten hat.</p>");
					wasError = true;
				}
				if (object.containsKey("exitedCleanly") && object.getBoolean("exitedCleanly") == false) {
					out.println("<p>Das Program wurde nicht ordentlich beendet.</p>");
					wasError = true;
				}
				if (wasError && object.containsKey("stderr")) {
					String stderr = object.getString("stderr");
					if (object.containsKey("separator")) {
						String separator = object.getString("separator");
						int lastIndex = stderr.lastIndexOf(separator);
						if (lastIndex >= 0) {
							stderr = stderr.substring(lastIndex + separator.length());
						}
					}
					if (!stderr.trim().isEmpty()) {
						if (forStudent) { // TODO show stderr to students?
							out.println("<b>Laufzeitfehler:</b><br><pre>" + Util.escapeHTML(stderr) + "</pre>");
						} else {
							out.println("<textarea id=\"testresultajtt" + jtt.getId() + "\" cols=80 rows=15>" + Util.escapeHTML(stderr) + "</textarea>");
						}
					}
				}
			}
		}
	}
}
