/*
 * Copyright 2009 - 2011 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointHistory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for marking points as approved
 * @author Sven Strickroth
 */
public class MarkApproved extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.getStart().after(Util.correctTimezone(new Date())) && task.getDeadline().before(Util.correctTimezone(new Date())) && participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		Transaction tx = session.beginTransaction();
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);

		Enumeration<String> parameterNameEnumerator = request.getParameterNames();
		while (parameterNameEnumerator.hasMoreElements()) {
			String parameterName = parameterNameEnumerator.nextElement();
			if (parameterName.matches("sid[0-9]+")) {
				Submission submission = submissionDAO.getSubmissionLocked(Util.parseInteger(parameterName.substring(3), 0));
				if (submission != null && submission.getTask().getTaskid() == task.getTaskid() && submission.getPoints() != null && submission.getPoints().getPoints() != null && submission.getPoints().getPointStatus() != null && submission.getPoints().getPointStatus() == PointStatus.NICHT_ABGENOMMEN.ordinal()) {
					submission.getPoints().setPointStatus(PointStatus.ABGENOMMEN);
					submission.getPoints().setIssuedBy(participation);
					session.save(submission);
					// TODO: Attention! Manual update
					PointHistory ph = new PointHistory(submission, "status", PointStatus.NICHT_ABGENOMMEN.toString(), PointStatus.ABGENOMMEN.toString(), participation);
					session.save(ph);
				}
			}
		}

		tx.commit();

		response.sendRedirect(response.encodeRedirectURL("ShowTask?taskid=" + task.getTaskid()));
	}
}
