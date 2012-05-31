/*
 * Copyright 2012 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for adding/editing a task
 * @author Sven Strickroth
 */
public class TaskManagerDynamicTaskPreView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Task task = (Task) request.getAttribute("task");
		List<String> correctResults = (List<String>) request.getAttribute("correctResults");
		String[] resultFields = (String[]) request.getAttribute("resultFields");
		String[] variableNames = (String[]) request.getAttribute("variableNames");
		List<TaskNumber> taskNumbers = (List<TaskNumber>) request.getAttribute("taskNumbers");

		PrintWriter out = response.getWriter();

		template.printTemplateHeader("Vorschau", task);

		out.println("<dl>");
		out.println("<dt><b>Aufgabenstellung:</b></dt>");
		out.println("<dd>" + Util.makeCleanHTML(task.getDescription()) + "</dd>");

		out.println("<dt><b>Werte:</b></dt>");
		out.println("<dd>");
		int variableCounter = 0;
		for (TaskNumber tn : taskNumbers) {
			out.print(Util.escapeHTML(variableNames[variableCounter]) + " ($Var" + variableCounter + "$)" + ": " + Util.escapeHTML(tn.getNumber()));
			if (!tn.getNumber().equals(tn.getOrigNumber())) {
				out.print(" (" + Util.escapeHTML(tn.getOrigNumber()) + ")");
			}
			out.println("<br>");
			variableCounter++;
		}
		out.println("</dd>");

		out.println("<dt><b>Lösung:</b></dt>");
		out.println("<dd>");
		int resultCounter = 0;
		for (String result : correctResults) {
			if (resultFields[resultCounter].startsWith("-")) {
				out.println(Util.escapeHTML(resultFields[resultCounter]) + ": (" + Util.escapeHTML(result) + ")<br>");
			} else {
				out.println(Util.escapeHTML(resultFields[resultCounter]) + ": " + Util.escapeHTML(result) + "<br>");
			}
			resultCounter++;
		}
		out.println("</dd>");
		out.println("</dl>");

		out.println("<p><div class=mid><a href=\"" + response.encodeURL("TaskManager?lecture=" + task.getTaskGroup().getLecture().getId() + "&amp;taskid=" + task.getTaskid() + "&amp;action=editTask") + "\">Aufgabe bearbeiten</a></div>");

		template.printTemplateFooter();
	}
}
