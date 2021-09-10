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
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for adding a new plagiarism test
 * @author Sven Strickroth
 */
@GATEView
public class DupeCheckFormView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Task task = (Task) request.getAttribute("task");

		template.printEditTaskTemplateHeader("Ähnlichkeitsprüfung", task);
		PrintWriter out = response.getWriter();

		if (new File(Configuration.getInstance().getDataPath(), "jplag.jar").exists()) {
			out.println("<h2>JPlag Test</h2>");
			out.println("<form action=\"" + Util.generateHTMLLink("?action=savesimilaritytest", response) + "\" method=post>");
			out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
			out.println("<input type=hidden name=action value=performCheck>");
			out.println("<input type=hidden name=type value=jplag>");
			out.println("<input type=hidden name=normalizer1 value=\"\">");
			out.println("<input type=hidden name=normalizer2 value=\"\">");
			out.println("<input type=hidden name=normalizer3 value=\"\">");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Minimale Ähnlichkeit:</th>");
			out.println("<td><input size=5 type=text value=80 name=minsimilarity> %</td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Dateien ausschließen:</th>");
			out.println("<td><input size=100 type=text name=excludeFiles><br>(Dateinamen durch Komma getrennt)</td>");
			out.println("</tr>");
			out.println("<tr>");
			out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
			out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&action=editTask&lecture=" + task.getTaskGroup().getLecture().getId(), response));
			out.println("\">Abbrechen</a></td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");
		} else {
			out.println("<p>(JPlag-Tests sind nicht verfügbar, da JPlag nicht gefunden wurde.)</p>");
		}

		out.println("<h2>Plaggie Test (Java <= 1.6)</h2>");
		out.println("<form action=\"" + Util.generateHTMLLink("?action=savesimilaritytest", response) + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		out.println("<input type=hidden name=action value=performCheck>");
		out.println("<input type=hidden name=type value=plaggie>");
		out.println("<input type=hidden name=normalizer1 value=\"\">");
		out.println("<input type=hidden name=normalizer2 value=\"\">");
		out.println("<input type=hidden name=normalizer3 value=\"\">");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Minimale Ähnlichkeit:</th>");
		out.println("<td><input size=5 type=text value=80 name=minsimilarity> %</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Dateien ausschließen:</th>");
		out.println("<td><input size=100 type=text name=excludeFiles><br>(Dateinamen durch Komma getrennt)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&action=editTask&lecture=" + task.getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		out.println("<h2>Weitere Tests</h2>");
		out.println("<form action=\"" + Util.generateHTMLLink("?action=savesimilaritytest", response) + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		out.println("<input type=hidden name=action value=performCheck>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Typ:</th>");
		out.println("<td><select size=1 name=type>");
		out.println("<option value=\"levenshtein\">Levenshtein</option>");
		out.println("<option value=\"compression\">Kolmogorov Komplexität</option>");
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Grundlage:</th>");
		out.println("<td><select size=1 name=normalizer1>");
		out.println("<option value=\"code\">Quellcode ohne Kommentare</option>");
		out.println("<option value=\"comments\">nur Kommentare</option>");
		out.println("<option value=\"both\">kompletter Quellcode</option>");
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Schreibweise:</th>");
		out.println("<td><select size=1 name=normalizer2>");
		out.println("<option value=\"lc\">Groß- und Kleinschreibung ignorieren</option>");
		out.println("<option value=\"none\">nicht ignorieren</option>");
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Normalizer:</th>");
		out.println("<td><select size=1 name=normalizer3>");
		out.println("<option value=\"newlines\">Doppelte Leerzeilen entfernen</option>");
		out.println("<option value=\"spacestabs\">Doppelte Spaces/Tabs entfernen</option>");
		out.println("<option value=\"all\" selected>Doppelte Leerzeilen/Spaces/Tabs entfernen</option>");
		out.println("<option value=\"none\">nicht normalisieren</option>");
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Minimale Ähnlichkeit:</th>");
		out.println("<td><input size=5 type=text value=80 name=minsimilarity> %</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Dateien ausschließen:</th>");
		out.println("<td><input size=100 type=text name=excludeFiles value=\".classpath,.project,META-INF,.settings\"><br>(Dateinamen durch Komma getrennt)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&action=editTask&lecture=" + task.getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		template.printTemplateFooter();
	}
}
