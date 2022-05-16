/*
 * Copyright 2021-2022 Sven Strickroth <email@cs-ware.de>
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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.EditLectureView;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for editing a lecture
 * @author Sven Strickroth
 */
@GATEController
public class EditLecture extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			request.setAttribute("title", "Veranstaltung nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), lecture);
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("lecture", lecture);
		getServletContext().getNamedDispatcher(EditLectureView.class.getSimpleName()).forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			request.setAttribute("title", "Veranstaltung nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), lecture);
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		Transaction tx = session.beginTransaction();
		if ("addParticipants".equals(request.getParameter("action"))) {
			UserDAOIf userDAO = DAOFactory.UserDAOIf(session);
			int count = 0;
			List<String> errors = new ArrayList<>();
			String mailadresses[] = request.getParameter("mailadresses").replaceAll("\r\n", "\n").split("\n");
			for (String mailaddress : mailadresses) {
				if (mailaddress.isEmpty()) {
					continue;
				}
				User user = userDAO.getUserByEmail(mailaddress);
				if (user == null) {
					errors.add("\"" + mailaddress + "\" nicht gefunden.");
					continue;
				}
				if (!participationDAO.createParticipation(user, lecture, ParticipationRole.NORMAL)) {
					errors.add("\"" + mailaddress + "\" ist bereits Teilnehmender der Veranstaltung.");
					continue;
				}
				++count;
			}

			StringBuilder output = new StringBuilder();
			if (!errors.isEmpty()) {
				output.append("<h2>Fehler</h2><ul>");
				for (String string : errors) {
					output.append("<li>" + Util.escapeHTML(string) + "</li>");
				}
				output.append("</ul>");
				if (request.getParameter("failonerror") != null) {
					tx.rollback();
					count = 0;
				} else {
					tx.commit();
				}
			} else {
				tx.commit();
			}
			output.append("<h2>Ergebnis</h2>");
			output.append("<p>Teilnehmende hinzugefügt: " + count + "</p>");
			output.append("<p class=mid><a href=\"" + Util.generateHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + lecture.getId(), response) + "\">zurück zur Veranstaltung</a></p>");
			request.setAttribute("title", "Teilnehmende hinzufügen");
			request.setAttribute("message", output.toString());
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}
		lecture.setDescription(request.getParameter("description"));
		lecture.setAllowSelfSubscribe(request.getParameter("allowselfsubscribe") != null);
		tx.commit();
		response.sendRedirect(Util.generateRedirectURL(ShowLecture.class.getSimpleName() + "?lecture=" + lecture.getId(), response));
	}
}
