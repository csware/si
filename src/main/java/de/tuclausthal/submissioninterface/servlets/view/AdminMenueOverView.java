/*
 * Copyright 2009-2012, 2020-2021, 2023-2025 Sven Strickroth <email@cs-ware.de>
 *
 * This file is part of the GATE.
 *
 * GATE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * GATE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GATE. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.servlets.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.SelfTest;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying the admin-overview/startpage
 * @author Sven Strickroth
 */
@GATEView
public class AdminMenueOverView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		template.printAdminMenueTemplateHeader();
		PrintWriter out = response.getWriter();

		@SuppressWarnings("unchecked")
		Iterator<Lecture> lectureIterator = ((List<Lecture>) request.getAttribute("lectures")).iterator();
		if (lectureIterator.hasNext()) {
			out.println("<table>");
			out.println("<thead>");
			out.println("<tr>");
			out.println("<th>Veranstaltung</th>");
			out.println("<th>Semester</th>");
			out.println("</tr>");
			out.println("</thead>");
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
		out.println("<p class=mid><a href=\"" + Util.generateHTMLLink(SelfTest.class.getSimpleName(), response) + "\">Selbsttest</a></p>");
		out.println("<p class=mid><a href=\"" + Util.generateHTMLLink("?action=cleanup", response) + "\">Verzeichnis Cleanup (erst Dry-Run, kann lange dauern)</a></p>");

		out.println("<h2>Veranstaltung aus Export importieren</h2>");
		out.println("<FORM class=mid ENCTYPE=\"multipart/form-data\" method=POST action=\"" + Util.generateHTMLLink("?action=import", response) + "\">");
		out.println("<INPUT TYPE=checkbox NAME=dryrun id=importdryrun checked> <label for=importdryrun>Dry-Run</label><br>");
		out.println("<INPUT TYPE=file NAME=file required>");
		out.println("<INPUT TYPE=submit VALUE=upload>");
		out.println("</FORM>");

		template.printTemplateFooter();
	}
}
