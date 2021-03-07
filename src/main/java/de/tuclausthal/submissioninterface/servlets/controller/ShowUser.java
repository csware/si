/*
 * Copyright 2009-2010, 2013, 2017, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for displaying a user
 * @author Sven Strickroth
 */
public class ShowUser extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		List<Lecture> possibleLectures = new ArrayList<>();
		for (Participation participation : RequestAdapter.getUser(request).getLectureParticipant()) {
			if (participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
				possibleLectures.add(participation.getLecture());
			}
		}

		if (possibleLectures.isEmpty()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		User user = DAOFactory.UserDAOIf(session).getUser(Util.parseInteger(request.getParameter("uid"), 0));
		if (user == null) {
			request.setAttribute("title", "BenutzerIn nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		List<Participation> participations = new ArrayList<>();
		Boolean isAtLeastAdvisorOnce = false;
		for (Participation participation : user.getLectureParticipant()) {
			if (possibleLectures.contains(participation.getLecture())) {
				if (!isAtLeastAdvisorOnce && participation.getRoleType().compareTo(ParticipationRole.ADVISOR) >= 0) {
					isAtLeastAdvisorOnce = true;
				}
				participations.add(participation);
			}
		}

		if (participations.isEmpty()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("user", user);
		request.setAttribute("participations", participations);
		request.setAttribute("isAtLeastAdvisorOnce", isAtLeastAdvisorOnce);
		getServletContext().getNamedDispatcher("ShowUserView").forward(request, response);
	}
}
