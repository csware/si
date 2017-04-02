/*
 * Copyright 2011, 2017 Sven Strickroth <email@cs-ware.de>
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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.GroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.LectureDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.MailSender;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for mass mails
 * @author Sven Strickroth
 */
public class MassMail extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Lecture lecture = null;
		Group group = null;

		if (request.getParameter("groupid") == null && request.getParameter("lectureid") == null) {
			request.setAttribute("title", "ungültiger Aufruf");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		if (request.getParameter("groupid") != null) {
			GroupDAOIf groupDAO = DAOFactory.GroupDAOIf(session);
			group = groupDAO.getGroup(Util.parseInteger(request.getParameter("groupid"), 0));
			if (group == null) {
				request.setAttribute("title", "Gruppe nicht gefunden");
				request.getRequestDispatcher("MessageView").forward(request, response);
				return;
			}
			lecture = group.getLecture();
		} else if (request.getParameter("lectureid") != null) {
			LectureDAOIf lectureDAO = DAOFactory.LectureDAOIf(session);
			lecture = lectureDAO.getLecture(Util.parseInteger(request.getParameter("lectureid"), 0));
			if (lecture == null) {
				request.setAttribute("title", "Vorlesung nicht gefunden");
				request.getRequestDispatcher("MessageView").forward(request, response);
				return;
			}
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), lecture);
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "operation not allowed");
			return;
		}

		request.setAttribute("lecture", lecture);
		request.setAttribute("group", group);
		request.getRequestDispatcher("MassMailView").forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		if (request.getParameter("subject") == null && request.getParameter("message") == null) {
			request.setAttribute("title", "ungültiger Aufruf");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		LectureDAOIf lectureDAO = DAOFactory.LectureDAOIf(session);
		Lecture lecture = lectureDAO.getLecture(Util.parseInteger(request.getParameter("lectureid"), 0));
		if (lecture == null) {
			request.setAttribute("title", "Vorlesung nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), lecture);
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "operation not allowed");
			return;
		}

		Set<String> receipients = new LinkedHashSet<>();
		if (request.getParameterValues("toall") != null) {
			for (Participation lectureParticipation : lecture.getParticipants()) {
				receipients.add(lectureParticipation.getUser().getFullEmail());
			}
		} else if (request.getParameterValues("gids") != null && request.getParameterValues("gids").length > 0) {
			GroupDAOIf groupDAO = DAOFactory.GroupDAOIf(session);
			for (String gid : request.getParameterValues("gids")) {
				if ("nogroup".equals(gid)) {
					for (Participation noGroupParticipation : participationDAO.getParticipationsWithoutGroup(lecture)) {
						receipients.add(noGroupParticipation.getUser().getFullEmail());
					}
				} else {
					Group group = groupDAO.getGroup(Util.parseInteger(gid, 0));
					if (group != null && group.getLecture().getId() == lecture.getId()) {
						for (Participation groupParticipation : group.getMembers()) {
							receipients.add(groupParticipation.getUser().getFullEmail());
						}
						for (Participation groupTutorParticipation : group.getTutors()) {
							receipients.add(groupTutorParticipation.getUser().getFullEmail());
						}
					}
				}
			}
		}
		if (receipients.size() == 0) {
			request.setAttribute("title", "Keine Empfänger ausgewählt");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}
		for (String receipient : receipients) {
			MailSender.sendMail(receipient, request.getParameter("subject"), request.getParameter("message").trim() + "\n\n-- \nGesendet von: " + participation.getUser().getFullName() + " <" + participation.getUser().getFullEmail() + ">\nDirect reply is not possible.");
		}
		request.setAttribute("title", "Mail gesendet");
		request.getRequestDispatcher("MessageView").forward(request, response);
	}
}
