/*
 * Copyright 2009-2011, 2013, 2020-2022, 2024 Sven Strickroth <email@cs-ware.de>
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.opencsv.CSVWriter;

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
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for exporting lecture points of all participants as CSV
 * @author Sven Strickroth
 */
@GATEView
public class ShowLectureTutorCSVView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Participation participation = (Participation) request.getAttribute("participation");
		Lecture lecture = participation.getLecture();
		Session session = RequestAdapter.getSession(request);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);

		boolean showMatNo = Configuration.getInstance().isMatrikelNoAvailable() && (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0 || Configuration.getInstance().isMatrikelNoAvailableToTutors());

		response.setContentType("text/csv");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=export.csv");

		List<TaskGroup> taskGroupList = lecture.getTaskGroups();

		final String[] empty = new String[0];
		try (CSVWriter writer = new CSVWriter(new PrintWriter(response.getWriter()), ';', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
			List<String> header = new ArrayList<>();
			if (showMatNo) {
				header.add("MatrikelNo");
			}
			header.add("Studiengang");
			header.add("Nachname");
			header.add("Vorname");
			header.add("eMail");
			for (TaskGroup taskGroup : taskGroupList) {
				List<Task> taskList = taskGroup.getTasks();
				for (Task task : taskList) {
					header.add(task.getTitle() + " (Pkts: " + Util.showPoints(task.getMaxPoints()) + ")");
				}
			}
			header.add("Gesamt");
			writer.writeNext(header.toArray(empty), false);

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
				String[] line = new String[header.size()];
				int column = 0;
				if (lectureParticipation.getUser() instanceof Student student) {
					if (showMatNo) {
						line[column++] = String.valueOf(student.getMatrikelno());
					}
					line[column++] = student.getStudiengang();
				} else {
					if (showMatNo) {
						line[column++] = "n/a;";
					}
					line[column++] = "n/a;";
				}
				line[column++] = lectureParticipation.getUser().getLastName();
				line[column++] = lectureParticipation.getUser().getFirstName();
				line[column++] = lectureParticipation.getUser().getEmail();
				int points = 0;
				boolean noshow = true;
				for (TaskGroup taskGroup : taskGroupList) {
					List<Task> taskList = taskGroup.getTasks();
					for (Task task : taskList) {
						if (submission != null && submission.getTask() == task) {
							noshow = false;
							if (submission.getPoints() != null && submission.getPoints().getPointStatus() != PointStatus.NICHT_BEWERTET.ordinal()) {
								if (submission.getPoints().getPointsOk()) {
									line[column++] = Util.showPoints(submission.getPoints().getPointsByStatus(task.getMinPointStep()));
									points += submission.getPoints().getPointsByStatus(task.getMinPointStep());
								} else {
									line[column++] = "(" + Util.showPoints(submission.getPoints().getPlagiarismPoints(task.getMinPointStep())) + ")";
								}
							} else {
								line[column++] = "n.b.";
							}
							if (submissionIterator.hasNext()) {
								submission = submissionIterator.next();
							}
						} else {
							line[column++] = "k.A.";
						}
					}
				}
				if (!noshow) {
					line[column++] = Util.showPoints(points);
				} else {
					line[column++] = "n/a";
				}
				writer.writeNext(line, false);
			}
			writer.flush();
		}
	}
}
