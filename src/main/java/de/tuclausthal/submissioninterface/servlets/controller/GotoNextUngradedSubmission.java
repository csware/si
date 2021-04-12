/*
 * Copyright 2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for going to the next ungraded submission
 * @author Sven Strickroth
 *
 */
public class GotoNextUngradedSubmission extends HttpServlet {
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
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		int lastSubmissionID = Util.parseInteger(request.getParameter("sid"), 0);
		if (!"taskWise".equals(task.getTaskGroup().getLecture().getGradingMethod())) {
			Group group = DAOFactory.GroupDAOIf(session).getGroup(Util.parseInteger(request.getParameter("groupid"), 0));
			Submission submission = DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, lastSubmissionID, group, request.getParameter("prev") != null);
			if (submission == null && lastSubmissionID > 0) {
				submission = DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, 0, group, request.getParameter("prev") != null);
			}
			if (submission != null) {
				if (group != null) {
					response.sendRedirect(Util.generateRedirectURL("ShowSubmission?sid=" + submission.getSubmissionid() + "&groupid=" + group.getGid(), response));
				} else {
					response.sendRedirect(Util.generateRedirectURL("ShowSubmission?sid=" + submission.getSubmissionid(), response));
				}
			} else {
				response.sendRedirect(Util.generateRedirectURL("ShowTask?taskid=" + task.getTaskid(), response));
			}
		} else {
			Submission submission = DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, lastSubmissionID, request.getParameter("prev") != null);
			if (submission == null && lastSubmissionID > 0) {
				submission = DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, 0, request.getParameter("prev") != null);
			}
			if (submission != null) {
				response.sendRedirect(Util.generateRedirectURL("ShowSubmission?sid=" + submission.getSubmissionid(), response));
			} else {
				response.sendRedirect(Util.generateRedirectURL("ShowTask?taskid=" + task.getTaskid(), response));
			}
		}
	}
}
