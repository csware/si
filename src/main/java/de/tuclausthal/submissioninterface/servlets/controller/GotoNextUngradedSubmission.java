/*
 * Copyright 2012, 2020-2022 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for going to the next ungraded submission
 * @author Sven Strickroth
 *
 */
@GATEController
public class GotoNextUngradedSubmission extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		Task task = DAOFactory.TaskDAOIf(session).getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		calculcateRedirect(response, session, task, Util.parseInteger(request.getParameter("sid"), 0), request.getParameter("groupid"), request.getParameter("prev") != null);
	}

	public static void calculcateRedirect(HttpServletResponse response, Session session, Task task, int lastSubmissionID, String groupId, boolean reverse) throws IOException {
		if (!"taskWise".equals(task.getTaskGroup().getLecture().getGradingMethod())) {
			Group group = DAOFactory.GroupDAOIf(session).getGroup(Util.parseInteger(groupId, 0));
			Submission submission = DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, lastSubmissionID, group, reverse);
			if (submission == null && lastSubmissionID > 0) {
				submission = DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, 0, group, reverse);
			}
			if (submission != null) {
				if (group != null) {
					response.sendRedirect(Util.generateRedirectURL(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid() + "&groupid=" + group.getGid(), response));
				} else {
					response.sendRedirect(Util.generateRedirectURL(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response));
				}
			} else {
				response.sendRedirect(Util.generateRedirectURL(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response));
			}
		} else {
			Submission submission = DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, lastSubmissionID, reverse);
			if (submission == null && lastSubmissionID > 0) {
				submission = DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, 0, reverse);
			}
			if (submission != null) {
				response.sendRedirect(Util.generateRedirectURL(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response));
			} else {
				response.sendRedirect(Util.generateRedirectURL(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response));
			}
		}
	}
}
