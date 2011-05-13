/*
 * Copyright 2011 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for editing a group
 * @author Sven Strickroth
 */
public class EditMultipleGroupsFormView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Lecture lecture = (Lecture) request.getAttribute("lecture");

		template.printTemplateHeader("Mehrere Gruppen bearbeiten", lecture);
		out.println("<form action=\"" + response.encodeURL("?") + "\" method=post>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Studenten können sich eintragen:</th>");
		out.println("<td><input type=checkbox name=allowStudentsToSignup></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studenten können wechseln:</th>");
		out.println("<td><input type=checkbox name=allowStudentsToQuit></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Studenten:</th>");
		out.println("<td><input type=text name=maxStudents> (leer: keine Speicherung)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Abgabegruppe:</th>");
		out.println("<td><input type=checkbox name=submissionGroup></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Setzen für:</th>");
		out.println("<td><select multiple size=15 name=gids>");
		for (Group group : lecture.getGroups()) {
			out.println("<option value=" + group.getGid() + ">" + Util.escapeHTML(group.getName()) + "</option>");
		}
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"" + response.encodeURL("ShowLecture?lecture=" + lecture.getId()) + "\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		template.printTemplateFooter();
	}
}
