/*
 * Copyright 2009-2010, 2020-2025 Sven Strickroth <email@cs-ware.de>
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

import jakarta.persistence.LockModeType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for changing participationroles by advisors
 * @author Sven Strickroth
 *
 */
@GATEController
public class EditParticipation extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(Util.parseInteger(request.getParameter("participationid"), 0));
		if (participation == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Teilnahme nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}
		Participation callerParticipation = participationDAO.getParticipation(RequestAdapter.getUser(request), participation.getLecture());
		if (callerParticipation == null || callerParticipation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (request.getParameter("type") == null || callerParticipation == participation) {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "invalid request");
			return;
		}

		Transaction tx = session.beginTransaction();
		session.refresh(participation, LockModeType.PESSIMISTIC_WRITE);
		if ("tutor".equals(request.getParameter("type"))) {
			participation.setRoleType(ParticipationRole.TUTOR);
			if (participation.getGroup() != null) {
				participation.getGroup().getTutors().add(participation);
				participation.setGroup(null);
			}
		} else {
			if (participation.getLecture().getGroups().stream().anyMatch(g -> g.getTutors().contains(participation))) {
				tx.rollback();
				request.setAttribute("title", "Ungültige Anfrage");
				request.setAttribute("message", "Studierender ist in mindestens einer Gruppe als TutorIn zugeordnet.");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			participation.setRoleType(ParticipationRole.NORMAL);
		}
		tx.commit();
		response.sendRedirect(Util.generateRedirectURL(ShowLecture.class.getSimpleName() + "?lecture=" + callerParticipation.getLecture().getId(), response));
	}
}
