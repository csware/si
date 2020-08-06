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

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.GroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for adding/removing a group
 * @author Sven Strickroth
 *
 */
public class AddGroup extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			request.setAttribute("title", "Veranstaltung nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), lecture);
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (request.getParameter("action") != null && request.getParameter("action").equals("saveNewGroup") && request.getParameter("name") != null) {
			GroupDAOIf groupDAO = DAOFactory.GroupDAOIf(session);
			Transaction tx = session.beginTransaction();
			Group group = groupDAO.createGroup(lecture, request.getParameter("name"), request.getParameter("allowStudentsToSignup") != null, request.getParameter("allowStudentsToQuit") != null, Util.parseInteger(request.getParameter("maxStudents"), 0));
			tx.commit();
			response.sendRedirect(response.encodeRedirectURL("EditGroup?groupid=" + group.getGid()));
			return;
		} else if (request.getParameter("action") != null && request.getParameter("action").equals("deleteGroup") && request.getParameter("gid") != null) {
			GroupDAOIf groupDAO = DAOFactory.GroupDAOIf(session);
			Group group = groupDAO.getGroup(Util.parseInteger(request.getParameter("gid"), 0));
			int lectureid = 0;
			if (group != null) {
				lectureid = group.getLecture().getId();
				Transaction tx = session.beginTransaction();
				groupDAO.deleteGroup(group);
				tx.commit();
			}
			response.sendRedirect(response.encodeRedirectURL("ShowLecture?lecture=" + lectureid));
			return;
		} else {
			request.setAttribute("lecture", DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0)));
			request.getRequestDispatcher("AddGroupFormView").forward(request, response);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
