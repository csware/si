/*
 * Copyright 2011, 2020-2022, 2025 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.persistence.dao.GroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.EditMultipleGroupsFormView;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for editing multiple groups at once
 * @author Sven Strickroth
 */
@GATEController
public class EditMultipleGroups extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Veranstaltung nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), lecture);
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("lecture", lecture);
		getServletContext().getNamedDispatcher(EditMultipleGroupsFormView.class.getSimpleName()).forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Veranstaltung nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), lecture);
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		GroupDAOIf groupDAO = DAOFactory.GroupDAOIf(session);
		if (request.getParameterValues("gids") != null && request.getParameterValues("gids").length > 0) {
			Transaction tx = session.beginTransaction();
			for (String gid : request.getParameterValues("gids")) {
				Group group = groupDAO.getGroupLocked(Util.parseInteger(gid, 0));
				if (group != null && group.getLecture().getId() == lecture.getId()) {
					if ("0".equals(request.getParameter("allowStudentsToSignup")) || "1".equals(request.getParameter("allowStudentsToSignup"))) {
						group.setAllowStudentsToSignup("1".equals(request.getParameter("allowStudentsToSignup")));
					}
					if ("0".equals(request.getParameter("allowStudentsToQuit")) || "1".equals(request.getParameter("allowStudentsToQuit"))) {
						group.setAllowStudentsToQuit("1".equals(request.getParameter("allowStudentsToQuit")));
					}
					if (request.getParameter("maxStudents") != null && !request.getParameter("maxStudents").isEmpty()) {
						group.setMaxStudents(Util.parseInteger(request.getParameter("maxStudents"), 0));
					}
					if ("0".equals(request.getParameter("membersvisible")) || "1".equals(request.getParameter("membersvisible"))) {
						group.setMembersVisibleToStudents("1".equals(request.getParameter("membersvisible")));
					}
					if ("0".equals(request.getParameter("submissionGroup")) || "1".equals(request.getParameter("submissionGroup"))) {
						group.setSubmissionGroup("1".equals(request.getParameter("submissionGroup")));
					}
				}
			}
			tx.commit();
		}
		response.sendRedirect(Util.generateRedirectURL(ShowLecture.class.getSimpleName() + "?lecture=" + lecture.getId(), response));
	}
}
