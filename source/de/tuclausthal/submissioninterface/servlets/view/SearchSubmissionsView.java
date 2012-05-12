/*
 * Copyright 2011-2012 Sven Strickroth <email@cs-ware.de>
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
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for search
 * @author Sven Strickroth
 */
public class SearchSubmissionsView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Task task = (Task) request.getAttribute("task");

		template.addKeepAlive();
		template.printTemplateHeader("Abgaben durchsuchen", task);
		out.println("<form action=\"" + response.encodeURL("?taskid=" + task.getTaskid()) + "\" method=post>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Suchen nach:</th>");
		out.println("<td><input type=input name=q></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Suchen in:</th>");
		out.println("<td><select multiple size=15 name=search>");
		if (task.isADynamicTask()) {
			out.println("<option value=\"dyntaskdescription\" selected>Dynamische Daten der Aufgabenstellung</option>");
			out.println("<option value=\"dyntasksolution\" selected>Dynamische Daten der Lösung</option>");
		}
		out.println("<option value=\"files\" selected>Abgegebene Dateien</option>");
		if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
			out.println("<option value=\"publiccomments\" selected>Öffentliche Bewertungs-Kommentare</option>");
			out.println("<option value=\"privatecomments\" selected>Interne Bewertungs-Kommentare</option>");
			if (task.getTests().size() >= 0) {
				out.println("<option value=\"testresults\" selected>Testergebnisse</option>");
			}
		}
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=Suchen> <a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		template.printTemplateFooter();
	}
}
