/*
 * Copyright 2009-2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTestStep;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for adding/editing/deleting advanved Java IO test steps
 * @author Sven Strickroth
 */
public class JavaAdvancedIOTestManagerOverView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		JavaAdvancedIOTest test = (JavaAdvancedIOTest) request.getAttribute("test");

		template.addJQuery();
		template.addKeepAlive();
		template.printTemplateHeader("Erweiteren Java-IO-Test bearbeiten", test.getTask());

		PrintWriter out = response.getWriter();

		// similar code in TestManagerAddTestFormView
		out.println("<h2>" + Util.escapeHTML(test.getTestTitle()) + "</h2>");
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=testid value=\"" + test.getId() + "\">");
		out.println("<input type=hidden name=action value=edittest>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title maxlength=250 size=60 value=\"" + Util.escapeHTML(test.getTestTitle()) + "\" required=required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.println(Util.generateHTMLLink("TaskManager?action=editTask&taskid=" + test.getTask().getTaskid() + "&lecture=" + test.getTask().getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		for (JavaAdvancedIOTestStep step : test.getTestSteps()) {
			out.println("<h2>" + Util.escapeHTML(step.getTitle()) + "</h2>");
			out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
			out.println("<input type=hidden name=testid value=\"" + test.getId() + "\">");
			out.println("<input type=hidden name=teststepid value=\"" + step.getTeststepid() + "\">");
			out.println("<input type=hidden name=action value=updateStep>");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Titel:</th>");
			out.println("<td><input type=text name=title maxlength=250 size=60 value=\"" + Util.escapeHTML(step.getTitle()) + "\" required=required></td>");
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
			out.println("<a onclick=\"return sendAsPost(this, 'Wirklich löschen?')\"  href=\"");
			out.println(Util.generateHTMLLink("JavaAdvancedIOTestManager?testid=" + test.getId() + "&action=deleteStep&teststepid=" + step.getTeststepid(), response));
			out.println("\">Löschen</a></td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");
		}

		out.println("<hr>");

		out.println("<h2>Neuer Test-Schritt <a href=\"#\" onclick=\"$('#teststephelp').toggle(); return false;\">(?)</a></h2>");

		out.println("<div style=\"display:none;\" id=teststephelp><b>Hilfe:</b><br>");
		out.println("<p>Diese Art von Test erlaubt es einfache Ausgabe-Tests zu definieren. Sollen Studierende z. B. in der Klasse MathFunctions die Methode leastCommonMultiple(a,b) programmieren, so kann die Methode relativ einfach mit verschiedenen Werten aufgerufen und die Ausgabe auf STDOUT mit einem erwartetem Wert überprüft werden. Der STDERR wird den Studierenden im Normalfall nicht gezeigt, ebensowenig mögliche Compiler-Fehler.</p>");
		out.println("<p>Test-Schritt-Definition z. B.:<br>"+
				"<table class=border>" + 
				"<tr>" + 
				"<th>Titel:</th>" + 
				"<td><input type=text name=title disabled size=60 value=\"leastCommonMultiple(26,61)\"></td>" + 
				"</tr>" + 
				"<tr>" + 
				"<th>Testcode:</th>" + 
				"<td><textarea cols=60 rows=2 name=testcode disabled>MathFunctions mc = new MathFunctions();\n" + 
				"System.out.println(mc.leastCommonMultiple(26,61));</textarea></td>" + 
				"</tr>" + 
				"<tr>" + 
				"<th>Erwartete Ausgabe:</th>" + 
				"<td><textarea cols=60 rows=1 name=expect disabled>1586</textarea></td>" + 
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
				"<td>leastCommonMultiple(1,1)</td>" + 
				"<td><pre>1</pre></td>" + 
				"<td><pre>1" + 
				"</pre></td>" + 
				"<td><span class=green>ja</span></td>" + 
				"</tr>" + 
				"<tr>" + 
				"<td>leastCommonMultiple(1,2)</td>" + 
				"<td><pre>2</pre></td>" + 
				"<td><pre>2" + 
				"</pre></td>" + 
				"<td><span class=green>ja</span></td>" + 
				"</tr>" + 
				"<tr>" + 
				"<td>leastCommonMultiple(26,61)</td>" + 
				"<td><pre>1586</pre></td>" + 
				"<td><pre>1326</pre></td>" + 
				"<td><span class=\"red\">nein</span></td>" + 
				"</tr>" + 
				"</table></p>");
		out.println("<p>Schlägt die Kompilierung des studentischen Codes oder der definierten Tests fehl oder wird ein Laufzeitfehler geworfen, erhalten die Studierenden die Ausgabe \"Nicht alle Tests wurden durchlaufen. Das Program wurde nicht ordentlich beendet.\", wobei die Tabelle alle bisherigen inkl. des zuletzt ausgeführten Tests zeigt (die Spalte \"Erhalten\" ist dann ggf. leer). Für genauere Ausgaben bei Syntaxfehlern bitte einen Syntaxtest nutzen. Syntaxfehler im Testcode treten z. B. dann auf, wenn eine Methode aufgerufen werden soll, die im studentischen Code nicht existiert. Bei einem Laufzeitfehler wird der STDERR (Stacktrace) separat für Studierende und TutorInnen angezeigt.</p>");
		out.println("<p>Die erwartete Ausgabe und tatsächliche Ausgabe wird getrimmt und hinsichtlich der Zeilenenden auf \"\\n\" normalisiert und mittels exaktem Stringvergleich verglichen. Im Testcode kann beliebiger Java-Code verwendet werden, der auch in einer normalen Block-Umgebung erlaubt ist (da keine imports möglich sind, sind ggf. absolute Klassennamen notwendig).</p>");
		out.println("<p>Diese Art von Test ist für einfache Ausgabetests ausgerichtet, es können aber auch approximative Tests oder nahezu beliebige Überprüfungen durchgefühert werden, z.B. erwartet \"ca. 0.13\" und Testcode \"float f = StudentCode.getFloat(); if (Math.abs(f - 0.13) < 0.01) { System.out.println(\"ca. 0.13\"); } else { System.out.println(f); }\".</p>");
		out.println("</div>");

		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=testid value=\"" + test.getId() + "\">");
		out.println("<input type=hidden name=action value=addNewStep>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title maxlength=250 value=\"\" size=60 required=required></td>");
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
