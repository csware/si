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

package de.tuclausthal.submissioninterface.servlets.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTestStep;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for adding/editing/deleting docker test steps
 * @author Sven Strickroth
 */
public class DockerTestManagerOverView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		DockerTest test = (DockerTest) request.getAttribute("test");

		template.addKeepAlive();
		template.printTemplateHeader("Docker Test bearbeiten", test.getTask());

		PrintWriter out = response.getWriter();

		// similar code in TestManagerAddTestFormView
		out.println("<h2>" + Util.escapeHTML(test.getTestTitle()) + "</h2>");
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=testid value=\"" + test.getId() + "\">");
		out.println("<input type=hidden name=action value=edittest>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title maxlength=250 size=60 value=\"" + Util.escapeHTML(test.getTestTitle()) + "\" required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Preparation Code:</th>");
		out.println("<td><textarea cols=60 rows=10 name=preparationcode>" + Util.escapeHTML(test.getPreparationShellCode()) + "</textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.println(Util.generateHTMLLink("TaskManager?action=editTask&taskid=" + test.getTask().getTaskid() + "&lecture=" + test.getTask().getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		out.println("<hr>");
		out.println("<h2>Testschritte</h2>");

		for (DockerTestStep step : test.getTestSteps()) {
			out.println("<h3>" + Util.escapeHTML(step.getTitle()) + "</h2>");
			out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
			out.println("<input type=hidden name=testid value=\"" + test.getId() + "\">");
			out.println("<input type=hidden name=teststepid value=\"" + step.getTeststepid() + "\">");
			out.println("<input type=hidden name=action value=updateStep>");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Titel:</th>");
			out.println("<td><input type=text name=title maxlength=250 size=60 value=\"" + Util.escapeHTML(step.getTitle()) + "\" required></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Testcode:</th>");
			out.println("<td><textarea cols=60 rows=10 name=testcode required>" + Util.escapeHTML(step.getTestcode()) + "</textarea></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Erwartete Ausgabe:</th>");
			out.println("<td><textarea cols=60 rows=10 name=expect>" + Util.escapeHTML(step.getExpect()) + "</textarea></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid><input type=submit value=speichern></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid>");
			out.println("<a onclick=\"return sendAsPost(this, 'Wirklich löschen?')\" href=\"");
			out.println(Util.generateHTMLLink("DockerTestManager?testid=" + test.getId() + "&action=deleteStep&teststepid=" + step.getTeststepid(), response));
			out.println("\">Löschen</a></td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");
		}

		out.println("<hr>");

		out.println("<h3>Neuer Test-Schritt <a href=\"#\" onclick=\"toggleVisibility('teststephelp'); return false;\">(?)</a></h2>");

		out.println("<div style=\"display:none;\" id=teststephelp><b>Hilfe:</b><br>");
		out.println("<p>Diese Art von Test erlaubt es beliebige einfache Ausgabe-Tests zu definieren. Mit dem Preparation-Code können vorbereitende Schritte als Bash-Skript programmiert werden. Schlägt dieser Schritt fehl, wird den Studierenden dies als Syntaxfehler angezeigt und der Inhalt von STDERR bereitgestellt. Ist dieser Schritt erfolgreich, werden die einzelnen Testschritte nacheinander aufgerufen, wobei für jeden Testschritt die Ausgabe auf STDOUT mit einem erwartetem Wert überprüft werden.</p>");
		out.println("<p>Preparation-Code z. B.:<br>"+
				"<textarea cols=80 rows=2 name=testcode disabled>echo -e \"hallo servus\\nsehr sakrisch\\ngut guad\" > woerterbuch.txt\n"
				+ "ghc uebersetzer-a.hs</textarea></p>");
		out.println("<p>Test-Schritt-Definition z. B.:<br>"+
				"<table class=border>" + 
				"<tr>" + 
				"<th>Titel:</th>" + 
				"<td><input type=text name=title disabled size=60 value=\"Eingabe &quot;gut&quot;\"></td>" + 
				"</tr>" + 
				"<tr>" + 
				"<th>Testcode:</th>" + 
				"<td><textarea cols=60 rows=1 name=testcode disabled>echo \"gut\" | ./uebersetzer-a</textarea></td>" + 
				"</tr>" + 
				"<tr>" + 
				"<th>Erwartete Ausgabe:</th>" + 
				"<td><textarea cols=60 rows=2 name=expect disabled>Geben Sie ein hochdeutsches Wort ein:\n"
				+ "\"guad\"</textarea></td>" + 
				"</tr>" + 
				"</table></p>");
		out.println("<p>Ausgabe bei Testdurchführung z. B.:<br>" +
				"<table>" + 
				"<tr>" + 
				"<th>Test</th>" + 
				"<th>Erwartet</th>" + 
				"<th>Erhalten</th>" + 
				"<th>OK?</th>" + 
				"</tr>" + 
				"<tr>" + 
				"<td>Eingabe \"gut\"</td>" + 
				"<td><pre>Geben Sie ein hochdeutsches Wort ein:\n"
				+ "\"guad\"</pre></td>" + 
				"<td><pre>Geben Sie ein hochdeutsches Wort ein:\n"
				+ "\"guad\"" + 
				"</pre></td>" + 
				"<td><span class=green>ja</span></td>" + 
				"</tr>" + 
				"<tr>" + 
				"<td>Leere Eingabe</td>" + 
				"<td><pre></pre></td>" + 
				"<td><pre></pre></td>" + 
				"<td><span class=green>ja</span></td>" + 
				"</tr>" + 
				"</table></p>");
		out.println("<p>Die erwartete Ausgabe und tatsächliche Ausgabe wird getrimmt und hinsichtlich der Zeilenenden auf \"\\n\" normalisiert und mittels exaktem Stringvergleich verglichen. Im Testcode kann beliebiger Bash-Code verwendet werden. In der Umgebung ist per Default \"set -e\" gesetzt, so dass das Skript nach einem nicht behandelten Fehler sofort abgebrochen wird.</p>");
		out.println("<p>Wird das Bash-Skript vorzeitig beendet, erhalten die Studierenden die Ausgabe \"Nicht alle Tests wurden durchlaufen. Das Program wurde nicht ordentlich beendet.\", wobei die Tabelle alle bisherigen zzgl. den zuletzt ausgeführten Test zeigt (die Spalte \"Erhalten\" ist dann ggf. leer). Bricht das Testskript nach der Preparation-Code-Phase ab, wird dies den Studierenden als Laufzeitfehler angezeigt und der Inhalt von STDERR seit dem Beginn des Testschritts bereitgestellt. Bricht das Testskript beim Preparationcode ab, wird den Studierenden ein Syntaxfehler angezeigt inkl. Inhalt von STDERR.</p>");
		out.println("<p>Diese Art von Test ist für einfache Ausgabetests ausgerichtet, es können aber auch approximative Tests oder nahezu beliebige Überprüfungen durchgefühert werden, z.B. erwartet \"True\" und Testcode \"ghci -e \"pi_approx 6 < 3.0 && pi_approx 6 > 2.99\" pi_approx.hs\".</p>");
		out.println("</div>");

		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=testid value=\"" + test.getId() + "\">");
		out.println("<input type=hidden name=action value=addNewStep>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title maxlength=250 value=\"\" size=60 required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Testcode:</th>");
		out.println("<td><textarea cols=60 rows=10 name=testcode required></textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Erwartete Ausgabe:</th>");
		out.println("<td><textarea cols=60 rows=10 name=expect></textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.println(Util.generateHTMLLink("TaskManager?action=editTask&taskid=" + test.getTask().getTaskid() + "&lecture=" + test.getTask().getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		template.printTemplateFooter();
	}
}
