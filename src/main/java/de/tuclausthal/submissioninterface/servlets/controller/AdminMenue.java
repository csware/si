/*
 * Copyright 2009-2012, 2020 Sven Strickroth <email@cs-ware.de>
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
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for the Admin-Menue
 * @author Sven Strickroth
 *
 */
public class AdminMenue extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		if (!RequestAdapter.getUser(request).isSuperUser()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ("newLecture".equals(request.getParameter("action"))) {
			request.getRequestDispatcher("AdminMenueAddLectureView").forward(request, response);
		} else if ("cleanup".equals(request.getParameter("action"))) {
			File path = new ContextAdapter(getServletContext()).getDataPath();
			// list lectures
			for (File lectures : path.listFiles()) {
				if (lectures.isDirectory() && DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(lectures.getName(), 0)) == null) {
					Util.recursiveDelete(lectures);
				} else if (lectures.isDirectory()) {
					// list all tasks
					for (File tasks : lectures.listFiles()) {
						if (!tasks.isDirectory() || DAOFactory.TaskDAOIf(session).getTask(Util.parseInteger(tasks.getName(), 0)) == null) {
							Util.recursiveDelete(tasks);
						} else {
							// list all submissions
							for (File submissions : tasks.listFiles()) {
								// check for junittests
								if (submissions.getName().startsWith("junittest")) {
									if (DAOFactory.TaskDAOIf(session).getTask(Util.parseInteger(submissions.getName(), 0)) == null) {
										boolean kill = true;
										for (Test test : DAOFactory.TaskDAOIf(session).getTask(Util.parseInteger(tasks.getName(), 0)).getTests()) {
											if (submissions.getName().equals("junittest" + test.getId() + ".jar") && test instanceof JUnitTest) {
												kill = false;
												break;
											}
										}
										if (kill) {
											tasks.delete();
										}
									}
								} else if (DAOFactory.SubmissionDAOIf(session).getSubmissionLocked(Util.parseInteger(submissions.getName(), 0)) == null) {
									Util.recursiveDelete(submissions);
								} else {
									Util.recursiveDeleteEmptySubDirectories(submissions);
								}
							}
						}
					}
				}
			}
			response.sendRedirect(response.encodeRedirectURL(request.getRequestURL() + "?"));
		} else if ("saveLecture".equals(request.getParameter("action")) && request.getParameter("name") != null && !request.getParameter("name").trim().isEmpty()) {
			Transaction tx = session.beginTransaction();
			Lecture newLecture = DAOFactory.LectureDAOIf(session).newLecture(request.getParameter("name").trim(), request.getParameter("requiresAbhnahme") != null, request.getParameter("groupWise") != null);
			DAOFactory.ParticipationDAOIf(session).createParticipation(RequestAdapter.getUser(request), newLecture, ParticipationRole.ADVISOR);
			tx.commit();
			response.sendRedirect(response.encodeRedirectURL(request.getRequestURL() + "?action=showLecture&lecture=" + newLecture.getId()));
		} else if ("deleteLecture".equals(request.getParameter("action")) && request.getParameter("lecture") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			if (lecture != null) {
				DAOFactory.LectureDAOIf(session).deleteLecture(lecture);
			}
			// do a redirect, so that refreshing the page in a browser doesn't create duplicates
			response.sendRedirect(response.encodeRedirectURL(request.getRequestURL() + "?"));
		} else if ("showLecture".equals(request.getParameter("action")) && request.getParameter("lecture") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			request.setAttribute("lecture", lecture);
			request.setAttribute("participants", DAOFactory.ParticipationDAOIf(session).getLectureParticipations(lecture));
			request.getRequestDispatcher("AdminMenueEditLectureView").forward(request, response);
		} else if ("showAdminUsers".equals(request.getParameter("action"))) {
			request.setAttribute("superusers", DAOFactory.UserDAOIf(session).getSuperUsers());
			request.getRequestDispatcher("AdminMenueShowAdminUsersView").forward(request, response);
		} else if (("addSuperUser".equals(request.getParameter("action")) || "removeSuperUser".equals(request.getParameter("action"))) && request.getParameter("userid") != null) {
			UserDAOIf userDAO = DAOFactory.UserDAOIf(session);
			session.beginTransaction();
			User user = userDAO.getUser(Util.parseInteger(request.getParameter("userid"), 0));
			if (user != null) {
				user.setSuperUser("addSuperUser".equals(request.getParameter("action")));
				userDAO.saveUser(user);
			}
			session.getTransaction().commit();
			response.sendRedirect(response.encodeURL(request.getRequestURL() + "?action=showAdminUsers"));
		} else if (("addUser".equals(request.getParameter("action")) || "removeUser".equals(request.getParameter("action"))) && request.getParameter("lecture") != null && request.getParameter("userid") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			UserDAOIf userDAO = DAOFactory.UserDAOIf(session);
			User user = userDAO.getUser(Util.parseInteger(request.getParameter("userid"), 0));
			if (lecture == null || user == null) {
				response.sendRedirect(response.encodeURL(request.getRequestURL() + "?"));
			} else {
				// request.getParameter("type") != null
				ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
				Transaction tx = session.beginTransaction();
				if (request.getParameter("action").equals("addUser")) {
					if ("advisor".equals(request.getParameter("type"))) {
						participationDAO.createParticipation(user, lecture, ParticipationRole.ADVISOR);
					} else {
						participationDAO.createParticipation(user, lecture, ParticipationRole.TUTOR);
					}
				} else { // dregregate user
					participationDAO.createParticipation(user, lecture, ParticipationRole.NORMAL);
				}
				tx.commit();
				response.sendRedirect(response.encodeURL(request.getRequestURL() + "?action=showLecture&lecture=" + lecture.getId()));
			}
		} else if ("su".equals(request.getParameter("action")) && request.getParameter("userid") != null) {
			User user = DAOFactory.UserDAOIf(session).getUser(Util.parseInteger(request.getParameter("userid"), 0));
			if (user != null) {
				RequestAdapter.getSessionAdapter(request).setUser(user, request.getRemoteAddr());
				response.sendRedirect(response.encodeRedirectURL("Overview"));
			} else {
				response.sendRedirect(response.encodeRedirectURL(request.getRequestURL() + "?"));
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
