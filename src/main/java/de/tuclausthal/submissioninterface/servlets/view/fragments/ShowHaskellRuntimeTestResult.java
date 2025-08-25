/*
 * Copyright 2021-2023 Sven Strickroth <email@cs-ware.de>
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

import static de.tuclausthal.submissioninterface.servlets.controller.HaskellRuntimeTestManager.extractUnescapedGhciExpressionWrappedInCatchAndTimeout;
import static de.tuclausthal.submissioninterface.servlets.controller.HaskellRuntimeTestManager.prettyPrintCyclicIntMappers;

import java.io.PrintWriter;
import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;

import de.tuclausthal.submissioninterface.persistence.datamodel.HaskellRuntimeTest;
import de.tuclausthal.submissioninterface.util.Util;

public class ShowHaskellRuntimeTestResult { // similar code in ShowDockerTestResult
	public static void printTestResults(PrintWriter out, HaskellRuntimeTest haskellRuntimeTest, String testOutput, boolean forStudent, StringBuilder javaScript) {
		JsonObject object = null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(testOutput))) {
			object = jsonReader.readObject();
		} catch (JsonParsingException ignored) {
		}

		if (object == null) {
			out.println("Keine gültige Ausgabe erhalten.");
		} else if (object.containsKey("steps")) {
			JsonValue arr = object.get("steps");
			if (arr.getValueType().equals(JsonValue.ValueType.ARRAY) && arr.asJsonArray().isEmpty()) {
				// TODO@CHW make this nicer
				if ((object.containsKey("exitedCleanly") && !object.getBoolean("exitedCleanly")) || (object.containsKey("time-exceeded") && object.getBoolean("time-exceeded")) || (object.containsKey("missing-tests") && object.getBoolean("missing-tests"))) {
					if (object.containsKey("stderr")) {
						if (forStudent) { // TODO show stderr to students?
							if (object.containsKey("exitCode") && object.getInt("exitCode") == 15) {
								out.println("<b>Der zu testende Code ist syntaktisch nicht korrekt und kann daher nicht getestet werden.</b><br>");
							} else {
								out.println("<b>Syntaxfehler:</b><br><pre>" + Util.escapeHTML(cleanup(object, object.getString("stderr"))) + "</pre>");
							}
						} else {
							out.println("<textarea id=\"testresultajtt1\" cols=80 rows=15>" + Util.escapeHTML(object.getString("stderr")) + "</textarea>");
						}
					} else {
						out.println("Ein unbekannter Fehler ist aufgetreten.");
					}
				}
			} else if (arr.getValueType().equals(JsonValue.ValueType.ARRAY)) {
				out.println("<br>");
				JsonArray array = arr.asJsonArray();
				for (int i = 0; i < array.size(); ++i) {
					JsonObject stepObject = array.get(i).asJsonObject();
					int foundTest = -1;
					for (int j = 0; j < haskellRuntimeTest.getTestSteps().size(); ++j) {
						if (haskellRuntimeTest.getTestSteps().get(j).getTeststepid() == stepObject.getInt("id")) {
							foundTest = j;
							break;
						}
					}
					if (foundTest >= 0) {
						out.println("<div style=\"max-width: 100%; overflow-x: auto;\">");
						out.println("<table style=\"width: 100%; overflow-wrap: break-word;\">");
						out.println("<thead>");
						out.println("<tr>");
						out.println("<th style=\"white-space: nowrap; width: auto;\">Testfall</th>");
						String testCode = haskellRuntimeTest.getTestSteps().get(foundTest).getTestcode();
						String simplifiedTestCode = extractUnescapedGhciExpressionWrappedInCatchAndTimeout(testCode);
						if (simplifiedTestCode != null) {
							simplifiedTestCode = prettyPrintCyclicIntMappers(simplifiedTestCode);
						}
						out.println("<th style=\"width: 100%\"><code>" + Util.escapeHTML(simplifiedTestCode != null ? simplifiedTestCode : testCode) + "</code></th>");
						out.println("</tr>");
						out.println("</thead>");

						out.println("<tr>");
						out.println("<td>Erwartet</td>");
						out.println("<td><pre id=\"exp" + haskellRuntimeTest.getId() + "-" + i + "\"><code>" + Util.escapeHTML(stepObject.getString("expected")) + "</code></pre></td>");
						out.println("</tr>");

						out.println("<tr>");
						out.println("<td>Erhalten</td>");
						out.println("<td><pre id=\"got" + haskellRuntimeTest.getId() + "-" + i + "\"><code>" + Util.escapeHTML(cleanup(object, stepObject.getString("got"))) + "</code></pre><pre id=\"diff" + haskellRuntimeTest.getId() + "-" + i + "\" style=\"display:none;\"></pre></td>");
						out.println("</tr>");

						out.println("<tr>");
						out.println("<td>OK?</td>");
						out.println("<td>" + Util.boolToHTML(stepObject.getBoolean("ok")) + (stepObject.getBoolean("ok") ? "" : " (<a href=\"javascript:dodiff('" + haskellRuntimeTest.getId() + "-" + i + "')\">Diff</a>)") + "</td>");
						out.println("</tr>");

						out.println("</table>");
						out.println("</div>");
						out.println("<br>");
					}
				}

				boolean wasError = false;
				if (object.containsKey("missing-tests") && object.getBoolean("missing-tests")) {
					out.println("<p>Nicht alle Tests wurden durchlaufen.</p>");
					wasError = true;
				}
				if (object.containsKey("time-exceeded") && object.getBoolean("time-exceeded")) {
					out.println("<p>Der Test wurde zwangweise beendet, da er das Zeitlimit überschritten hat.</p>");
					wasError = true;
				}
				if (object.containsKey("exitedCleanly") && !object.getBoolean("exitedCleanly")) {
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
							out.println("<b>Laufzeitfehler/Warnungen:</b><br><pre>" + Util.escapeHTML(cleanup(object, stderr)) + "</pre>");
						} else {
							out.println("<textarea id=\"testresultadtt" + haskellRuntimeTest.getId() + "\" cols=80 rows=15>" + Util.escapeHTML(stderr) + "</textarea>");
						}
					}
				}
			}
		}
	}

	private static String cleanup(JsonObject object, String string) {
		if (object.containsKey("tmpdir")) {
			string = string.replace(object.getString("tmpdir") + "/administrative/", "");
			string = string.replace(object.getString("tmpdir") + "/administrative", "");
			string = string.replace(object.getString("tmpdir") + "/student/", "");
			string = string.replace(object.getString("tmpdir") + "/student", "");
			string = string.replace(object.getString("tmpdir"), "");
		}
		return string;
	}
}
