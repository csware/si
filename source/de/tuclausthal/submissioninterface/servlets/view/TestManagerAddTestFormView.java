/*
 * Copyright 2009 - 2012 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;

/**
 * View-Servlet for displaying a form for adding a function test
 * @author Sven Strickroth
 */
public class TestManagerAddTestFormView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Task task = (Task) request.getAttribute("task");

		template.addKeepAlive();
		template.printTemplateHeader("Test erstellen", task);
		out.println("<h2>Compile/Syntax Test</h2>");
		out.println("<form action=\"" + response.encodeURL("?") + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		out.println("<input type=hidden name=action value=saveNewTest>");
		out.println("<input type=hidden name=type value=compile>");
		out.println("<table class=border>");
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
		out.println("<td><input type=checkbox name=tutortest></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># Ausführbar für Studenten:</th>");
		out.println("<td><input type=text name=timesRunnableByStudents value=\"0\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studenten Test-Details anzeigen:</th>");
		out.println("<td><input type=checkbox name=giveDetailsToStudents></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.println(response.encodeURL("ShowTask?taskid=" + task.getTaskid()));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		//Formular um UML Constraint Test anzulegen
		out.println("<p><h2>UML Constraint Test</h2>");
		out.println("<form ENCTYPE=\"multipart/form-data\" action=\"" + response.encodeURL("?taskid=" + task.getTaskid() + "&amp;action=saveNewTest&amp;type=umlConstraint") + "\" method=post>");
		out.println("<table class=border>");
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
		out.println("<td><input type=checkbox name=tutortest></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># Ausführbar für Studenten:</th>");
		out.println("<td><input type=text name=timesRunnableByStudents value=\"0\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.println(response.encodeURL("ShowTask?taskid=" + task.getTaskid()));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		out.println("<h2>RegExp. Test</h2>");
		out.println("<form action=\"" + response.encodeURL("?") + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		out.println("<input type=hidden name=action value=saveNewTest>");
		out.println("<input type=hidden name=type value=regexp>");
		out.println("<table class=border>");
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
		out.println("<td><input type=checkbox name=tutortest></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># Ausführbar für Studenten:</th>");
		out.println("<td><input size=5 type=text name=timesRunnableByStudents value=\"0\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studenten Test-Details anzeigen:</th>");
		out.println("<td><input type=checkbox name=giveDetailsToStudents></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.println(response.encodeURL("ShowTask?taskid=" + task.getTaskid()));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		out.println("<p><h2>JUnit. Test</h2>");
		out.println("<form ENCTYPE=\"multipart/form-data\" action=\"" + response.encodeURL("?taskid=" + task.getTaskid() + "&amp;action=saveNewTest&amp;type=junit") + "\" method=post>");
		out.println("<table class=border>");
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
		out.println("<td><input type=checkbox name=tutortest></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># Ausführbar für Studenten:</th>");
		out.println("<td><input size=5 type=text name=timesRunnableByStudents value=\"0\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studenten Test-Details anzeigen:</th>");
		out.println("<td><input type=checkbox name=giveDetailsToStudents></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.println(response.encodeURL("ShowTask?taskid=" + task.getTaskid()));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		out.println("<h2>Kommentar-Metrik-Test</h2>");
		out.println("<form action=\"" + response.encodeURL("?") + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		out.println("<input type=hidden name=action value=saveNewTest>");
		out.println("<input type=hidden name=type value=commentmetric>");
		out.println("<table class=border>");
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
		out.println("<td><input type=checkbox name=tutortest></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th># Ausführbar für Studenten:</th>");
		out.println("<td><input size=5 type=text name=timesRunnableByStudents value=\"0\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studenten Test-Details anzeigen:</th>");
		out.println("<td><input type=checkbox name=giveDetailsToStudents></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.println(response.encodeURL("ShowTask?taskid=" + task.getTaskid()));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		template.printTemplateFooter();
	}
}
