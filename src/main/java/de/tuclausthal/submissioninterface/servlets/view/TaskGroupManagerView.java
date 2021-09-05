/*
 * Copyright 2010-2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.ShowLecture;
import de.tuclausthal.submissioninterface.servlets.controller.TaskManager;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for adding/editing a task
 * @author Sven Strickroth
 */
@GATEView
public class TaskGroupManagerView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		TaskGroup taskGroup = (TaskGroup) request.getAttribute("taskGroup");
		Lecture lecture = taskGroup.getLecture();

		if (taskGroup.getTaskGroupId() != 0) {
			template.printTemplateHeader("Aufgabengruppe bearbeiten", lecture);
		} else {
			template.printTemplateHeader("neue Aufgabengruppe", lecture);
		}

		PrintWriter out = response.getWriter();
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		if (taskGroup.getTaskGroupId() != 0) {
			out.println("<input type=hidden name=action value=saveTaskGroup>");
			out.println("<input type=hidden name=taskgroupid value=\"" + taskGroup.getTaskGroupId() + "\">");
		} else {
			out.println("<input type=hidden name=action value=saveNewTaskGroup>");
		}
		out.println("<input type=hidden name=lecture value=\"" + lecture.getId() + "\">");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title value=\"" + Util.escapeHTML(taskGroup.getTitle()) + "\" required=required ></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.print(Util.generateHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + lecture.getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		if (taskGroup.getTaskGroupId() != 0) {
			out.print("<p class=mid><a onclick=\"return sendAsPost(this, 'Wirklich löschen?')\" href=\"");
			out.print(Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?action=deleteTaskGroup&taskgroupid=" + taskGroup.getTaskGroupId() + "&lecture=" + lecture.getId(), response));
			out.println("\">Löschen</a></p>");
		}
		template.printTemplateFooter();
	}
}
