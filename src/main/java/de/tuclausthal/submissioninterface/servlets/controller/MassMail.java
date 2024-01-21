/*
 * Copyright 2011, 2017, 2020-2022, 2024 Sven Strickroth <email@cs-ware.de>
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.opencsv.CSVWriter;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.GroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.LectureDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MassMailView;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.MailSender;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for mass mails
 * @author Sven Strickroth
 */
@GATEController
public class MassMail extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Lecture lecture = null;
		Group group = null;

		if (request.getParameter("groupid") == null && request.getParameter("lectureid") == null) {
			request.setAttribute("title", "ungültiger Aufruf");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		if (request.getParameter("groupid") != null) {
			GroupDAOIf groupDAO = DAOFactory.GroupDAOIf(session);
			group = groupDAO.getGroup(Util.parseInteger(request.getParameter("groupid"), 0));
			if (group == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Gruppe nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			lecture = group.getLecture();
		} else if (request.getParameter("lectureid") != null) {
			LectureDAOIf lectureDAO = DAOFactory.LectureDAOIf(session);
			lecture = lectureDAO.getLecture(Util.parseInteger(request.getParameter("lectureid"), 0));
			if (lecture == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Vorlesung nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
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
		getServletContext().getNamedDispatcher(MassMailView.class.getSimpleName()).forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		if (request.getParameter("subject") == null && request.getParameter("message") == null) {
			request.setAttribute("title", "ungültiger Aufruf");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		LectureDAOIf lectureDAO = DAOFactory.LectureDAOIf(session);
		Lecture lecture = lectureDAO.getLecture(Util.parseInteger(request.getParameter("lectureid"), 0));
		if (lecture == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Vorlesung nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), lecture);
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "operation not allowed");
			return;
		}

		Set<User> receipients = new LinkedHashSet<>();
		if (request.getParameterValues("toall") != null) {
			for (Participation lectureParticipation : lecture.getParticipants()) {
				receipients.add(lectureParticipation.getUser());
			}
		} else if (request.getParameterValues("gids") != null && request.getParameterValues("gids").length > 0) {
			GroupDAOIf groupDAO = DAOFactory.GroupDAOIf(session);
			for (String gid : request.getParameterValues("gids")) {
				if ("nogroup".equals(gid)) {
					for (Participation noGroupParticipation : participationDAO.getParticipationsWithoutGroup(lecture)) {
						receipients.add(noGroupParticipation.getUser());
					}
				} else {
					Group group = groupDAO.getGroup(Util.parseInteger(gid, 0));
					if (group != null && group.getLecture().getId() == lecture.getId()) {
						for (Participation groupParticipation : group.getMembers()) {
							receipients.add(groupParticipation.getUser());
						}
						for (Participation groupTutorParticipation : group.getTutors()) {
							receipients.add(groupTutorParticipation.getUser());
						}
					}
				}
			}
		}
		if (receipients.isEmpty()) {
			request.setAttribute("title", "Keine Empfänger ausgewählt");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}
		MailSender.MailOptions mailOptions = MailSender.newMailOptions().setReplyTo(participation.getUser().getFullName(), participation.getUser().getEmail());
		for (User receipient : receipients) {
			if (receipient.getUid() == participation.getUser().getUid()) {
				continue;
			}
			MailSender.sendMail(receipient.getEmail(), request.getParameter("subject"), request.getParameter("message").trim() + "\n\n-- \nGesendet von: " + participation.getUser().getFullName() + " <" + participation.getUser().getEmail() + ">", mailOptions);
		}

		final String[] fixedHeader = { "Name", "E-Mail" };
		final Path tmpDir = Util.createTemporaryDirectory("csv");
		final Path tmpFile = tmpDir.resolve("alle-empfaenger.csv");
		try (CSVWriter writer = new CSVWriter(Files.newBufferedWriter(tmpFile), ';', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
			writer.writeNext(fixedHeader, false);
			for (User receipient : receipients) {
				String[] line = new String[fixedHeader.length];
				int column = 0;
				line[column++] = receipient.getFullName();
				line[column++] = receipient.getEmail();
				writer.writeNext(line, false);
			}
		}
		MailSender.sendMail(participation.getUser().getEmail(), request.getParameter("subject"), request.getParameter("message").trim() + "\n\n-- \nGesendet von: " + participation.getUser().getFullName() + " <" + participation.getUser().getEmail() + ">\nDirect reply is not possible.", Arrays.asList(tmpFile), MailSender.newMailOptions().enableAutoSubmittedAutoGenerated());
		Util.recursiveDelete(tmpDir);

		request.setAttribute("title", "Mail gesendet");
		getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
	}
}
