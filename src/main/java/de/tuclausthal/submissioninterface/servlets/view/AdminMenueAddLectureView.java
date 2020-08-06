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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for adding a new lecture
 * @author Sven Strickroth
 */
public class AdminMenueAddLectureView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		template.printTemplateHeader("neue Veranstaltung", "<a href=\"" + response.encodeURL("Overview") + "\">Meine Veranstaltungen</a> - <a href=\"AdminMenue\">Admin-Menü</a> &gt; neue Veranstaltung");
		PrintWriter out = response.getWriter();

		Lecture dummyLecture = new Lecture();
		dummyLecture.setSemester(Util.getCurrentSemester());
		out.println("<form action=\"" + response.encodeURL("?action=saveLecture") + "\" method=post>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Name der Veranstaltung:</th>");
		out.println("<td><input type=text name=name required=required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Lösungen müssen abgenommen werden:</th>");
		out.println("<td><input type=checkbox name=requiresAbhnahme></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Semester:</th>");
		out.println("<td>" + dummyLecture.getReadableSemester() + "</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Gruppenweise Bewertung:</th>");
		out.println("<td><input type=checkbox name=groupWise></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=anlegen> <a href=\"" + response.encodeURL("?") + "\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		template.printTemplateFooter();
	}
}
