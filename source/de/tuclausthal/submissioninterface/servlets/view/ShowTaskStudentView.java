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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestCountDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
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

		Session session = HibernateSessionHelper.getSessionFactory().openSession();

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
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Punkte:</th>");
		out.println("<td class=points>" + task.getMaxPoints() + "</td>");
		out.println("</tr>");
		out.println("</table>");

		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(task, new SessionAdapter(request).getUser(session));
		if (submission != null) {
			out.println("<p><h2>Informationen zu meiner Abgabe:</h2>");
			out.println("<table class=border>");
			if (submission.getSubmitters().size() > 1) {
				out.println("<tr>");
				out.println("<th>Bearbeitet von:</th>");
				out.println("<td>");
				out.println(submission.getSubmitterNames().replaceAll(", ", "<br>"));
				out.println("</td>");
				out.println("</tr>");
			}
			out.println("<tr>");
			out.println("<th>Besteht aus:</th>");
			out.println("<td>");
			File path = new File(new ContextAdapter(getServletContext()).getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
			for (File file : path.listFiles()) {
				out.println("<a target=\"_blank\" href=\"" + response.encodeURL("ShowFile/" + file.getName() + "?sid=" + submission.getSubmissionid()) + "\">" + Util.mknohtml(file.getName()) + "</a>");
				if (task.getDeadline().after(Util.correctTimezone(new Date()))) {
					out.println(" (<a href=\"" + response.encodeURL("DeleteFile/" + file.getName() + "?sid=" + submission.getSubmissionid()) + "\">löschen</a>)");
				}
				out.println("<br>");
			}
			out.println("</td>");
			out.println("</tr>");
			if (task.getShowPoints().before(Util.correctTimezone(new Date())) && submission.getPoints() != null) {
				out.println("<tr>");
				out.println("<th>Bewertung:</th>");
				out.println("<td>");
				if (submission.getPoints().getPointsOk()) {
					out.println(submission.getPoints().getPoints() + " von " + task.getMaxPoints());
				} else {
					out.println("0 von " + task.getMaxPoints() + ", nicht vorgestellt");
				}
				out.println("</td>");
				out.println("</tr>");
				if (submission.getPoints().getComment() != null && !"".equals(submission.getPoints().getComment())) {
					out.println("<tr>");
					out.println("<th>Kommentar:</th>");
					out.println("<td>");
					out.println(Util.mkTextToHTML(submission.getPoints().getComment()));
					out.println("</td>");
					out.println("</tr>");
				}
			}
			out.println("</table>");

			out.println("<p>");
			if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
				out.println("<div class=mid>Keine Abgabe mehr möglich.</div>");
			} else {
				out.println("<div class=mid><a href=\"" + response.encodeURL("SubmitSolution?taskid=" + task.getTaskid()) + "\">Abgabe starten</a></div");
			}

			List<Test> tests = DAOFactory.TestDAOIf(session).getStudentTests(task);
			TestCountDAOIf testCountDAO = DAOFactory.TestCountDAOIf(session);
			if (tests.size() > 0 && task.getDeadline().after(Util.correctTimezone(new Date()))) {
				out.println("<p><h2>Mögliche Tests:</h2>");
				out.println("<table class=border>");
				for (Test test : tests) {
					out.println("<tr>");
					out.println("<th>" + Util.mknohtml(test.getTestTitle()) + "</th>");
					out.println("<td>");
					out.println(Util.mkTextToHTML(test.getTestDescription()));
					out.println("</td>");
					out.println("<td>");
					if (testCountDAO.canStillRunXTimes(test, participation.getUser()) > 0) {
						out.println("<a href=\"PerformTest?sid=" + submission.getSubmissionid() + "&amp;testid=" + test.getId() + "\">Test ausführen</a>");
					} else {
						out.println("Limit erreicht");
					}
					out.println("</td>");
					out.println("</tr>");
				}
				out.println("</table>");
			}
		} else {
			out.println("<p>");
			if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
				out.println("<div class=mid>Keine Abgabe mehr möglich.</div>");
			} else {
				out.println("<div class=mid><a href=\"" + response.encodeURL("SubmitSolution?taskid=" + task.getTaskid()) + "\">Abgabe starten</a></div");
			}
		}
		template.printTemplateFooter();
	}
}
