/*
 * Copyright 2020-2023, 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2019 Dustin Reineke <dustin.reineke@tu-clausthal.de>
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

import jakarta.persistence.LockModeType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.CloseSubmissionView;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller to finally close a submission by a student. Students can close
 * the submitted solution before the deadline ends, to enable the grading
 * options for the tutors and advisors.
 */
@GATEController
public class CloseSubmissionByStudent extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		RequestAdapter requestAdapter = new RequestAdapter(request);
		Session session = requestAdapter.getSession();

		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(Util.parseInteger(request.getParameter("sid"), 0));
		if (submission == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Abgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		Participation participation = DAOFactory.ParticipationDAOIf(session).getParticipation(requestAdapter.getUser(), submission.getTask().getTaskGroup().getLecture());
		if (participation == null || !submission.getSubmitters().contains(participation)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (!submission.getTask().isAllowPrematureSubmissionClosing()) {
			request.setAttribute("title", "Ungültige Anfrage");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		if (submission.getTask().getDeadline().isBefore(ZonedDateTime.now()) || submission.isClosed()) {
			request.setAttribute("title", "An dieser Abgabe sind keine Veränderungen mehr möglich.");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		request.setAttribute("submission", submission);
		getServletContext().getNamedDispatcher(CloseSubmissionView.class.getSimpleName()).forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		RequestAdapter requestAdapter = new RequestAdapter(request);
		Session session = requestAdapter.getSession();

		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(Util.parseInteger(request.getParameter("sid"), 0));
		if (submission == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Abgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		Participation participation = DAOFactory.ParticipationDAOIf(session).getParticipation(requestAdapter.getUser(), submission.getTask().getTaskGroup().getLecture());
		if (participation == null || !submission.getSubmitters().contains(participation)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (!submission.getTask().isAllowPrematureSubmissionClosing()) {
			request.setAttribute("title", "Ungültige Anfrage");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		Transaction tx = session.beginTransaction();
		session.refresh(submission, LockModeType.PESSIMISTIC_WRITE);
		if (submission.getTask().getDeadline().isBefore(ZonedDateTime.now()) || submission.isClosed()) {
			tx.rollback();
			request.setAttribute("title", "An dieser Abgabe sind keine Veränderungen mehr möglich.");
			request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + submission.getTask().getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		// update submission information
		submission.setClosedTime(ZonedDateTime.now());
		submission.setClosedBy(participation);

		tx.commit();

		response.sendRedirect(Util.generateRedirectURL(ShowTask.class.getSimpleName() + "?taskid=" + submission.getTask().getTaskid(), response));
	}
}
