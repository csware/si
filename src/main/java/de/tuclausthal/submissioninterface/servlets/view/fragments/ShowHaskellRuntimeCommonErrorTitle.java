/*
 * Copyright 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2025 Christian Wagner <christian.wagner@campus.lmu.de>
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

import static de.tuclausthal.submissioninterface.servlets.controller.HaskellRuntimeTestManager.prettyPrintCyclicIntMappers;

import java.io.PrintWriter;
import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import de.tuclausthal.submissioninterface.util.Util;

public class ShowHaskellRuntimeCommonErrorTitle {
	public static void formatCommonErrorTitle(PrintWriter out, String commonErrorTitle) {
		try {
			JsonObject commonErrorTitleAsJson = Json.createReader(new StringReader(commonErrorTitle)).readObject();

			if (commonErrorTitleAsJson.containsKey("function")) {
				out.println("<strong>Funktion</strong>: <code>" + Util.escapeHTML(commonErrorTitleAsJson.getString("function", "")) + "</code><br>");
				out.println("<br>");
			}

			if (commonErrorTitleAsJson.containsKey("testcases")) {
				out.println("<strong>Fehlgeschlagene Testfälle:</strong>");

				out.println("<ul>");
				for (JsonValue testCaseVal : commonErrorTitleAsJson.getJsonArray("testcases")) {
					JsonObject testCase = testCaseVal.asJsonObject();
					String testcase = prettyPrintCyclicIntMappers(testCase.getString("testcase", ""));
					String got = testCase.getString("got", "");

					out.println(String.format("""
							<li>
								<b><code>%1$s</code></b>
								<br>
								<span class="red"><code>%2$s</code></span>
							</li>
							""", Util.escapeHTML(testcase), Util.escapeHTML(got)));
				}
				out.println("</ul>");
			}

			if (commonErrorTitleAsJson.containsKey("flags")) {
				out.println("<p><strong>Ausführungsstatus des Tests:</strong></p>");
				out.println("<ul>");
				for (JsonValue flagValue : commonErrorTitleAsJson.getJsonArray("flags")) {
					out.println("<li>" + Util.escapeHTML(flagValue.toString().replace("\"", "")) + "</li>");
				}
				out.println("</ul>");
			}
		} catch (Exception e) {
			out.println("<p><strong>Fehler beim Parsen des Fehler-Titels. Original Fehler-Titel:</strong></p>");
			out.println("<p>Original Fehler-Titel: " + Util.escapeHTML(commonErrorTitle) + "</p>");
		}
	}
}
