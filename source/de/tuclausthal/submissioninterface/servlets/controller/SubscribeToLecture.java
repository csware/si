/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for users to subscribe to lectures
 * @author Sven Strickroth
 */
public class SubscribeToLecture extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if (request.getParameter("lecture") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf().getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			if (lecture == null) {
				request.setAttribute("title", "Veranstaltung nicht gefunden");
				request.getRequestDispatcher("MessageView").forward(request, response);
				return;
			}

			ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
			Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(), lecture);
			if (participation != null || lecture.getSemester() < Util.getCurrentSemester()) {
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
				return;
			} else {
				participationDAO.createParticipation(new SessionAdapter(request).getUser(), lecture, ParticipationRole.NORMAL);
				response.sendRedirect(response.encodeRedirectURL("ShowLecture?lecture=" + lecture.getId()));
				return;
			}
		} else {
			request.getRequestDispatcher("SubscribeToLectureView").forward(request, response);
		}
	}
}
