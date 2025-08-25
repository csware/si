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

package de.tuclausthal.submissioninterface.servlets.view;

import static de.tuclausthal.submissioninterface.servlets.controller.HaskellRuntimeTestManager.extractUnescapedGhciExpressionWrappedInCatchAndTimeout;
import static de.tuclausthal.submissioninterface.servlets.controller.HaskellRuntimeTestManager.prettyPrintCyclicIntMappers;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTestStep;
import de.tuclausthal.submissioninterface.persistence.datamodel.HaskellRuntimeTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.HaskellRuntimeTestIdentifier;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.HaskellRuntimeTestManager;
import de.tuclausthal.submissioninterface.servlets.controller.PerformTest;
import de.tuclausthal.submissioninterface.servlets.controller.TaskManager;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for clustering haskell submissions based on common errors (dynamic/runtime analysis)
 *
 * @author Christian Wagner
 */
@GATEView
public class HaskellRuntimeTestManagerView extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		HaskellRuntimeTest test = (HaskellRuntimeTest) request.getAttribute("test");

		HttpSession httpSession = request.getSession(false);

		template.addKeepAlive();
		template.printEditTaskTemplateHeader("Haskell Runtime Test bearbeiten", test.getTask());

		PrintWriter out = response.getWriter();

		if (httpSession != null) {
			out.println(errorBoxIfErrorOccurred(httpSession, "haskellRuntimeTestBrowseError", "Beim Analysieren der Musterlösung ist ein Fehler aufgetreten"));
			out.println(errorBoxIfErrorOccurred(httpSession, "haskellRuntimeTestGenerateError", "Beim Generieren der Testfälle ist ein Fehler aufgetreten"));
			out.println(errorBoxIfErrorOccurred(httpSession, "haskellRuntimeTestEditTestcaseError", "Beim Bearbeiten des Testfalls ist ein Fehler aufgetreten"));
		}

		out.println("""
				<script>
					function disableGeneratorCalls(button) {
						const rect = button.getBoundingClientRect();
						button.style.width = rect.width + "px";
						button.style.height = rect.height + "px";
						button.innerHTML = '<span class="spinner"></span>';
						Array.from(document.getElementsByClassName('generatorCaller')).forEach(b => b.disabled = true);
					}
					function submitGeneratorForm(formId, button) {
						const f = document.getElementById(formId);
						if (f.reportValidity()) {
							disableGeneratorCalls(button);
							f.submit();
						}
					}
					function toggleTableRowHighlight(checkbox) {
						const row = checkbox.closest('tr');
						if (checkbox.checked) {
							row.classList.add('selected-table-row');
						} else {
							row.classList.remove('selected-table-row');
						}
					}
					function setActionInputField(actionInputFieldId, actionName) {
						document.getElementById(actionInputFieldId).value = actionName;
					}
					function toggleAllTestcaseSelectionCheckboxes(masterCheckbox, formId) {
						const checkboxes = document.getElementById(formId).getElementsByClassName('testcaseSelectionCheckbox');
						Array.from(checkboxes).forEach(cb => {
							cb.checked = masterCheckbox.checked;
							toggleTableRowHighlight(cb);
						});
					}
					function syncTestcaseSelectionMasterCheckbox(formId) {
						const checkboxes = document.getElementById(formId).getElementsByClassName('testcaseSelectionCheckbox');
						const masterCheckbox = document.getElementById(formId).getElementsByClassName('testcaseSelectionMasterCheckbox')[0];
						const allChecked = Array.from(checkboxes).every(cb => cb.checked);
						masterCheckbox.checked = allChecked;
					}
					function enterEditMode(formId, noEditModeDivId, editModeDivId, testcaseSelectionCheckboxId) {
						setElementWithIdVisible(noEditModeDivId, false);
						setElementWithIdVisible(editModeDivId, true);
						const checkboxes = document.getElementById(formId).getElementsByClassName('testcaseSelectionCheckbox');
						const masterCheckbox = document.getElementById(formId).getElementsByClassName('testcaseSelectionMasterCheckbox')[0];
						Array.from(checkboxes).forEach(cb => {
							cb.checked = false;
							cb.disabled = true;
							toggleTableRowHighlight(cb);
						});
						masterCheckbox.checked = false;
						masterCheckbox.disabled = true;
						const testcaseSelectionCheckbox = document.getElementById(testcaseSelectionCheckboxId);
						testcaseSelectionCheckbox.disabled = false;
						testcaseSelectionCheckbox.checked = true;
						testcaseSelectionCheckbox.style.pointerEvents = 'none';
						toggleTableRowHighlight(testcaseSelectionCheckbox);
					}
					function leaveEditMode(formId, noEditModeDivId, editModeDivId) {
						setElementWithIdVisible(noEditModeDivId, true);
						setElementWithIdVisible(editModeDivId, false);
						const checkboxes = document.getElementById(formId).getElementsByClassName('testcaseSelectionCheckbox');
						const masterCheckbox = document.getElementById(formId).getElementsByClassName('testcaseSelectionMasterCheckbox')[0];
						Array.from(checkboxes).forEach(cb => {
							cb.checked = false;
							cb.disabled = false;
							cb.style.pointerEvents = 'auto';
							toggleTableRowHighlight(cb);
						});
						masterCheckbox.checked = false;
						masterCheckbox.disabled = false;
					}
					function updateTestcodeSimplification(fullTestcodeClass, noGhciFullFunctionsClass, simpleTestcodeClass, showGhciEmbeddingCheckboxId, showRandomFunctionsCheckboxId) {
						const showGhciEmbedding = document.getElementById(showGhciEmbeddingCheckboxId).checked;
						const showRandomFunctions = document.getElementById(showRandomFunctionsCheckboxId).checked;
				
						Array.from(document.getElementsByClassName(fullTestcodeClass)).forEach(elem => elem.style.display = 'none');
						Array.from(document.getElementsByClassName(noGhciFullFunctionsClass)).forEach(elem => elem.style.display = 'none');
						Array.from(document.getElementsByClassName(simpleTestcodeClass)).forEach(elem => elem.style.display = 'none');
						document.getElementById(showRandomFunctionsCheckboxId).disabled = false;
				
						if (showGhciEmbedding) {
							Array.from(document.getElementsByClassName(fullTestcodeClass)).forEach(elem => elem.style.display = 'block');
							document.getElementById(showRandomFunctionsCheckboxId).checked = true;
							document.getElementById(showRandomFunctionsCheckboxId).disabled = true;
						} else if (showRandomFunctions) {
							Array.from(document.getElementsByClassName(noGhciFullFunctionsClass)).forEach(elem => elem.style.display = 'block');
						} else {
							Array.from(document.getElementsByClassName(simpleTestcodeClass)).forEach(elem => elem.style.display = 'block');
						}
					}
					function setElementWithIdVisible(elementId, visible) {
						document.getElementById(elementId).style.display = visible ? 'block' : 'none';
					}
				</script>
				""");

		out.println("<link href=\"" + request.getContextPath() + "/assets/prism/prism.css\" rel=\"stylesheet\">");

		// similar code in TestManagerAddTestFormView
		out.println("<h2>" + Util.escapeHTML(test.getTestTitle()) + "</h2>");
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=testid value=\"" + test.getId() + "\">");
		out.println("<input type=hidden name=action value=edittest>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title maxlength=250 size=60 value=\"" + Util.escapeHTML(test.getTestTitle()) + "\" required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Tutorentest:</th>");
		out.println("<td><input type=checkbox name=tutortest" + (test.isForTutors() ? " checked" : "") + "> (Ergebnis wird den TutorInnen zur Korrektur angezeigt)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># ausführbar für Studierende:</th>");
		out.println("<td><input type=text name=timesRunnableByStudents value=\"" + test.getTimesRunnableByStudents() + "\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studierenden Test-Details anzeigen:</th>");
		out.println("<td><input type=checkbox name=giveDetailsToStudents" + (test.isGiveDetailsToStudents() ? " checked" : "") + "></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Preparation Code:</th>");
		out.println("<td><textarea cols=60 rows=10 name=preparationcode>" + Util.escapeHTML(test.getPreparationShellCode()) + "</textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?action=editTask&taskid=" + test.getTask().getTaskid() + "&lecture=" + test.getTask().getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		out.println("<hr>");

		out.println("<h2>Benutzerdefinierte Haskell Funktionen und Datentypen der Musterlösung</h2>");
		StringBuilder newtypeOrDatasHtml = new StringBuilder();
		newtypeOrDatasHtml.append("""
				<tr>
					<th style="width: 25%">Typname</th>
					<th style="width: 75%">Typdefinition und <code>Arbitrary</code> Instanz</th>
				</tr>
				""");

		StringBuilder functionsHtml = new StringBuilder();
		functionsHtml.append("""
				<tr>
					<th>Funktion</th>
					<th>Typsignatur (<code>:t</code>)</th>
					<th>Konkrete Typsignatur</th>
					<th>Generator ausführen</th>
				</tr>
				""");

		boolean showFunctionTable = false;
		boolean showNewtypeOrDataTable = false;

		for (HaskellRuntimeTestIdentifier identifier : test.getIdentifiers()) {
			switch (identifier.getIdentifierClass()) {
				case "newtypeordata":
					showNewtypeOrDataTable = true;
					newtypeOrDatasHtml.append(String.format("""
							<tr>
								<td style="width: 25%%">
									<div style="overflow-x: auto; overflow-y: hidden;">
										<code class="language-haskell">%1$s</code>
									</div>
								</td>
								<td style="width: 75%%">
									<div style="overflow-x: auto; overflow-y: hidden;">
									<details style="width: 100%%;">
										<summary><code class="language-haskell">%2$s</code></summary>
										<pre style="overflow-x: auto; white-space: pre; max-width: 100%%"><code class="language-haskell">%3$s</code></pre>
									</details>
									</div>
								</td>
							</tr>
							""", Util.escapeHTML(identifier.getNewtypeOrDataTypename()), Util.escapeHTML(identifier.getNewtypeOrDataDefinition()), Util.escapeHTML(identifier.getNewtypeOrDataArbitraryInstance())));
					break;
				case "function":
					showFunctionTable = true;
					boolean functionHasConcreteType = !Objects.equals(identifier.getFunctionConcreteType(), "") && !identifier.getFunctionConcreteType().contains("=>");
					String formHiddenState = functionHasConcreteType ? "" : "hidden";
					String missingConcreteWarningHiddenState = functionHasConcreteType ? "hidden" : "";
					functionsHtml.append(String.format("""
							<tr>
								<td><div style="overflow-x: auto; overflow-y: hidden;">
									<code class="language-haskell">%2$s</code>
								</div></td>
								<td><div style="overflow-x: auto; overflow-y: hidden;">
									<code class="language-haskell">%3$s</code>
								</div></td>
								<td><div style="overflow-x: auto; overflow-y: hidden;">
									<code class="language-haskell">%7$s</code>
								</div></td>
								<td style="text-align: center;">
									<form action="%4$s" method="post" id="generateFunctionTestcasesForm%1$s" %8$s>
										<input type=hidden name=testid value=%5$s>
										<input type=hidden name=identifierid value=%1$s>
										<input type=hidden name=action value=generateFunctionTestcases>
										<input type="text" name="numberOfTestSteps" value="10" required="required" pattern="^[1-9][0-9]?$" style="width: 3ch">
										<button class="generatorCaller"
												onclick="submitGeneratorForm('generateFunctionTestcasesForm%1$s', this)">
											Testfälle generieren
										</button>
									</form>
									<p style="color: red" %9$s>Fehler: Konkrete Typsignatur enthält Constraints</p>
								</td>
							</tr>
							""", identifier.getIdentifierid(), Util.escapeHTML(identifier.getFunctionName()), Util.escapeHTML(identifier.getFunctionType()), Util.generateHTMLLink("?", response), test.getId(), Util.escapeHTML(identifier.getFunctionDefaultType()), Util.escapeHTML(identifier.getFunctionConcreteType()), formHiddenState, missingConcreteWarningHiddenState));
					break;
			}
		}

		if (showNewtypeOrDataTable) {
			out.println("<div style=\"max-width: 100%; width: 100%; overflow-x: auto;\">");
			out.println("<table style=\"width: 100%; table-layout: fixed; overflow-wrap: break-word;\">");
			out.println(newtypeOrDatasHtml);
			out.println("</table>");
			out.println("</div>");
		}
		if (showNewtypeOrDataTable && showFunctionTable) {
			out.println("<br>");
		}
		if (showFunctionTable) {
			out.println("<div style=\"max-width: 100%; overflow-x: auto;\">");
			out.println("<table style=\"width: 100%; table-layout: fixed; overflow-wrap: break-word;\">");
			out.println(functionsHtml);

			out.println(String.format("""
					<td colspan="4">
						<div align="center">
							<form action="%1$s" method=post id=addFunctionForm style="display: flex; align-items: center; gap: 0.5rem; justify-content: center;">
								<input type=hidden name=testid value="%2$s">
								<input type=hidden name=action value=addFunction>
								<span><b>Funktion manuell hinzufügen:</b></span>
								<label for="funcName">Name:</label>
								<input type="text" id="funcName" name="functionName" required>
								<label for="funcType">Typ:</label>
								<input type="text" id="funcType" name="functionType" required>
								<button class="generatorCaller"
									onclick="submitGeneratorForm('addFunctionForm', this)">
									hinzufügen
								</button>
							</form>
						</div>
					</td>
					""", Util.generateHTMLLink("?", response), test.getId()));

			out.println("</table>");
			out.println("</div>");
		}
		if (showNewtypeOrDataTable || showFunctionTable) {
			out.println("<br>");
		}

		out.println(String.format("""
				<div align="center">
					<form action="%1$s" method=post id=browseModelSolutionForm>
						<input type=hidden name=testid value="%2$s">
						<input type=hidden name=action value=browseModelSolution>
						<button class="generatorCaller"
							onclick="submitGeneratorForm('browseModelSolutionForm', this)">
							Musterlösung analysieren (<code>:browse</code>)
						</button>
						<a  onclick="return sendAsPost(this, 'Wirklich alle Haskell Identifier löschen?')"
							href="%3$s">
							(Zurücksetzen)
						</a>
					</form>
				</div>
				""", Util.generateHTMLLink("?", response), test.getId(), Util.generateHTMLLink(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + test.getId() + "&action=deleteHaskellIdentifiers", response)));

		// NOTE: DockerTestStep title is used for storing the function signature (see controller servlet)
		final Map<String, List<DockerTestStep>> testStepsGroupedByFunctionNameWithType = test.getTestSteps().stream().collect(Collectors.groupingBy(DockerTestStep::getTitle));
		List<String> sortedKeys = testStepsGroupedByFunctionNameWithType.keySet().stream().sorted().toList();

		if (!sortedKeys.isEmpty()) {
			final int numberOfTestSteps = test.getTestSteps().size();
			final String numberOfTestStepsText = numberOfTestSteps + " " + (numberOfTestSteps == 1 ? "Testschritt" : "Testschritte");
			out.println("<h2>Testschritte bearbeiten <span style=\"font-weight: normal;\">(" + numberOfTestStepsText + ", <a onclick=\"return sendAsPost(this, 'Wirklich mit Musterlösung testen?')\" href=\"" + Util.generateHTMLLink(PerformTest.class.getSimpleName() + "?modelsolution=true&testid=" + test.getId(), response) + "\">mit Musterlösung testen</a>)</span></h2>");
		}

		for (int i = 0; i < sortedKeys.size(); i++) {
			String functionNameWithType = sortedKeys.get(i);

			final int numberOfTestSteps = testStepsGroupedByFunctionNameWithType.get(functionNameWithType).size();
			final String numberOfTestStepsText = numberOfTestSteps + " " + (numberOfTestSteps == 1 ? "Testschritt" : "Testschritte");

			out.println("<h3>Funktion <code class=\"language-haskell\">" + Util.escapeHTML(functionNameWithType) + "</code><span style=\"font-weight: normal;\"> (" + numberOfTestStepsText + ")</span></h3>");

			String formId = "deleteOrDuplicateMultipleTestStepsForm" + i;
			String formActionInputFieldId = "deleteOrDuplicateMultipleTestStepsFormAction" + i;

			String showGhciEmbeddingCheckboxId = "showGhciEmbeddingCheckbox" + i;
			String showRandomFunctionsCheckboxId = "showRandomFunctionsCheckbox" + i;

			String fullTestcodeClass = "fullTestcode" + i;
			String noGhciFullFunctionsClass = "noGhciFullFunctions" + i;
			String simpleTestcodeClass = "simpleTestcode" + i;

			out.println(String.format("""
					<form action="%1$s" method="post" id="%2$s">
						<input type="hidden" name="testid" value="%3$s">
						<div style="max-width: 100%%; overflow-x: auto;">
						<table style="width: 100%%; table-layout: fixed;">
							<thead>
								<tr>
									<th style="width: 2em; white-space: nowrap;">
										<input type="checkbox" class="testcaseSelectionMasterCheckbox" onchange="toggleAllTestcaseSelectionCheckboxes(this, '%2$s')">
									</th>
									<th style="width: 66%%">
										Testcode
										<div style="font-weight: normal;">
											<label>
												<input 	type="checkbox"
														onchange="updateTestcodeSimplification('%6$s', '%7$s', '%8$s', '%4$s', '%5$s');"
														id="%4$s">
												<code>ghci</code> Einbettung anzeigen
											</label>
											<label>
												<input	type="checkbox"
														onchange="updateTestcodeSimplification('%6$s', '%7$s', '%8$s', '%4$s', '%5$s');"
														id="%5$s">
												Zufallsfunktionsdefinitionen anzeigen
											</label>
										</div>
									</th>
									<th>Erwartete Ausgabe</th>
								</tr>
							</thead>
					""", Util.generateHTMLLink("?", response), formId, test.getId(), showGhciEmbeddingCheckboxId, showRandomFunctionsCheckboxId, fullTestcodeClass, noGhciFullFunctionsClass, simpleTestcodeClass));

			for (DockerTestStep step : testStepsGroupedByFunctionNameWithType.get(functionNameWithType)) {
				String testcodeWithoutWrapperCode = extractUnescapedGhciExpressionWrappedInCatchAndTimeout(step.getTestcode());
				if (testcodeWithoutWrapperCode == null) {
					testcodeWithoutWrapperCode = step.getTestcode();
				}

				String testcodeWithoutWrapperCodeWithoutCyclicIntMappers = prettyPrintCyclicIntMappers(testcodeWithoutWrapperCode);

				String noEditModeDivId = "noEditModeDiv" + step.getTeststepid();
				String editModeDivId = "editModeDiv" + step.getTeststepid();
				String testcaseSelectionCheckboxId = "testcaseSelectionCheckbox" + step.getTeststepid();

				out.println(String.format("""
						<tr>
							<td style="width: 2em; white-space: nowrap; text-align: center;">
								<input type="checkbox"
									   class="testcaseSelectionCheckbox"
									   id="%12$s"
						  		       name="selectedTestStepIds"
						  		       value="%3$s"
						  		       onchange="syncTestcaseSelectionMasterCheckbox('%4$s'); toggleTableRowHighlight(this)">
							</td>
							<td>
								<div id="%10$s" style="display: block;">
									<div style="overflow-x: auto; overflow-y: hidden; display: none;" class="%7$s">
										<code class="language-haskell">%1$s</code>
										<a href="#" onclick="enterEditMode('%4$s', '%10$s', '%11$s', '%12$s'); return false;">bearbeiten</a>
									</div>
									<div style="overflow-x: auto; overflow-y: hidden; display: none;" class="%8$s">
										<code class="language-haskell">%5$s</code>
										<a href="#" onclick="enterEditMode('%4$s', '%10$s', '%11$s', '%12$s'); return false;">bearbeiten</a>
									</div>
									<div style="overflow-x: auto; overflow-y: hidden; display: block;" class="%9$s">
										<code class="language-haskell">%6$s</code>
										<a href="#" onclick="enterEditMode('%4$s', '%10$s', '%11$s', '%12$s'); return false;">bearbeiten</a>
									</div>
								</div>
								<div id="%11$s" style="display: none;">
									<textarea name="userEnteredFunctionCall%3$s" style="width: 99%%; resize: none" rows="8">%5$s</textarea>
									<div style="text-align: center">
										<a href="#" onclick="leaveEditMode('%4$s', '%10$s', '%11$s'); return false;">abbrechen</a>
										<button class="generatorCaller"
												type="button"
												onclick="setActionInputField('%13$s', 'editSingleTestStep'); submitGeneratorForm('%4$s', this)">
												speichern
										</button>
									</div>
								</div>
							</td>
							<td><div style="overflow-x: auto; overflow-y: hidden;">
								<code class="language-haskell">%2$s</code>
							</div></td>
						</tr>
						""", Util.escapeHTML(step.getTestcode()), Util.escapeHTML(step.getExpect()), step.getTeststepid(), formId, Util.escapeHTML(testcodeWithoutWrapperCode), Util.escapeHTML(testcodeWithoutWrapperCodeWithoutCyclicIntMappers), fullTestcodeClass, noGhciFullFunctionsClass, simpleTestcodeClass, noEditModeDivId, editModeDivId, testcaseSelectionCheckboxId, formActionInputFieldId));
			}

			out.println(String.format("""
					<tr>
						<td colspan="3" style="text-align: center; padding: 5px">
							Aktionen für selektierte Testschritte: <input type="hidden" name="action" id="%2$s">
							<button class="generatorCaller"
								type="button"
								onclick="setActionInputField('%2$s', 'duplicateMultipleTestSteps'); submitGeneratorForm('%1$s', this)">
								duplizieren
							</button>
							<button class="generatorCaller"
								type="button"
								onclick="setActionInputField('%2$s', 'deleteMultipleTestSteps'); submitGeneratorForm('%1$s', this)">
								löschen
							</button>
						</td>
					</tr>
					""", formId, formActionInputFieldId));
			// TODO@CHW: add button "Duplikate in Selektion entfernen"
			out.println("</table>");
			out.println("</div>");
			out.println("</form>");
			out.println("<br>");
		}

		out.println("<script src=\"" + request.getContextPath() + "/assets/prism/prism.js\" defer></script>");
		template.printTemplateFooter();
	}

	private String errorBoxIfErrorOccurred(HttpSession httpSession, String httpSessionAttributeName, String errorTitle) {
		String errorMessage = (String) httpSession.getAttribute(httpSessionAttributeName);
		httpSession.removeAttribute(httpSessionAttributeName);

		return (errorMessage == null) ? "" : String.format("""
				<div>
					<div class="error-title">
						%1$s
					</div>
					<div class="error-message">
						<pre>%2$s</pre>
					</div>
				</div>
				<br>
				<script>alert("%1$s")</script>
				""", Util.escapeHTML(errorTitle), Util.escapeHTML(errorMessage));
	}
}
