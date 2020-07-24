/*
 * Copyright 2011-2012, 2020 Sven Strickroth <email@cs-ware.de>
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
 * View-Servlet for displaying a form for mass mails
 * @author Sven Strickroth
 */
public class MassMailView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Lecture lecture = (Lecture) request.getAttribute("lecture");
		Group group = (Group) request.getAttribute("group");

		template.addKeepAlive();
		template.printTemplateHeader("E-Mail senden", lecture);
		PrintWriter out = response.getWriter();
		out.println("<form action=\"" + response.encodeURL("?lectureid=" + lecture.getId()) + "\" method=post>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Betreff:</th>");
		out.println("<td><input type=input required=required name=subject></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Nachricht:</th>");
		out.println("<td><textarea cols=60 rows=10 required=required name=message></textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Senden an:</th>");
		out.println("<td><input type=checkbox " + (group != null ? "" : "checked ") + "name=toall id=toall> <label for=toall>an alle Studenden der Vorlesung</label> <b>oder</b> an<br><select multiple size=15 name=gids>");
		out.println("<option value=\"nogroup\">ohne Gruppe</option>");
		for (Group theGroup : lecture.getGroups()) {
			String selected = "";
			if (group != null && group.getGid() == theGroup.getGid()) {
				selected = " selected";
			}
			out.println("<option value=" + theGroup.getGid() + selected + ">" + Util.escapeHTML(theGroup.getName()) + "</option>");
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
