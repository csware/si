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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying the list of lectures a user can subscribe to
 * @author Sven Strickroth
 */
@GATEView
public class SubscribeToLectureView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		@SuppressWarnings("unchecked")
		List<Lecture> lectures = (List<Lecture>) request.getAttribute("lectures");

		template.printTemplateHeader("Veranstaltungen", "<a href=\"" + Util.generateHTMLLink("Overview", response) + "\">Meine Veranstaltungen</a> &gt; Veranstaltungen");
		PrintWriter out = response.getWriter();

		if (!lectures.isEmpty()) {
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Veranstaltung</th>");
			out.println("<th>Anmelden</th>");
			out.println("</tr>");
			for (Lecture lecture : lectures) {
				out.println("<tr>");
				out.println("<td>" + Util.escapeHTML(lecture.getName()) + "</td>");
				out.println("<td><form method=post action=\"" + Util.generateHTMLLink("?lecture=" + lecture.getId(), response) + "\"><input type=submit value=anmelden></form></td>");
				out.println("</tr>");
			}
			out.println("</table><p>");
		} else {
			out.println("<div class=mid>keine Veranstaltungen gefunden.</div>");
		}

		template.printTemplateFooter();
	}
}
