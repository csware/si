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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for the Admin-Menue
 * @author Sven Strickroth
 *
 */
public class AdminMenue extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = HibernateSessionHelper.getSession();
		SessionAdapter sa = new SessionAdapter((HttpServletRequest) request);
		if (!sa.getUser(session).isSuperUser()) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (request.getParameter("action") != null && request.getParameter("action").equals("newLecture")) {
			request.getRequestDispatcher("AdminMenueAddLectureView").forward(request, response);
		} else if (request.getParameter("action") != null && request.getParameter("action").equals("cleanup")) {
			File path = new ContextAdapter(getServletContext()).getDataPath();
			// list lectures
			for (File lectures : path.listFiles()) {
				if (lectures.isDirectory() && DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(lectures.getName(), 0)) == null) {
					Util.recursiveDelete(lectures);
				} else if (lectures.isDirectory()) {
					// list all tasks
					for (File tasks : lectures.listFiles()) {
						if (!tasks.getName().startsWith("junittest") && DAOFactory.TaskDAOIf(session).getTask(Util.parseInteger(tasks.getName(), 0)) == null) {
							Util.recursiveDelete(tasks);
						} else if (tasks.getName().startsWith("junittest")) {
							// TODO: 2bd
							//if (DAOFactory.TaskDAOIf().getTask(Util.parseInteger(tasks.getName(), 0)) == null && !(DAOFactory.TaskDAOIf().getTask(Util.parseInteger(tasks.getName(), 0)).getTest() instanceof JUnitTest)) {
								tasks.delete();
							//}
						} else {
							// list all submissions
							for (File submissions : tasks.listFiles()) {
								if (DAOFactory.SubmissionDAOIf(session).getSubmission(Util.parseInteger(submissions.getName(), 0)) == null) {
									Util.recursiveDelete(submissions);
								}
							}
						}
					}
				}
			}
			response.sendRedirect(response.encodeRedirectURL(request.getRequestURL() + "?"));
		} else if (request.getParameter("action") != null && request.getParameter("action").equals("saveLecture") && request.getParameter("name") != null && !request.getParameter("name").trim().isEmpty()) {
			Lecture newLecture = DAOFactory.LectureDAOIf(session).newLecture(request.getParameter("name").trim());
			// do a redirect, so that refreshing the page in a browser doesn't create duplicates
			response.sendRedirect(response.encodeRedirectURL(request.getRequestURL() + "?action=showLecture&lecture=" + newLecture.getId()));
		} else if (request.getParameter("action") != null && request.getParameter("action").equals("deleteLecture") && request.getParameter("lecture") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			if (lecture != null) {
				DAOFactory.LectureDAOIf(session).deleteLecture(lecture);
			}
			// do a redirect, so that refreshing the page in a browser doesn't create duplicates
			response.sendRedirect(response.encodeRedirectURL(request.getRequestURL() + "?"));
		} else if (request.getParameter("action") != null && request.getParameter("action").equals("showLecture") && request.getParameter("lecture") != null) {
			request.setAttribute("lecture", DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0)));
			request.getRequestDispatcher("AdminMenueEditLectureView").forward(request, response);
		} else if (request.getParameter("action") != null && request.getParameter("action").equals("showAdminUsers")) {
			request.setAttribute("superusers", DAOFactory.UserDAOIf(session).getSuperUsers());
			request.getRequestDispatcher("AdminMenueShowAdminUsersView").forward(request, response);
		} else if (("addSuperUser".equals(request.getParameter("action")) || "removeSuperUser".equals(request.getParameter("action"))) && request.getParameter("userid") != null) {
			UserDAOIf userDAO = DAOFactory.UserDAOIf(session);
			User user = userDAO.getUser(Util.parseInteger(request.getParameter("userid"), 0));
			if (user != null) {
				user.setSuperUser("addSuperUser".equals(request.getParameter("action")));
				userDAO.saveUser(user);
			}
			response.sendRedirect(response.encodeURL(request.getRequestURL() + "?action=showAdminUsers"));
		} else if (request.getParameter("action") != null && (request.getParameter("action").equals("addUser") || request.getParameter("action").equals("removeUser")) && request.getParameter("lecture") != null && request.getParameter("userid") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			UserDAOIf userDAO = DAOFactory.UserDAOIf(session);
			User user = userDAO.getUser(Util.parseInteger(request.getParameter("userid"), 0));
			if (lecture == null || user == null) {
				response.sendRedirect(response.encodeURL(request.getRequestURL() + "?"));
			} else {
				// request.getParameter("type") != null
				ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
				if (request.getParameter("action").equals("addUser")) {
					if (request.getParameter("type").equals("advisor")) {
						participationDAO.createParticipation(user, lecture, ParticipationRole.ADVISOR);
					} else {
						participationDAO.createParticipation(user, lecture, ParticipationRole.TUTOR);
					}
				} else { // dregregate user
					participationDAO.createParticipation(user, lecture, ParticipationRole.NORMAL);
				}
				response.sendRedirect(response.encodeURL(request.getRequestURL() + "?action=showLecture&lecture=" + lecture.getId()));
			}
		} else { // list all lectures
			request.setAttribute("lectures", DAOFactory.LectureDAOIf(session).getLectures());
			request.getRequestDispatcher("AdminMenueOverView").forward(request, response);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
