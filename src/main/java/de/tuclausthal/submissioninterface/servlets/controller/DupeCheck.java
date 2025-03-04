/*
 * Copyright 2009-2010, 2020-2022, 2025 Sven Strickroth <email@cs-ware.de>
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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SimilarityTestDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.DupeCheckFormView;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for adding and removing plagiarism tests
 * @author Sven Strickroth
 *
 */
@GATEController
public class DupeCheck extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
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

		request.setAttribute("task", task);
		getServletContext().getNamedDispatcher(DupeCheckFormView.class.getSimpleName()).forward(request, response);
	}

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

		SimilarityTestDAOIf semilarityTestDAO = DAOFactory.SimilarityTestDAOIf(session);
		if ("deleteSimilarityTest".equals(request.getParameter("action")) && request.getParameter("similaritytestid") != null) {
			Transaction tx = session.beginTransaction();
			SimilarityTest similarityTest = semilarityTestDAO.getSimilarityTestLocked(Util.parseInteger(request.getParameter("similaritytestid"), 0));
			if (similarityTest != null && similarityTest.getTask().getTaskid() == task.getTaskid()) {
				semilarityTestDAO.deleteSimilarityTest(similarityTest);
			}
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&action=editTask&lecture=" + task.getTaskGroup().getLecture().getId(), response));
		} else if (request.getParameter("type") != null && "savesimilaritytest".equals(request.getParameter("action"))) {
			int minSimilarity = Util.parseInteger(request.getParameter("minsimilarity"), 50);
			Transaction tx = session.beginTransaction();
			semilarityTestDAO.addSimilarityTest(task, request.getParameter("type"), request.getParameter("normalizer1"), "lc".equals(request.getParameter("normalizer2")), request.getParameter("normalizer3"), minSimilarity, request.getParameter("excludeFiles"));
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&action=editTask&lecture=" + task.getTaskGroup().getLecture().getId(), response));
		} else if ("rerunSimilarityTest".equals(request.getParameter("action")) && request.getParameter("similaritytestid") != null) {
			Transaction tx = session.beginTransaction();
			SimilarityTest similarityTest = semilarityTestDAO.getSimilarityTestLocked(Util.parseInteger(request.getParameter("similaritytestid"), 0));
			if (similarityTest != null && similarityTest.getTask().getTaskid() == task.getTaskid()) {
				similarityTest.setStatus(1);
			}
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&action=editTask&lecture=" + task.getTaskGroup().getLecture().getId(), response));
		} else {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "invalid request");
		}
	}
}
