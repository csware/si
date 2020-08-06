/*
 * Copyright 2009-2013, 2020 Sven Strickroth <email@cs-ware.de>
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
import java.util.Date;
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
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
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

		User user = (User) request.getAttribute("user");
		List<Lecture> lectures = (List<Lecture>) request.getAttribute("lectures");
		Boolean isAtLeastAdvisorOnce = (Boolean) request.getAttribute("isAtLeastAdvisorOnce");
		Session session = RequestAdapter.getSession(request);

		template.printTemplateHeader("BenutzerIn \"" + Util.escapeHTML(user.getFullName()) + "\"");
		PrintWriter out = response.getWriter();
		out.println("<p><a href=\"mailto:" + Util.escapeHTML(user.getFullEmail()) + "\">" + Util.escapeHTML(user.getFullEmail()) + "</a></p>");

		if (user instanceof Student) {
			if (isAtLeastAdvisorOnce) {
				out.println("<p>Matrikelnummer: " + ((Student) user).getMatrikelno() + "</p>");
			}
			out.println("<p>Studiengang: " + Util.escapeHTML(((Student) user).getStudiengang()) + "</p>");
		}

		boolean titleShown = false;
		for (Participation participation : user.getLectureParticipant()) {
			if (participation.getRoleType() == ParticipationRole.NORMAL && lectures.contains(participation.getLecture())) {
				if (titleShown == false) {
					out.println("<h1>Vorlesungen</h1>");
					titleShown = true;
				}
				out.println("<h2><a href=\"" + response.encodeURL("ShowLecture?lecture=" + participation.getLecture().getId()) + "\">" + Util.escapeHTML(participation.getLecture().getName()) + " (" + participation.getLecture().getReadableSemester() + ")</a></h2>");
				if (participation.getGroup() != null) {
					out.println("<p>Gruppe: " + Util.escapeHTML(participation.getGroup().getName()) + "</p>");
				}

				int points = 0;
				int maxPoints = 0;

				Iterator<TaskGroup> taskGroupIterator = participation.getLecture().getTaskGroups().iterator();
				if (taskGroupIterator.hasNext()) {
					boolean isStartedTable = false;
					while (taskGroupIterator.hasNext()) {
						TaskGroup taskGroup = taskGroupIterator.next();
						Iterator<Task> taskIterator = taskGroup.getTasks().iterator();
						if (taskIterator.hasNext()) {
							boolean isStartedTaskgroup = false;
							while (taskIterator.hasNext()) {
								Task task = taskIterator.next();
								if (task.getStart().before(Util.correctTimezone(new Date()))) { // doesn't make sense to check for tutors, since the students cannot see the task
									if (!isStartedTable) {
										isStartedTable = true;
										out.println("<table class=border>");
										out.println("<tr>");
										out.println("<th>Aufgabe</th>");
										out.println("<th>Max. Punkte</th>");
										out.println("<th>Meine Punkte</th>");
										out.println("</tr>");
									}
									if (!isStartedTaskgroup && taskGroup.getTitle() != null) {
										isStartedTaskgroup = true;
										out.println("<tr>");
										out.println("<th colspan=3>Aufgabengruppe " + Util.escapeHTML(taskGroup.getTitle()) + "</th>");
										out.println("</tr>");
									}
									out.println("<tr>");
									out.println("<td><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">" + Util.escapeHTML(task.getTitle()) + "</a></td>");
									out.println("<td class=points>" + Util.showPoints(task.getMaxPoints()) + "</td>");
									maxPoints += task.getMaxPoints();
									Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(task, user);
									if (submission != null) {
										if (submission.getPoints() != null && submission.getPoints().getPointStatus() != PointStatus.NICHT_BEWERTET.ordinal()) {
											if (submission.getPoints().getPointsOk()) {
												out.println("<td class=\"points" + Util.getPointsCSSClass(submission.getPoints()) + "\"><a href=\"" + response.encodeURL("ShowSubmission?sid=" + submission.getSubmissionid()) + "\">" + Util.showPoints(submission.getPoints().getPointsByStatus(task.getMinPointStep())) + "");
												points += submission.getPoints().getPointsByStatus(task.getMinPointStep());
											} else {
												out.println("<td class=\"points" + Util.getPointsCSSClass(submission.getPoints()) + "\"><a href=\"" + response.encodeURL("ShowSubmission?sid=" + submission.getSubmissionid()) + "\">(" + Util.showPoints(submission.getPoints().getPlagiarismPoints(task.getMinPointStep())) + ")");
											}
										} else {
											if (task.getDeadline().after(Util.correctTimezone(new Date()))) {
												out.println("<td class=points><a href=\"" + response.encodeURL("ShowSubmission?sid=" + submission.getSubmissionid()) + "\">(noch unbewertet)");
											} else {
												out.println("<td class=points><a href=\"" + response.encodeURL("ShowSubmission?sid=" + submission.getSubmissionid()) + "\">noch unbewertet");
											}
										}
										out.println("</a></td>");
									} else {
										out.println("<td class=points>nicht bearbeitet</td>");
									}
									out.println("</tr>");
								}
							}
						}
					}
					if (isStartedTable) {
						out.println("<tr>");
						out.println("<td colspan=3 style=\"height: 1px\"></td>");
						out.println("</tr>");
						out.println("<tr>");
						out.println("<td><b>Gesamt:</b></td>");
						out.println("<td class=points>" + Util.showPoints(maxPoints) + "</td>");
						out.println("<td class=points>" + Util.showPoints(points) + "</td>");
						out.println("</tr>");
						out.println("</table>");
					} else {
						out.println("<div class=mid>keine Aufgaben gefunden.</div>");
					}
				} else {
					out.println("<div class=mid>keine Aufgaben gefunden.</div>");
				}
			}
		}
		template.printTemplateFooter();
	}
}
