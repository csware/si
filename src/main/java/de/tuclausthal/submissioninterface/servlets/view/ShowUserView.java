/*
 * Copyright 2009-2013, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.ShowLecture;
import de.tuclausthal.submissioninterface.servlets.controller.ShowSubmission;
import de.tuclausthal.submissioninterface.servlets.controller.ShowTask;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying the startpage of the system
 * @author Sven Strickroth
 */
@GATEView
public class ShowUserView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		User user = (User) request.getAttribute("user");
		@SuppressWarnings("unchecked")
		List<Participation> participations = (List<Participation>) request.getAttribute("participations");
		Boolean isAtLeastAdvisorOnce = (Boolean) request.getAttribute("isAtLeastAdvisorOnce");
		Session session = RequestAdapter.getSession(request);

		template.printTemplateHeader("BenutzerIn \"" + Util.escapeHTML(user.getFullName()) + "\"");
		PrintWriter out = response.getWriter();
		out.println("<p><a href=\"mailto:" + Util.escapeHTML(user.getEmail()) + "\">" + Util.escapeHTML(user.getEmail()) + "</a></p>");

		if (user instanceof Student) {
			if (isAtLeastAdvisorOnce) {
				out.println("<p>Matrikelnummer: " + ((Student) user).getMatrikelno() + "</p>");
			}
			out.println("<p>Studiengang: " + Util.escapeHTML(((Student) user).getStudiengang()) + "</p>");
		}

		boolean titleShown = false;
		for (Participation participation : participations) {
			if (titleShown == false) {
				out.println("<h1>Vorlesungen</h1>");
				titleShown = true;
			}
			out.println("<h2><a href=\"" + Util.generateHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + participation.getLecture().getId(), response) + "\">" + Util.escapeHTML(participation.getLecture().getName()) + " (" + participation.getLecture().getReadableSemester() + ")</a></h2>");
			if (participation.getGroup() != null) {
				out.println("<p>Gruppe: " + Util.escapeHTML(participation.getGroup().getName()) + "</p>");
			}

			if (!participation.getRoleType().equals(ParticipationRole.NORMAL)) {
				continue;
			}

			List<Task> tasks = DAOFactory.TaskDAOIf(session).getTasks(participation.getLecture(), false).stream().filter(t -> t.getStart().isBefore(ZonedDateTime.now())).collect(Collectors.toList());
			List<Submission> submissions = DAOFactory.SubmissionDAOIf(session).getAllSubmissions(participation);
			if (tasks.isEmpty()) {
				out.println("<div class=mid>keine Aufgaben gefunden.</div>");
				continue;
			}

			out.println("<table>");
			out.println("<tr>");
			out.println("<th>Aufgabe</th>");
			out.println("<th>Max. Punkte</th>");
			out.println("<th>Meine Punkte</th>");
			out.println("</tr>");

			int points = 0;
			int maxPoints = 0;
			Iterator<Submission> submissionIterator = submissions.iterator();
			Submission submission = null;
			if (submissionIterator.hasNext()) {
				submission = submissionIterator.next();
			}
			TaskGroup lastTaskGroup = null;
			for (Task task : tasks) {
				if (lastTaskGroup == null || lastTaskGroup.getTaskGroupId() != task.getTaskGroup().getTaskGroupId()) {
					out.println("<tr>");
					out.println("<th colspan=3>Aufgabengruppe " + Util.escapeHTML(task.getTaskGroup().getTitle()) + "</th>");
					out.println("</tr>");
					lastTaskGroup = task.getTaskGroup();
				}
				out.println("<tr>");
				out.println("<td><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">" + Util.escapeHTML(task.getTitle()) + "</a></td>");
				out.println("<td class=points>" + Util.showPoints(task.getMaxPoints()) + "</td>");
				maxPoints += task.getMaxPoints();

				if (submission == null || submission.getTask().getTaskid() != task.getTaskid()) {
					out.println("<td class=points>nicht bearbeitet</td>");
					continue;
				}
				if (submission.getPoints() != null && submission.getPoints().getPointStatus() != PointStatus.NICHT_BEWERTET.ordinal()) {
					if (submission.getPoints().getPointsOk()) {
						out.println("<td class=\"points" + Util.getPointsCSSClass(submission.getPoints()) + "\"><a href=\"" + Util.generateHTMLLink(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">" + Util.showPoints(submission.getPoints().getPointsByStatus(task.getMinPointStep())));
						points += submission.getPoints().getPointsByStatus(task.getMinPointStep());
					} else {
						out.println("<td class=\"points" + Util.getPointsCSSClass(submission.getPoints()) + "\"><a href=\"" + Util.generateHTMLLink(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">(" + Util.showPoints(submission.getPoints().getPlagiarismPoints(task.getMinPointStep())) + ")");
					}
				} else {
					if (task.getDeadline().isAfter(ZonedDateTime.now())) {
						out.println("<td class=points><a href=\"" + Util.generateHTMLLink(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">(noch unbewertet)");
					} else {
						out.println("<td class=points><a href=\"" + Util.generateHTMLLink(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">noch unbewertet");
					}
				}
				if (submissionIterator.hasNext()) {
					submission = submissionIterator.next();
				}
				out.println("</a></td>");
				out.println("</tr>");
			}
			out.println("<tr>");
			out.println("<td colspan=3 style=\"height: 1px\"></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td><b>Gesamt:</b></td>");
			out.println("<td class=points>" + Util.showPoints(maxPoints) + "</td>");
			out.println("<td class=points>" + Util.showPoints(points) + "</td>");
			out.println("</tr>");
			out.println("</table>");
		}
		template.printTemplateFooter();
	}
}
