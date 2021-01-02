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
import java.util.Iterator;
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
 * View-Servlet for displaying the admin-overview/startpage
 * @author Sven Strickroth
 */
public class AdminMenueOverView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		template.printTemplateHeader("Admin-Menü", "<a href=\"" + Util.generateHTMLLink("Overview", response) + "\">Meine Veranstaltungen</a> - Admin-Menü");
		PrintWriter out = response.getWriter();

		@SuppressWarnings("unchecked")
		Iterator<Lecture> lectureIterator = ((List<Lecture>) request.getAttribute("lectures")).iterator();
		if (lectureIterator.hasNext()) {
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Veranstaltung</th>");
			out.println("<th>Semester</th>");
			out.println("</tr>");
			while (lectureIterator.hasNext()) {
				Lecture lecture = lectureIterator.next();
				out.println("<tr>");
				out.println("<td><a href=\"" + Util.generateHTMLLink("?action=showLecture&lecture=" + lecture.getId(), response) + "\">" + Util.escapeHTML(lecture.getName()) + "</a></td>");
				out.println("<td>" + lecture.getReadableSemester() + "</td>");
				out.println("</tr>");
			}
			out.println("</table>");
		}
		out.println("<p class=mid><a href=\"" + Util.generateHTMLLink("?action=newLecture", response) + "\">Neue Veranstaltung</a></p>");
		out.println("<p class=mid><a href=\"" + Util.generateHTMLLink("?action=showAdminUsers", response) + "\">Super User anzeigen</a></p>");
		out.println("<p class=mid><a onclick=\"return sendAsPost(this, 'Wirklich CleanUp durchführen?')\" href=\"" + Util.generateHTMLLink("?action=cleanup", response) + "\">Verzeichnis Cleanup</a></p>");

		template.printTemplateFooter();
	}
}
