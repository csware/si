/*
 * Copyright 2009 - 2011 Sven Strickroth <email@cs-ware.de>
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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

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
 * Controller-Servlet for displaying a lecture
 * loads a lecture and differs between the student and tutor view
 * @author Sven Strickroth
 *
 */
public class ShowLecture extends HttpServlet {
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
		if (participation == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("participation", participation);
		if (participation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
			GroupDAOIf groupDAO = DAOFactory.GroupDAOIf(session);
			List<Group> joinAbleGroups = groupDAO.getJoinAbleGroups(lecture, participation.getGroup());
			if (participation.getGroup() == null || participation.getGroup().isAllowStudentsToQuit() && joinAbleGroups.size() > 0) {
				request.setAttribute("joinAbleGroups", joinAbleGroups);
			}
			request.getRequestDispatcher("ShowLectureStudentView").forward(request, response);
		} else if ("list".equals(request.getParameter("show"))) {
			request.getRequestDispatcher("ShowLectureTutorFullView").forward(request, response);
		} else if ("csv".equals(request.getParameter("show"))) {
			request.getRequestDispatcher("ShowLectureTutorCSVView").forward(request, response);
		} else {
			request.getRequestDispatcher("ShowLectureTutorView").forward(request, response);
		}
	}
}
