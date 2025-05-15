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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTestStep;
import de.tuclausthal.submissioninterface.persistence.datamodel.HaskellRuntimeTest;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.HaskellRuntimeTestManager;
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

		template.addKeepAlive();
		template.printEditTaskTemplateHeader("Haskell Runtime Test bearbeiten", test.getTask());

		PrintWriter out = response.getWriter();

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

		out.println("<h2>Neue Testschritte automatisch generieren</h2>");
		out.println("<p style=\"color: red\">Testschritt Generator ist noch nicht vollständig implementiert.</p>"); // TODO@CHW
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=testid value=\"" + test.getId() + "\">");
		out.println("<input type=hidden name=action value=generateNewTestSteps>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Number of test steps</th>");
		out.println("<td><input type=text name=numberOfTestSteps value=\"10\" required=required pattern=\"[0-9]+\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?action=editTask&taskid=" + test.getTask().getTaskid() + "&lecture=" + test.getTask().getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		out.println("<h2>Testschritte bearbeiten</h2>");
		out.println("<table>");
		out.println(/* @formatter:off */
            "<thead>" +
                "<tr>" +
                    "<th>Titel</th>" +
                    "<th>Testcode</th>" +
                    "<th>Expected</th>" +
                "</tr>" +
            "</thead>"
        /* @formatter:on */);

		for (DockerTestStep step : test.getTestSteps()) {
			String deleteTestStepLink = Util.generateHTMLLink(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + test.getId() + "&action=deleteStep&teststepid=" + step.getTeststepid(), response);
			out.println(/* @formatter:off */
                "<tr>" +
                    "<td>" +
                        Util.escapeHTML(step.getTitle()) + " " +
                        "<a onclick=\"return sendAsPost(this, 'Wirklich löschen?')\" href=\"" + deleteTestStepLink + "\">" +
                            "(Löschen)" +
                        "</a>" +
                    "</td>" +
                    "<td>" + Util.escapeHTML(step.getTestcode()) + "</td>" +
                    "<td>" + Util.escapeHTML(step.getExpect()) + "</td>" +
                "</tr>"
            /* @formatter:on */);
		}
		out.println("</table>");

		template.printTemplateFooter();
	}
}
