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
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for displaying a user
 * @author Sven Strickroth
 */
public class ShowUser extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = HibernateSessionHelper.getSession();

		List<Lecture> lectures = new LinkedList<Lecture>();
		for (Participation participation : new SessionAdapter(request).getUser(session).getLectureParticipant()) {
			if (participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
				lectures.add(participation.getLecture());
			}
		}

		if (lectures.size() == 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		User user = DAOFactory.UserDAOIf(session).getUser(Util.parseInteger(request.getParameter("uid"), 0));
		if (user == null) {
			request.setAttribute("title", "Benutzer nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		request.setAttribute("user", user);
		request.setAttribute("lectures", lectures);
		request.setAttribute("session", session);
		request.getRequestDispatcher("ShowUserView").forward(request, response);
	}
}
