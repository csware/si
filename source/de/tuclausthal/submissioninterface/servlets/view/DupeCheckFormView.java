/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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
 * View-Servlet for displaying a form for adding a new plagiarism test
 * @author Sven Strickroth
 */
public class DupeCheckFormView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Task task = (Task) request.getAttribute("task");

		template.printTemplateHeader("Ähnlichkeitsprüfung", task);
		out.println("<h2>Plaggie Test</h2>");
		out.println("<form action=\"" + response.encodeURL("?action=savesimilaritytest") + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		out.println("<input type=hidden name=action value=performCheck>");
		out.println("<input type=hidden name=type value=plaggie>");
		out.println("<input type=hidden name=normalizer1 value=\"\">");
		out.println("<input type=hidden name=normalizer2 value=\"\">");
		out.println("<input type=hidden name=normalizer3 value=\"\">");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Minimale Ähnlichkeit:</th>");
		out.println("<td><input type=text value=50 name=minsimilarity> %</td>");
		out.println("</tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=starten> <a href=\"");
		out.println(response.encodeURL("TaskManager?taskid=" + task.getTaskid() + "&amp;action=editTask&amp;lecture=" + task.getLecture().getId()));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		out.println("<h2>Weitere Tests</h2>");
		out.println("<form action=\"" + response.encodeURL("?action=savesimilaritytest") + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		out.println("<input type=hidden name=action value=performCheck>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Typ:</th>");
		out.println("<td><select name=type>");
		out.println("<option value=\"levenshtein\">Levenshtein</option>");
		out.println("<option value=\"compression\">Kolmogorov Komplexität</option>");
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Grundlage:</th>");
		out.println("<td><select name=normalizer1>");
		out.println("<option value=\"code\">Quellcode ohne Kommentare</option>");
		out.println("<option value=\"comments\">nur Kommentare</option>");
		out.println("<option value=\"both\">kompletter Quellcode</option>");
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Schreibweise:</th>");
		out.println("<td><select name=normalizer2>");
		out.println("<option value=\"lc\">Groß- und Kleinschreibung ignorieren</option>");
		out.println("<option value=\"none\">nicht ignorieren</option>");
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<th>Normalizer:</th>");
		out.println("<td><select name=normalizer3>");
		out.println("<option value=\"newlines\">Doppelte Leerzeilen entfernen</option>");
		out.println("<option value=\"spacestabs\">Doppelte Spaces/Tabs entfernen</option>");
		out.println("<option value=\"all\" selected>Doppelte Leerzeilen/Spaces/Tabs entfernen</option>");
		out.println("<option value=\"none\">nicht normalisieren</option>");
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Minimale Ähnlichkeit:</th>");
		out.println("<td><input type=text value=50 name=minsimilarity> %</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=starten> <a href=\"");
		out.println(response.encodeURL("TaskManager?taskid=" + task.getTaskid() + "&amp;action=editTask&amp;lecture=" + task.getLecture().getId()));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		template.printTemplateFooter();
	}
}
