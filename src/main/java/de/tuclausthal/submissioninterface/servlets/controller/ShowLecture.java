/*
 * Copyright 2009-2011, 2020-2022, 2025 Sven Strickroth <email@cs-ware.de>
 *
 * This file is part of the GATE.
 *
 * GATE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * GATE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GATE. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.servlets.controller;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.GroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.servlets.view.ShowLectureStudentView;
import de.tuclausthal.submissioninterface.servlets.view.ShowLectureTutorCSVView;
import de.tuclausthal.submissioninterface.servlets.view.ShowLectureTutorFullView;
import de.tuclausthal.submissioninterface.servlets.view.ShowLectureTutorView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for displaying a lecture
 * loads a lecture and differs between the student and tutor view
 * @author Sven Strickroth
 *
 */
@GATEController
public class ShowLecture extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Veranstaltung nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), lecture);
		if (participation == null) {
			if (lecture.getSemester() == Util.getCurrentSemester() && lecture.isAllowSelfSubscribe()) {
				request.setAttribute("title", "Zugriff verweigert (403)");
				request.setAttribute("message", "<p>Sie versuchen auf eine Veranstaltung zuzugreifen, für die Sie (noch) nicht angemeldet sind.</p><p><form method=post action=\"" + Util.generateHTMLLink(SubscribeToLecture.class.getSimpleName() + "?lecture=" + lecture.getId(), response) + "\"><input type=submit value=\"zur Veranstaltung anmelden\"></form></p>");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			request.setAttribute("title", "Zugriff verweigert (403)");
			request.setAttribute("message", "<p>Sie versuchen auf eine Veranstaltung zuzugreifen, für die Sie nicht angemeldet sind.</p><p>Eine Anmeldung ist nicht (mehr) möglich.</p>");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		request.setAttribute("participation", participation);
		if (participation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
			GroupDAOIf groupDao = DAOFactory.GroupDAOIf(session);
			List<Group> joinAbleGroups = groupDao.getJoinAbleGroups(lecture, participation.getGroup());
			request.setAttribute("joinAbleGroups", joinAbleGroups);
			request.setAttribute("groupSizes", groupDao.getGroupSizes(joinAbleGroups, participation.getGroup()));
			request.setAttribute("tasks", DAOFactory.TaskDAOIf(session).getTasks(lecture, true));
			request.setAttribute("submissions", DAOFactory.SubmissionDAOIf(session).getAllSubmissions(participation));
			getServletContext().getNamedDispatcher(ShowLectureStudentView.class.getSimpleName()).forward(request, response);
		} else if ("list".equals(request.getParameter("show"))) {
			getServletContext().getNamedDispatcher(ShowLectureTutorFullView.class.getSimpleName()).forward(request, response);
		} else if ("csv".equals(request.getParameter("show"))) {
			getServletContext().getNamedDispatcher(ShowLectureTutorCSVView.class.getSimpleName()).forward(request, response);
		} else {
			getServletContext().getNamedDispatcher(ShowLectureTutorView.class.getSimpleName()).forward(request, response);
		}
	}
}
