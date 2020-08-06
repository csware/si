/*
 * Copyright 2009-2010, 2012, 2020 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;

/**
 * View-Servlet for displaying a form for adding a group
 * @author Sven Strickroth
 */
public class AddGroupFormView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Lecture lecture = (Lecture) request.getAttribute("lecture");

		template.printTemplateHeader("Gruppe erstellen", lecture);
		PrintWriter out = response.getWriter();
		out.println("<form action=\"" + response.encodeURL("?") + "\" method=post>");
		out.println("<input type=hidden name=action value=saveNewGroup>");
		out.println("<input type=hidden name=lecture value=\"" + lecture.getId() + "\">");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Name:</th>");
		out.println("<td><input type=text required=required name=name></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studierende können sich eintragen:</th>");
		out.println("<td><input type=checkbox name=allowStudentsToSignup></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studierende können wechseln:</th>");
		out.println("<td><input type=checkbox name=allowStudentsToQuit></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Studierende:</th>");
		out.println("<td><input type=text name=maxStudents></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.println(response.encodeURL("ShowLecture?lecture=" + lecture.getId()));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		template.printTemplateFooter();
	}
}
