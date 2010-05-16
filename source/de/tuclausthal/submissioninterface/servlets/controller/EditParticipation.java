/*
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for changing participationroles by superusers and advisors
 * @author Sven Strickroth
 *
 */
public class EditParticipation extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = HibernateSessionHelper.getSession();
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(Util.parseInteger(request.getParameter("participationid"), 0));
		if (participation == null) {
			request.setAttribute("title", "Teilnahme nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}
		Participation callerParticipation = participationDAO.getParticipation(new SessionAdapter(request).getUser(session), participation.getLecture());
		if (callerParticipation == null || callerParticipation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		Transaction tx = session.beginTransaction();
		if (request.getParameter("type") != null && request.getParameter("type").equals("advisor") && callerParticipation.getUser().isSuperUser()) {
			participationDAO.createParticipation(participation.getUser(), participation.getLecture(), ParticipationRole.ADVISOR);
		} else if (request.getParameter("type") != null && request.getParameter("type").equals("tutor")) {
			participationDAO.createParticipation(participation.getUser(), participation.getLecture(), ParticipationRole.TUTOR);
		} else {
			participationDAO.createParticipation(participation.getUser(), participation.getLecture(), ParticipationRole.NORMAL);
		}
		tx.commit();
		if ("admin".equals(request.getParameter("goback"))) {
			response.sendRedirect(response.encodeURL("AdminMenue?action=showLecture&lecture=" + callerParticipation.getLecture().getId()));
		} else {
			response.sendRedirect(response.encodeURL("ShowLecture?action=showLecture&lecture=" + callerParticipation.getLecture().getId()));
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
