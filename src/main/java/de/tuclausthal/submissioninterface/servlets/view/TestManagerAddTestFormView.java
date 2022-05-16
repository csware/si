/*
 * Copyright 2009-2012, 2020-2022 Sven Strickroth <email@cs-ware.de>
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.TaskManager;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.testframework.tests.impl.DockerTest;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for adding a function test
 * @author Sven Strickroth
 */
@GATEView
public class TestManagerAddTestFormView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Task task = (Task) request.getAttribute("task");

		template.addKeepAlive();
		template.printEditTaskTemplateHeader("Test erstellen", task);
		PrintWriter out = response.getWriter();
		out.println("<h2>Java Compile/Syntax Test</h2>");
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		out.println("<input type=hidden name=action value=saveNewTest>");
		out.println("<input type=hidden name=type value=compile>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title value=\"Syntax-Test\" required=required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		out.println("<td><textarea cols=60 rows=10 name=description></textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Tutorentest:</th>");
		out.println("<td><input type=checkbox name=tutortest checked> (Ergebnis wird den TutorInnen zur Korrektur angezeigt)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># ausführbar für Studierende:</th>");
		out.println("<td><input type=text name=timesRunnableByStudents value=\"0\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studierenden Test-Details anzeigen:</th>");
		out.println("<td><input type=checkbox name=giveDetailsToStudents checked></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?action=editTask&taskid=" + task.getTaskid() + "&lecture=" + task.getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		out.println("<h2>Erweiterer Java-IO-Test</h2>");
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		out.println("<input type=hidden name=action value=saveNewTest>");
		out.println("<input type=hidden name=type value=advancedjavaio>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title value=\"Testen\" required=required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Tutorentest:</th>");
		out.println("<td><input type=checkbox name=tutortest checked> (Ergebnis wird den TutorInnen zur Korrektur angezeigt)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># ausführbar für Studierende:</th>");
		out.println("<td><input type=text name=timesRunnableByStudents value=\"0\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studierenden Test-Details anzeigen:</th>");
		out.println("<td><input type=checkbox name=giveDetailsToStudents checked></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid>Weitere Einstellungen auf zweiter Seite...</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?action=editTask&taskid=" + task.getTaskid() + "&lecture=" + task.getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		// similar code in DockerTestManagerView
		if (new File(DockerTest.SAFE_DOCKER_SCRIPT).exists()) {
			out.println("<h2>Docker Test</h2>");
			out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
			out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
			out.println("<input type=hidden name=action value=saveNewTest>");
			out.println("<input type=hidden name=type value=docker>");
			out.println("<table>");
			out.println("<tr>");
			out.println("<th>Titel:</th>");
			out.println("<td><input type=text name=title value=\"Testen\" required></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Tutorentest:</th>");
			out.println("<td><input type=checkbox name=tutortest checked> (Ergebnis wird den TutorInnen zur Korrektur angezeigt)</td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th># ausführbar für Studierende:</th>");
			out.println("<td><input type=text name=timesRunnableByStudents value=\"0\" required pattern=\"[0-9]+\"></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Studierenden Test-Details anzeigen:</th>");
			out.println("<td><input type=checkbox name=giveDetailsToStudents checked></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Preparation Code:</th>");
			out.println("<td><textarea cols=60 rows=10 name=preparationcode></textarea></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid>Weitere Einstellungen auf zweiter Seite...</td>");
			out.println("</tr>");
			out.println("<tr>");
			out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
			out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?action=editTask&taskid=" + task.getTaskid() + "&lecture=" + task.getTaskGroup().getLecture().getId(), response));
			out.println("\">Abbrechen</a></td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");
		} else {
			out.println("<p>(Docker-Tests sind nicht verfügbar, da /usr/local/bin/safe-docker nicht gefunden wurde.)</p>");
		}

		// similar code in ChecklistTestManagerView
		out.println("<h2>Checklist Test</h2>");
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		out.println("<input type=hidden name=action value=saveNewTest>");
		out.println("<input type=hidden name=type value=checklist>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title value=\"Testen\" required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># ausführbar für Studierende:</th>");
		out.println("<td><input type=text name=timesRunnableByStudents value=\"0\" required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid>Weitere Einstellungen auf zweiter Seite...</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?action=editTask&taskid=" + task.getTaskid() + "&lecture=" + task.getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		//Formular um UML Constraint Test anzulegen
		out.println("<h2>UML Constraint Test</h2>");
		out.println("<form ENCTYPE=\"multipart/form-data\" action=\"" + Util.generateHTMLLink("?taskid=" + task.getTaskid() + "&action=saveNewTest&type=umlConstraint", response) + "\" method=post>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title value=\"UML Vergleichstest\" required=required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		out.println("<td><textarea cols=60 rows=10 name=description></textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Timeout:</th>");
		out.println("<td><input type=text name=timeout value=15></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Musterl&ouml;sung:</th>");
		out.println("<td><INPUT TYPE=file NAME=testcase required=required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Tutorentest:</th>");
		out.println("<td><input type=checkbox name=tutortest checked> (Ergebnis wird den TutorInnen zur Korrektur angezeigt)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># ausführbar für Studierende:</th>");
		out.println("<td><input type=text name=timesRunnableByStudents value=\"0\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?action=editTask&taskid=" + task.getTaskid() + "&lecture=" + task.getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		out.println("<h2>Java RegExp. Test</h2>");
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		out.println("<input type=hidden name=action value=saveNewTest>");
		out.println("<input type=hidden name=type value=regexp>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title value=\"Funktionstest\" required=required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		out.println("<td><textarea cols=60 rows=10 name=description></textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Main-Klasse:</th>");
		out.println("<td><input type=text name=mainclass required=required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>CommandLine Parameter:</th>");
		out.println("<td><input type=text name=parameter></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Reg.Exp.:</th>");
		out.println("<td><input type=text name=regexp required=required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Timeout:</th>");
		out.println("<td><input type=text name=timeout value=15></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Tutorentest:</th>");
		out.println("<td><input type=checkbox name=tutortest checked> (Ergebnis wird den TutorInnen zur Korrektur angezeigt)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># ausführbar für Studierende:</th>");
		out.println("<td><input size=5 type=text name=timesRunnableByStudents value=\"0\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studierenden Test-Details anzeigen:</th>");
		out.println("<td><input type=checkbox name=giveDetailsToStudents checked></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?action=editTask&taskid=" + task.getTaskid() + "&lecture=" + task.getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		out.println("<h2>JUnit Test</h2>");
		out.println("<form ENCTYPE=\"multipart/form-data\" action=\"" + Util.generateHTMLLink("?taskid=" + task.getTaskid() + "&action=saveNewTest&type=junit", response) + "\" method=post>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title value=\"Funktionstest\" required=required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		out.println("<td><textarea cols=60 rows=10 name=description></textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Timeout:</th>");
		out.println("<td><input type=text name=timeout value=15></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>JUnit-Testcase:</th>");
		out.println("<td><INPUT TYPE=file NAME=testcase required=required> (Main Testclass: AllTests)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Main-Klasse:</th>");
		out.println("<td><input type=text name=mainclass value=\"AllTests\" required=required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Tutorentest:</th>");
		out.println("<td><input type=checkbox name=tutortest checked> (Ergebnis wird den TutorInnen zur Korrektur angezeigt)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># ausführbar für Studierende:</th>");
		out.println("<td><input size=5 type=text name=timesRunnableByStudents value=\"0\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studierenden Test-Details anzeigen:</th>");
		out.println("<td><input type=checkbox name=giveDetailsToStudents checked></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?action=editTask&taskid=" + task.getTaskid() + "&lecture=" + task.getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		out.println("<h2>Kommentar-Metrik-Test</h2>");
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		out.println("<input type=hidden name=action value=saveNewTest>");
		out.println("<input type=hidden name=type value=commentmetric>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title value=\"Kommentar-Metrik\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		out.println("<td><textarea cols=60 rows=10 name=description></textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Min. Prozent Kommentare:</th>");
		out.println("<td><input type=text name=minProzent value=5></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Excluded Files:</th>");
		out.println("<td><input type=text name=excludedFiles> (kommasepariert)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Tutorentest:</th>");
		out.println("<td><input type=checkbox name=tutortest checked> (Ergebnis wird den TutorInnen zur Korrektur angezeigt)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># ausführbar für Studierende:</th>");
		out.println("<td><input size=5 type=text name=timesRunnableByStudents value=\"0\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studierenden Test-Details anzeigen:</th>");
		out.println("<td><input type=checkbox name=giveDetailsToStudents checked></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?action=editTask&taskid=" + task.getTaskid() + "&lecture=" + task.getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		template.printTemplateFooter();
	}
}
