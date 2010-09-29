/*
 * Copyright 2010 Sven Strickroth <email@cs-ware.de>
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
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.GroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for adding and removing members from/to groups
 * @author Sven Strickroth
 *
 */
public class JoinGroup extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		GroupDAOIf groupDAO = DAOFactory.GroupDAOIf(session);
		Transaction tx = session.beginTransaction();
		Group group = groupDAO.getGroupLocked(Util.parseInteger(request.getParameter("groupid"), 0));
		if (group == null) {
			request.setAttribute("title", "Gruppe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			tx.commit();
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipationLocked(RequestAdapter.getUser(request), group.getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.NORMAL) != 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "operation not allowed");
			tx.commit();
			return;
		}

		boolean canJoin = group.isAllowStudentsToSignup() && group.getMembers().size() < group.getMaxStudents();
		if ((participation.getGroup() == null || participation.getGroup().isAllowStudentsToQuit()) && canJoin) {
			participation.setGroup(group);
			participationDAO.saveParticipation(participation);
		}
		response.sendRedirect(response.encodeRedirectURL("ShowLecture?lecture=" + group.getLecture().getId()));
		tx.commit();
		return;
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
