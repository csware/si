/*
 * Copyright 2009-2013, 2020-2025 Sven Strickroth <email@cs-ware.de>
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
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.ShowUser;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a lecture in tutor/advisor view
 * @author Sven Strickroth
 */
@GATEView
public class ShowLectureTutorFullView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Participation participation = (Participation) request.getAttribute("participation");
		Lecture lecture = participation.getLecture();
		Session session = RequestAdapter.getSession(request);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);

		boolean showMatNo = Configuration.getInstance().isMatrikelNoAvailable() && (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0 || Configuration.getInstance().isMatrikelNoAvailableToTutors());

		// list all tasks for a lecture
		template.printTemplateHeader("Gesamtübersicht", lecture);
		PrintWriter out = response.getWriter();

		List<TaskGroup> taskGroupList = lecture.getTaskGroups();

		out.println("<table>");
		out.println("<thead>");
		out.println("<tr>");
		if (showMatNo) {
			out.println("<th rowspan=2>MatrikelNo</th>");
		}
		out.println("<th rowspan=2>Studiengang</th>");
		out.println("<th rowspan=2>Nachname</th>");
		out.println("<th rowspan=2>Vorname</th>");
		for (TaskGroup taskGroup : taskGroupList) {
			List<Task> taskList = taskGroup.getTasks();
			if (!taskList.isEmpty()) {
				out.println("<th colspan=" + taskList.size() + ">" + Util.escapeHTML(taskGroup.getTitle()) + "</th>");
			}
		}
		out.println("<th rowspan=2>Gesamt</th>");
		out.println("</tr>");

		out.println("<tr>");
		for (TaskGroup taskGroup : taskGroupList) {
			List<Task> taskList = taskGroup.getTasks();
			for (Task task : taskList) {
				out.println("<th>" + Util.escapeHTML(task.getTitle()) + "<br>Pkts: " + Util.showPoints(task.getMaxPoints()) + "</th>");
			}
		}
		out.println("</tr>");
		out.println("</thead>");

		for (Participation lectureParticipation : DAOFactory.ParticipationDAOIf(session).getLectureParticipationsOrderedByName(lecture)) {
			if (lectureParticipation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
				continue;
			}
			List<Submission> studentSubmissions = submissionDAO.getAllSubmissions(lectureParticipation);
			Iterator<Submission> submissionIterator = studentSubmissions.iterator();
			Submission submission = null;
			if (submissionIterator.hasNext()) {
				submission = submissionIterator.next();
			}
			out.println("<tr>");
			if (lectureParticipation.getUser() instanceof Student student) {
				if (showMatNo) {
					out.println("<td>" + student.getMatrikelno() + "</td>");
				}
				out.println("<td>" + Util.escapeHTML(student.getStudiengang()) + "</td>");
			} else {
				if (showMatNo) {
					out.println("<td>n/a</td>");
				}
				out.println("<td>n/a</td>");
			}
			out.println("<td><a href=\"" + Util.generateHTMLLink(ShowUser.class.getSimpleName() + "?uid=" + lectureParticipation.getUser().getUid(), response) + "\">" + Util.escapeHTML(lectureParticipation.getUser().getLastName()) + "</a></td>");
			out.println("<td><a href=\"" + Util.generateHTMLLink(ShowUser.class.getSimpleName() + "?uid=" + lectureParticipation.getUser().getUid(), response) + "\">" + Util.escapeHTML(lectureParticipation.getUser().getFirstName()) + "</a></td>");
			int points = 0;
			boolean noshow = true;
			for (TaskGroup taskGroup : taskGroupList) {
				List<Task> taskList = taskGroup.getTasks();
				for (Task task : taskList) {
					if (submission != null && submission.getTask() == task) {
						noshow = false;
						if (submission.getPoints() != null && submission.getPoints().getPointStatus() != PointStatus.NICHT_BEWERTET.ordinal()) {
							if (submission.getPoints().getPointsOk()) {
								out.println("<td class=points>" + Util.showPoints(submission.getPoints().getPointsByStatus(task.getMinPointStep())) + "</td>");
								points += submission.getPoints().getPointsByStatus(task.getMinPointStep());
							} else {
								out.println("<td class=points>(" + Util.showPoints(submission.getPoints().getPlagiarismPoints(task.getMinPointStep())) + ")</td>");
							}
						} else {
							out.println("<td><span title=\"nicht bewertet\">n.b.</span></td>");
						}
						if (submissionIterator.hasNext()) {
							submission = submissionIterator.next();
						}
					} else {
						out.println("<td><span title=\"keine Abgabe des Studierenden\">k.A.</span></td>");
					}
				}
			}
			if (!noshow) {
				out.println("<td class=points>" + Util.showPoints(points) + "</td>");
			} else {
				out.println("<td>n/a</td>");
			}
			out.println("</tr>");
		}

		out.println("</table>");
		template.printTemplateFooter();
	}
}
