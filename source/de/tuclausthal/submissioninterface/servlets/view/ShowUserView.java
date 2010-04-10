/*
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
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

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying the startpage of the system
 * @author Sven Strickroth
 */
public class ShowUserView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		User user = (User) request.getAttribute("user");
		List<Lecture> lectures = (List<Lecture>) request.getAttribute("lectures");
		Session session = (Session) request.getAttribute("session");

		template.printTemplateHeader("Benutzer \"" + Util.mknohtml(user.getFullName()) + "\"");

		out.println("<p><a href=\"mailto:" + Util.mknohtml(user.getEmail()) + "@tu-clausthal.de\">" + Util.mknohtml(user.getEmail()) + "@tu-clausthal.de</a></p>");

		if (user instanceof Student) {
			out.println("<p>Matrikelnummer: " + ((Student) user).getMatrikelno() + "</p>");
		}

		out.println("<h1>Vorlesungen</h1>");
		for (Participation participation : user.getLectureParticipant()) {
			if (participation.getRoleType() == ParticipationRole.NORMAL && lectures.contains(participation.getLecture())) {
				out.println("<h2><a href=\"" + response.encodeURL("ShowLecture?lecture=" + participation.getLecture().getId()) + "\">" + Util.mknohtml(participation.getLecture().getName()) + "</a></h2>");
				int points = 0;
				int maxPoints = 0;
				Iterator<Task> taskIterator = participation.getLecture().getTasks().iterator();
				if (taskIterator.hasNext()) {
					out.println("<table class=border>");
					out.println("<tr>");
					out.println("<th>Aufgabe</th>");
					out.println("<th>Max. Punkte</th>");
					out.println("<th>Vergebene Punkte</th>");
					out.println("</tr>");
					while (taskIterator.hasNext()) {
						Task task = taskIterator.next();
						out.println("<tr>");
						out.println("<td><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">" + Util.mknohtml(task.getTitle()) + "</a></td>");
						out.println("<td class=points>" + task.getMaxPoints() + "</td>");
						maxPoints += task.getMaxPoints();
						Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(task, user);
						if (submission != null) {
							if (submission.getPoints() != null) {
								if (submission.getPoints().getPointsOk()) {
									out.println("<td class=points><a href=\"ShowSubmission?sid=" + submission.getSubmissionid() + "\">" + submission.getPoints().getPoints() + "");
									points += submission.getPoints().getPoints();
								} else {
									out.println("<td class=points><a href=\"ShowSubmission?sid=" + submission.getSubmissionid() + "\">(" + submission.getPoints().getPoints() + ")");
								}
							} else {
								out.println("<td><a href=\"ShowSubmission?sid=" + submission.getSubmissionid() + "\">noch unbewertet");
							}
							out.println("</a></td>");
						} else {
							out.println("<td>nicht bearbeitet</td>");
						}
						out.println("</tr>");
					}
					out.println("<tr>");
					out.println("<td>Gesamt:</td>");
					out.println("<td class=points>" + maxPoints + "</td>");
					out.println("<td class=points>" + points + "</td>");
					out.println("</tr>");
					out.println("</table>");
				} else {
					out.println("<div class=mid>keine Aufgaben gefunden.</div>");
				}
			}
		}
		template.printTemplateFooter();
	}
}
