/*
 * Copyright 2011-2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.ShowLecture;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for editing a group
 * @author Sven Strickroth
 */
@GATEView
public class EditMultipleGroupsFormView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Lecture lecture = (Lecture) request.getAttribute("lecture");

		template.addKeepAlive();
		template.printTemplateHeader("Mehrere Gruppen bearbeiten", lecture);
		PrintWriter out = response.getWriter();
		out.println("<form action=\"" + Util.generateHTMLLink("?lecture=" + lecture.getId(), response) + "\" method=post>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Studierende können sich eintragen:</th>");
		out.println("<td><select name=allowStudentsToSignup><option value=''>keine Änderung</option><option value=1>ja</option><option value=0>nein</option></select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studierende können wechseln:</th>");
		out.println("<td><select name=allowStudentsToQuit><option value=''>keine Änderung</option><option value=1>ja</option><option value=0>nein</option></select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Studierende:</th>");
		out.println("<td><input type=text name=maxStudents> (leer: keine Speicherung)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Abgabegruppe:</th>");
		out.println("<td><select name=submissionGroup><option value=''>keine Änderung</option><option value=1>ja</option><option value=0>nein</option></select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Mitglieder für Studierende einsehbar:</th>");
		out.println("<td><select name=membersvisible><option value=''>keine Änderung</option><option value=1>ja</option><option value=0>nein</option></select></td>");
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
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"" + Util.generateHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + lecture.getId(), response) + "\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		template.printTemplateFooter();
	}
}
