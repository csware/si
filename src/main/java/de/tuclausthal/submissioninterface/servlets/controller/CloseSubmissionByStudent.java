/*
 * Copyright 2020 Sven Strickroth <email@cs-ware.de>
 * Copyright 2019 Dustin Reineke <dustin.reineke@tu-clausthal.de>
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller to finally close a submission by a student. Students can close
 * the submitted solution before the deadline ends, to enable the grading
 * options for the tutors and advisors.
 */
public class CloseSubmissionByStudent extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		RequestAdapter requestAdapter = new RequestAdapter(request);
		Session session = requestAdapter.getSession();

		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(Util.parseInteger(request.getParameter("sid"), 0));
		if (submission == null) {
			request.setAttribute("title", "Abgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		Participation participation = DAOFactory.ParticipationDAOIf(session).getParticipation(requestAdapter.getUser(), submission.getTask().getTaskGroup().getLecture());
		if (participation == null || !submission.getSubmitters().contains(participation)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (!submission.getTask().isAllowPrematureSubmissionClosing()) {
			request.setAttribute("title", "Ungültige Anfrage");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		Transaction tx = session.beginTransaction();
		session.buildLockRequest(LockOptions.UPGRADE).lock(submission);
		if (submission.getTask().getDeadline().before(Util.correctTimezone(new Date())) || submission.isClosed()) {
			tx.rollback();
			request.setAttribute("title", "An dieser Abgabe sind keine Veränderungen mehr möglich.");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		// update submission information
		submission.setClosedTime(new Date());
		submission.setClosedBy(participation);

		// update submission into database
		session.update(submission);
		tx.commit();

		response.sendRedirect(response.encodeURL("ShowTask?taskid=" + submission.getTask().getTaskid()));
	}
}
