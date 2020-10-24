/*
 * Copyright 2009-2011, 2013, 2020 Sven Strickroth <email@cs-ware.de>
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

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a lecture in tutor/advisor view
 * @author Sven Strickroth
 */
public class ShowLectureTutorCSVView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Participation participation = (Participation) request.getAttribute("participation");
		Lecture lecture = participation.getLecture();
		Session session = RequestAdapter.getSession(request);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);

		boolean showMatNo = (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) || new ContextAdapter(getServletContext()).isMatrikelNoAvailableToTutors();

		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=export.csv");

		PrintWriter out = response.getWriter();

		List<TaskGroup> taskGroupList = lecture.getTaskGroups();

		out.print("Teilnahme;");
		if (showMatNo) {
			out.print("MatrikelNo;");
		}
		out.print("Studiengang;Nachname;Vorname;eMail;");

		for (TaskGroup taskGroup : taskGroupList) {
			List<Task> taskList = taskGroup.getTasks();
			for (Task task : taskList) {
				out.print(Util.csvQuote(task.getTitle()) + " (Pkts: " + Util.showPoints(task.getMaxPoints()) + ")" + ";");
			}
		}
		out.println("Gesamt");

		for (Participation lectureParticipation : DAOFactory.ParticipationDAOIf(session).getLectureParticipations(lecture)) {
			out.print(lectureParticipation.getRoleType().toString() + ";");
			if (lectureParticipation.getUser() instanceof Student) {
				if (showMatNo) {
					out.print(((Student) lectureParticipation.getUser()).getMatrikelno() + ";");
				}
				out.print(Util.csvQuote(((Student) lectureParticipation.getUser()).getStudiengang()) + ";");
			} else {
				if (showMatNo) {
					out.print("n/a;");
				}
				out.print("n/a;");
			}
			out.print(Util.csvQuote(lectureParticipation.getUser().getLastName()) + ";");
			out.print(Util.csvQuote(lectureParticipation.getUser().getFirstName()) + ";");
			out.print(Util.csvQuote(lectureParticipation.getUser().getEmail()));
			if (taskGroupList.size() > 0) {
				out.print(";");
			}
			int points = 0;
			for (TaskGroup taskGroup : taskGroupList) {
				List<Task> taskList = taskGroup.getTasks();
				for (Task task : taskList) {
					Submission submission = submissionDAO.getSubmission(task, lectureParticipation.getUser());
					if (submission != null) {
						if (submission.getPoints() != null && submission.getPoints().getPointStatus() != PointStatus.NICHT_BEWERTET.ordinal()) {
							if (submission.getPoints().getPointsOk()) {
								out.print(Util.showPoints(submission.getPoints().getPointsByStatus(task.getMinPointStep())) + ";");
								points += submission.getPoints().getPointsByStatus(task.getMinPointStep());
							} else {
								out.print("(" + Util.showPoints(submission.getPoints().getPlagiarismPoints(task.getMinPointStep())) + ");");
							}
						} else {
							out.print("n.b.;");
						}
					} else {
						out.print("k.A.;");
					}
				}
			}
			if (points > 0) {
				out.println(Util.showPoints(points));
			} else {
				out.println("n/a");
			}
		}
	}
}
