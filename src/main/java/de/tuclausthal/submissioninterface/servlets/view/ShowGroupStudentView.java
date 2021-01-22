/*
 * Copyright 2021 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a task in tutor view
 * @author Sven Strickroth
 */
public class ShowGroupStudentView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Group group = (Group) request.getAttribute("group");

		template.printTemplateHeader(group);

		PrintWriter out = response.getWriter();

		if (group.getTutors() != null && !group.getTutors().isEmpty()) {
			if (group.getTutors().size() > 1) {
				out.println("<h2>Meine TutorInnen</h2>");
			} else {
				out.println("<h2>Mein(e) TutorIn</h2>");
			}
			out.println("<ul>");
			for (Participation tutor : group.getTutors()) {
				out.print("<li><a href=\"mailto:" + Util.escapeHTML(tutor.getUser().getEmail()) + "\">" + Util.escapeHTML(tutor.getUser().getFullName()) + "</a></li>");
			}
			out.println("</ul>");
		}

		out.println("<h2>Gruppenmitglieder</h2>");
		out.println("<ul>");
		for (Participation tutor : group.getMembers()) {
			out.print("<li><a href=\"mailto:" + Util.escapeHTML(tutor.getUser().getEmail()) + "\">" + Util.escapeHTML(tutor.getUser().getFullName()) + "</a></li>");
		}
		out.println("</ul>");

		template.printTemplateFooter();
	}
}
