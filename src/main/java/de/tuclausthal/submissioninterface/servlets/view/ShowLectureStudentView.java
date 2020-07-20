/*
 * Copyright 2009-2012, 2020 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a lecture in student view
 * @author Sven Strickroth
 */
public class ShowLectureStudentView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Participation participation = (Participation) request.getAttribute("participation");
		Lecture lecture = participation.getLecture();
		List<Group> joinAbleGroups = (List<Group>) request.getAttribute("joinAbleGroups");

		// list all tasks for a lecture
		template.printTemplateHeader(lecture);
		PrintWriter out = response.getWriter();

		boolean canJoinGroup = (joinAbleGroups != null && joinAbleGroups.size() > 0);

		out.println("<div class=mid>");
		if (participation.getGroup() != null) {
			out.println("Meine Gruppe: " + Util.escapeHTML(participation.getGroup().getName()));
			if (participation.getGroup().getTutors() != null && participation.getGroup().getTutors().size() > 0) {
				if (participation.getGroup().getTutors().size() > 1) {
					out.println("<br>Meine TutorInnen: ");
				} else {
					out.println("<br>Mein(e) TutorIn: ");
				}
				boolean isFirst = true;
				for (Participation tutor : participation.getGroup().getTutors()) {
					if (!isFirst) {
						out.print(", ");
					}
					isFirst = false;
					out.print("<a href=\"mailto:" + Util.escapeHTML(tutor.getUser().getFullEmail()) + "\">" + Util.escapeHTML(tutor.getUser().getFullName()) + "</a>");
				}
			}
		} else if (canJoinGroup) {
			out.println("Sie sind derzeit in keiner Gruppe.");
		}
		if (canJoinGroup) {
			out.println("<form action=\"" + response.encodeURL("JoinGroup") + "\">");
			out.println("<select name=groupid>");
			for (Group group : joinAbleGroups) {
				out.println("<option value=" + group.getGid() + ">" + Util.escapeHTML(group.getName()));
			}
			out.println("</select>");
			if (participation.getGroup() != null) {
				out.println("<input type=submit value=\"Gruppe wechseln\">");
			} else {
				out.println("<input type=submit value=\"Gruppe beitreten\">");
			}
			out.println("</form>");
		}
		out.println("</div><p>");

		Iterator<TaskGroup> taskGroupIterator = lecture.getTaskGroups().iterator();
		if (taskGroupIterator.hasNext()) {
			int points = 0;
			int maxPoints = 0;

			boolean isStartedTable = false;
			while (taskGroupIterator.hasNext()) {
				TaskGroup taskGroup = taskGroupIterator.next();
				Iterator<Task> taskIterator = taskGroup.getTasks().iterator();
				if (taskIterator.hasNext()) {
					boolean isStartedTaskgroup = false;
					while (taskIterator.hasNext()) {
						Task task = taskIterator.next();
						if (task.getStart().before(Util.correctTimezone(new Date()))) {
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
							maxPoints += task.getMaxPoints();
							out.println("<tr>");
							out.println("<td><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">" + Util.escapeHTML(task.getTitle()) + "</a></td>");
							out.println("<td class=points>" + Util.showPoints(task.getMaxPoints()) + "</td>");
							Submission submission = DAOFactory.SubmissionDAOIf(RequestAdapter.getSession(request)).getSubmission(task, RequestAdapter.getUser(request));
							if (submission != null) {
								if (submission.isPointsVisibleToStudents()) {
									if (submission.getPoints().getPointStatus() == PointStatus.ABGENOMMEN.ordinal()) {
										out.println("<td class=points>" + Util.showPoints(submission.getPoints().getPointsByStatus(task.getMinPointStep())) + "</td>");
										points += submission.getPoints().getPointsByStatus(task.getMinPointStep());
									} else if (submission.getPoints().getPointStatus() == PointStatus.ABGENOMMEN_FAILED.ordinal()) {
										out.println("<td class=points>0, Abnahme nicht bestanden</td>");
									} else {
										out.println("<td class=points>0, nicht abgenommen</td>");
									}
								} else {
									out.println("<td class=points>noch unbewertet</td>");
								}
							} else if (task.getDeadline().after(Util.correctTimezone(new Date()))) {
								out.println("<td class=points>(noch) nicht bearbeitet</td>");
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
		template.printTemplateFooter();
	}
}
