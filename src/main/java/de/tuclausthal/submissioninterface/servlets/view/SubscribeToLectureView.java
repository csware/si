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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying the list of lectures a user can subscribe to
 * @author Sven Strickroth
 */
public class SubscribeToLectureView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		List<Lecture> lectures = (List<Lecture>) request.getAttribute("lectures");

		template.printTemplateHeader("Veranstaltungen", "<a href=\"" + response.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; Veranstaltungen");
		PrintWriter out = response.getWriter();

		if (lectures.size() > 0) {
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Veranstaltung</th>");
			out.println("<th>Anmelden</th>");
			out.println("</tr>");
			for (Lecture lecture : lectures) {
				out.println("<tr>");
				out.println("<td>" + Util.escapeHTML(lecture.getName()) + "</td>");
				out.println("<td><a href=\"" + response.encodeURL("?lecture=" + lecture.getId()) + "\">anmelden</a></td>");
				out.println("</tr>");
			}
			out.println("</table><p>");
		} else {
			out.println("<div class=mid>keine Veranstaltungen gefunden.</div>");
		}

		template.printTemplateFooter();
	}
}
