/*
 * Copyright 2009-2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a lecture in student view
 * @author Sven Strickroth
 */
public class ShowLectureStudentView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Participation participation = (Participation) request.getAttribute("participation");
		Lecture lecture = participation.getLecture();
		@SuppressWarnings("unchecked")
		List<Group> joinAbleGroups = (List<Group>) request.getAttribute("joinAbleGroups");
		@SuppressWarnings("unchecked")
		List<Task> tasks = (List<Task>) request.getAttribute("tasks");
		@SuppressWarnings("unchecked")
		List<Submission> submissions = (List<Submission>) request.getAttribute("submissions");

		// list all tasks for a lecture
		template.printTemplateHeader(lecture);
		PrintWriter out = response.getWriter();

		boolean canJoinGroup = !joinAbleGroups.isEmpty();

		out.println("<div class=mid>");
		if (participation.getGroup() != null) {
			out.print("Meine Gruppe: ");
			if (participation.getGroup().isMembersVisibleToStudents()) {
				out.print("<a href=\"" + Util.generateHTMLLink("ShowGroup?lecture=" + lecture.getId(), response) + "\">");
			}
			out.print(Util.escapeHTML(participation.getGroup().getName()));
			if (participation.getGroup().isMembersVisibleToStudents()) {
				out.println("</a>");
			}
			if (participation.getGroup().getTutors() != null && !participation.getGroup().getTutors().isEmpty()) {
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
					out.print("<a href=\"mailto:" + Util.escapeHTML(tutor.getUser().getEmail()) + "\">" + Util.escapeHTML(tutor.getUser().getFullName()) + "</a>");
				}
			}
		} else if (canJoinGroup) {
			out.println("Sie sind derzeit in keiner Gruppe.");
		}
		if (canJoinGroup) {
			out.println("<form method=post action=\"" + Util.generateHTMLLink("JoinGroup", response) + "\">");
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

		if (!tasks.isEmpty()) {
			int points = 0;
			int maxPoints = 0;

			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Aufgabe</th>");
			out.println("<th>Max. Punkte</th>");
			out.println("<th>Meine Punkte</th>");
			out.println("</tr>");

			Iterator<Submission> submissionIterator = submissions.iterator();
			Submission submission = null;
			if (submissionIterator.hasNext()) {
				submission = submissionIterator.next();
			}
			TaskGroup lastTaskGroup = null;
			for (Task task : tasks) {
				if (!task.getStart().before(Util.correctTimezone(new Date()))) {
					continue;
				}
				if (lastTaskGroup == null || lastTaskGroup.getTaskGroupId() != task.getTaskGroup().getTaskGroupId()) {
					out.println("<tr>");
					out.println("<th colspan=3>Aufgabengruppe " + Util.escapeHTML(task.getTaskGroup().getTitle()) + "</th>");
					out.println("</tr>");
					lastTaskGroup = task.getTaskGroup();
				}
				maxPoints += task.getMaxPoints();
				out.println("<tr>");
				out.println("<td><a href=\"" + Util.generateHTMLLink("ShowTask?taskid=" + task.getTaskid(), response) + "\">" + Util.escapeHTML(task.getTitle()) + "</a></td>");
				out.println("<td class=points>" + Util.showPoints(task.getMaxPoints()) + "</td>");

				if (submission != null && submission.getTask().getTaskid() == task.getTaskid()) {
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
					if (submissionIterator.hasNext()) {
						submission = submissionIterator.next();
					}
				} else if (task.getDeadline().after(Util.correctTimezone(new Date()))) {
					out.println("<td class=points>(noch) nicht bearbeitet</td>");
				} else {
					out.println("<td class=points>nicht bearbeitet</td>");
				}
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
		} else {
			out.println("<div class=mid>Es wurden noch keine Aufgaben angelegt oder für Sie sind noch keine Aufgaben sichtbar.</div>");
		}
		template.printTemplateFooter();
	}
}
