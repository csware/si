/*
 * Copyright 2009-2010, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import javax.persistence.LockModeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for changing participationroles by superusers and advisors
 * @author Sven Strickroth
 *
 */
public class EditParticipation extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(Util.parseInteger(request.getParameter("participationid"), 0));
		if (participation == null) {
			request.setAttribute("title", "Teilnahme nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}
		Participation callerParticipation = participationDAO.getParticipation(RequestAdapter.getUser(request), participation.getLecture());
		if (callerParticipation == null || callerParticipation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (request.getParameter("type") == null) {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "invalid request");
			return;
		}

		Transaction tx = session.beginTransaction();
		session.refresh(participation, LockModeType.PESSIMISTIC_WRITE);
		if ("tutor".equals(request.getParameter("type"))) {
			participation.setRoleType(ParticipationRole.TUTOR);
		} else {
			participation.setRoleType(ParticipationRole.NORMAL);
		}
		participationDAO.saveParticipation(participation);
		tx.commit();
		response.sendRedirect(Util.generateRedirectURL("ShowLecture?action=showLecture&lecture=" + callerParticipation.getLecture().getId(), response));
	}
}
