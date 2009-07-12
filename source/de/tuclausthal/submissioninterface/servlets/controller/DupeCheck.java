/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SimilarityTestDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for adding and removing plagiarism tests
 * @author Sven Strickroth
 *
 */
public class DupeCheck extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf();
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(), task.getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		SimilarityTestDAOIf semilarityTestDAO = DAOFactory.SimilarityTestDAOIf();
		if ("deleteSimilarityTest".equals(request.getParameter("action")) && request.getParameter("similaritytestid") != null) {
			SimilarityTest similarityTest = semilarityTestDAO.getSimilarityTest(Util.parseInteger(request.getParameter("similaritytestid"), 0));
			if (similarityTest != null) {
				semilarityTestDAO.deleteSimilarityTest(similarityTest);
			}
			response.sendRedirect(response.encodeRedirectURL("TaskManager?taskid=" + task.getTaskid() + "&action=editTask&lecture=" + task.getLecture().getId()));
		} else if (request.getParameter("type") != null && "savesimilaritytest".equals(request.getParameter("action"))) {
			int minSimilarity = Util.parseInteger(request.getParameter("minsimilarity"), 50);
			semilarityTestDAO.addSimilarityTest(task, request.getParameter("type"), request.getParameter("normalizer1"), "lc".equals(request.getParameter("normalizer2")), request.getParameter("normalizer3"), minSimilarity, request.getParameter("excludeFiles"));
			response.sendRedirect(response.encodeRedirectURL("TaskManager?taskid=" + task.getTaskid() + "&action=editTask&lecture=" + task.getLecture().getId()));
		} else {
			request.setAttribute("task", task);
			request.getRequestDispatcher("DupeCheckFormView").forward(request, response);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
