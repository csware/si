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

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.GroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for adding and removing members from/to groups
 * @author Sven Strickroth
 *
 */
public class EditGroup extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = HibernateSessionHelper.getSession();
		GroupDAOIf groupDAO = DAOFactory.GroupDAOIf(session);
		Group group = groupDAO.getGroup(Util.parseInteger(request.getParameter("groupid"), 0));
		if (group == null) {
			request.setAttribute("title", "Gruppe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(session), group.getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ("removeFromGroup".equals(request.getParameter("action")) && request.getParameter("participationid") != null) {
			Participation memberParticipation = participationDAO.getParticipation(Util.parseInteger(request.getParameter("participationid"), 0));
			if (memberParticipation != null && (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0 || memberParticipation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0)) {
				memberParticipation.setGroup(null);
				participationDAO.saveParticipation(memberParticipation);
			}
			response.sendRedirect(response.encodeRedirectURL("ShowLecture?lecture=" + group.getLecture().getId()));
			return;
		} else if (request.getParameterValues("members") != null && request.getParameterValues("members").length > 0) {
			if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) {
				group.setName(request.getParameter("title"));
				group.setAllowStudentsToSignup(request.getParameter("allowStudentsToSignup") != null);
				group.setAllowStudentsToQuit(request.getParameter("allowStudentsToQuit") != null);
				group.setMaxStudents(Util.parseInteger(request.getParameter("maxStudents"), 0));
				groupDAO.saveGroup(group);
			}
			for (String newMember : request.getParameterValues("members")) {
				Participation memberParticipation = participationDAO.getParticipation(Util.parseInteger(newMember, 0));
				if (memberParticipation != null && (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0 || memberParticipation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0)) {
					memberParticipation.setGroup(group);
					participationDAO.saveParticipation(memberParticipation);
				}
			}
			response.sendRedirect(response.encodeRedirectURL("ShowLecture?lecture=" + group.getLecture().getId()));
			return;
		}

		request.setAttribute("participation", participation);
		request.setAttribute("group", group);
		request.getRequestDispatcher("EditGroupFormView").forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
