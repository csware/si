/*
 * Copyright 2009-2011, 2020-2023 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.controller;

import java.io.IOException;
import java.time.ZonedDateTime;
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
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for marking points as approved
 * @author Sven Strickroth
 */
@GATEController
public class MarkApproved extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.getStart().isAfter(ZonedDateTime.now()) && task.getDeadline().isBefore(ZonedDateTime.now()) && participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
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
					// TODO: Attention! Manual update
					PointHistory ph = new PointHistory(submission, "status", PointStatus.NICHT_ABGENOMMEN.toString(), PointStatus.ABGENOMMEN.toString(), participation);
					session.persist(ph);
				}
			}
		}

		tx.commit();

		response.sendRedirect(Util.generateRedirectURL(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response));
	}
}
