/*
 * Copyright 2009-2010, 2017 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for users to subscribe to lectures
 * @author Sven Strickroth
 */
public class SubscribeToLecture extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		if (request.getParameter("lecture") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			if (lecture == null) {
				request.setAttribute("title", "Veranstaltung nicht gefunden");
				request.getRequestDispatcher("MessageView").forward(request, response);
				return;
			}

			ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
			Transaction tx = session.beginTransaction();
			Participation participation = participationDAO.getParticipationLocked(RequestAdapter.getUser(request), lecture);
			if (participation != null) {
				tx.commit();
				response.sendRedirect(response.encodeRedirectURL("ShowLecture?lecture=" + lecture.getId()));
				return;
			} else if (lecture.getSemester() < Util.getCurrentSemester()) {
				tx.commit();
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
				return;
			} else {
				participationDAO.createParticipation(RequestAdapter.getUser(request), lecture, ParticipationRole.NORMAL);
				tx.commit();
				response.sendRedirect(response.encodeRedirectURL("ShowLecture?lecture=" + lecture.getId()));
				return;
			}
		}
		request.setAttribute("lectures", DAOFactory.LectureDAOIf(session).getCurrentLecturesWithoutUser(RequestAdapter.getUser(request)));
		request.getRequestDispatcher("SubscribeToLectureView").forward(request, response);
	}
}
