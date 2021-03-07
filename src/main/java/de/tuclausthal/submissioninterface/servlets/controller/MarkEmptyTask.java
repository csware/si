/*
 * Copyright 2009-2011, 2017, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.PointsDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for displaying a lecture
 * loads a lecture and differs between the student and tutor view
 * @author Sven Strickroth
 *
 */
public class MarkEmptyTask extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		Task task = DAOFactory.TaskDAOIf(session).getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0 || (task.isShowTextArea() == true || !"-".equals(task.getFilenameRegexp()))) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("participations", DAOFactory.ParticipationDAOIf(session).getParticipationsWithNoSubmissionToTaskOrdered(task));

		request.setAttribute("task", task);
		getServletContext().getNamedDispatcher("MarkEmptyTaskView").forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		Task task = DAOFactory.TaskDAOIf(session).getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0 || (task.isShowTextArea() == true || !"-".equals(task.getFilenameRegexp()))) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (!Util.isInteger(request.getParameter("pid"))) {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "invalid request");
			return;
		}

		Participation studentParticipation = DAOFactory.ParticipationDAOIf(session).getParticipation(Util.parseInteger(request.getParameter("pid"), 0));
		if (studentParticipation == null || studentParticipation.getLecture().getId() != participation.getLecture().getId() || studentParticipation.getRoleType().compareTo(ParticipationRole.NORMAL) != 0) {
			request.setAttribute("title", "Gewählte Person ist kein normaler Teilnehmer der Vorlesung.");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}
		Transaction tx = session.beginTransaction();
		session.buildLockRequest(LockOptions.UPGRADE).lock(studentParticipation);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(task, studentParticipation.getUser());
		if (submission != null) {
			tx.commit();
			request.setAttribute("title", "Es existiert bereits eine Bewertung für diesen Studierenden: < href=\"" + Util.generateHTMLLink("ShowSubmission?sid=" + submission.getSubmissionid(), response) + "\">zur Bewertung</a>");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}
		submission = submissionDAO.createSubmission(task, studentParticipation);
		PointsDAOIf pointsDAO = DAOFactory.PointsDAOIf(session);
		String publicComment = "";
		if (request.getParameter("publiccomment") != null) {
			publicComment = request.getParameter("publiccomment");
		}
		String internalComment = "";
		if (request.getParameter("internalcomment") != null) {
			internalComment = request.getParameter("internalcomment");
		}
		PointStatus pointStatus = PointStatus.NICHT_ABGENOMMEN;
		if (request.getParameter("pointsok") != null) {
			pointStatus = PointStatus.ABGENOMMEN;
		}
		// attention: quite similar code in ShowSubmission
		if (!task.getPointCategories().isEmpty()) {
			pointsDAO.createPoints(request.getParameterMap(), submission, participation, publicComment, internalComment, pointStatus, null);
		} else {
			pointsDAO.createPoints(Util.convertToPoints(request.getParameter("points")), submission, participation, publicComment, internalComment, pointStatus, null);
		}
		tx.commit();
		response.sendRedirect(Util.generateRedirectURL("MarkEmptyTask?taskid=" + task.getTaskid(), response));
	}
}
