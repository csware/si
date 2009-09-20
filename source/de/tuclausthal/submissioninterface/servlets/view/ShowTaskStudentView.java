/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a task in student view
 * @author Sven Strickroth
 */
public class ShowTaskStudentView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Task task = (Task) request.getAttribute("task");
		Participation participation = (Participation) request.getAttribute("participation");

		template.printTemplateHeader(task);

		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		// HTML must be possible here
		out.println("<td>" + task.getDescription() + "&nbsp;</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Startdatum:</th>");
		out.println("<td>" + Util.mknohtml(task.getStart().toLocaleString()) + "</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Enddatum:</th>");
		out.println("<td>" + Util.mknohtml(task.getDeadline().toLocaleString()));
		if (task.getDeadline().before(new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60 * 1000))) {
			out.println(" Keine Abgabe mehr möglich");
		}
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Punkte:</th>");
		out.println("<td class=points>" + task.getMaxPoints() + "</td>");
		out.println("</tr>");
		out.println("</table>");

		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf();
		Submission submission = submissionDAO.getSubmission(task, new SessionAdapter(request).getUser());
		if (submission != null) {
			out.println("<p><h2>Informationen zu meiner Abgabe:</h2>");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Besteht aus:</th>");
			out.println("<td>");
			File path = new File(new ContextAdapter(getServletContext()).getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
			for (File file : path.listFiles()) {
				if (!file.getName().endsWith(".class")) {
					out.println("<a target=\"_blank\" href=\"" + response.encodeURL("ShowFile/" + file.getName() + "?sid=" + submission.getSubmissionid()) + "\">" + Util.mknohtml(file.getName()) + "</a>");
					if (task.getDeadline().after(new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60 * 1000))) {
						out.println(" (<a href=\"" + response.encodeURL("DeleteFile/" + file.getName() + "?sid=" + submission.getSubmissionid()) + "\">löschen</a>)<br>");
					}
				}
			}
			out.println("</td>");
			out.println("</tr>");
			if (task.getShowPoints().after(new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60 * 1000)) && submission.getPoints() != null) {
				out.println("<tr>");
				out.println("<th>Bewertung:</th>");
				out.println("<td>");
				out.println(submission.getPoints().getPoints() + " von " + task.getMaxPoints());
				out.println("</td>");
				out.println("</tr>");
			}
			out.println("</table>");
		}
		out.println("<p>");
		if (task.getDeadline().before(new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60 * 1000))) {
			out.println("<div class=mid>Keine Abgabe mehr möglich.</div>");
		} else {
			out.println("<div class=mid><a href=\"" + response.encodeURL("SubmitSolution?taskid=" + task.getTaskid()) + "\">Abgabe starten</a></div");
		}
		template.printTemplateFooter();
	}
}
