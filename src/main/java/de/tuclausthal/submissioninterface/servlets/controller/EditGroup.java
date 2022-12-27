/*
 * Copyright 2009-2010, 2012, 2020-2022 Sven Strickroth <email@cs-ware.de>
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

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.GroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.EditGroupFormView;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for adding and removing members from/to groups
 * @author Sven Strickroth
 *
 */
@GATEController
public class EditGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		GroupDAOIf groupDAO = DAOFactory.GroupDAOIf(session);
		Group group = groupDAO.getGroup(Util.parseInteger(request.getParameter("groupid"), 0));
		if (group == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Gruppe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), group.getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("participation", participation);
		request.setAttribute("group", group);
		getServletContext().getNamedDispatcher(EditGroupFormView.class.getSimpleName()).forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		GroupDAOIf groupDAO = DAOFactory.GroupDAOIf(session);
		Group group = groupDAO.getGroup(Util.parseInteger(request.getParameter("groupid"), 0));
		if (group == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Gruppe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), group.getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ("removeFromGroup".equals(request.getParameter("action")) && request.getParameter("participationid") != null) {
			Transaction tx = session.beginTransaction();
			Participation memberParticipation = participationDAO.getParticipationLocked(Util.parseInteger(request.getParameter("participationid"), 0));
			if (memberParticipation != null && memberParticipation.getGroup() != null && memberParticipation.getGroup().getGid() == group.getGid() && (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0 || memberParticipation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0)) {
				memberParticipation.setGroup(null);
			}
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(ShowLecture.class.getSimpleName() + "?lecture=" + group.getLecture().getId() + "#group" + group.getGid(), response));
			return;
		} else if ("removeTutorFromGroup".equals(request.getParameter("action"))) {
			if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) {
				Transaction tx = session.beginTransaction();
				session.lock(group, LockMode.PESSIMISTIC_WRITE);
				Participation memberParticipation = participationDAO.getParticipationLocked(Util.parseInteger(request.getParameter("participationid"), 0));
				if (memberParticipation != null) {
					group.getTutors().remove(memberParticipation);
				}
				tx.commit();
				response.sendRedirect(Util.generateRedirectURL(ShowLecture.class.getSimpleName() + "?lecture=" + group.getLecture().getId() + "#group" + group.getGid(), response));
				return;
			}
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		} else if ("editGroup".equals(request.getParameter("action"))) { // add member or edit group
			Transaction tx = session.beginTransaction();
			session.lock(group, LockMode.PESSIMISTIC_WRITE);
			session.lock(participation, LockMode.PESSIMISTIC_WRITE);
			if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) {
				group.setName(request.getParameter("title"));
				group.setAllowStudentsToSignup(request.getParameter("allowStudentsToSignup") != null);
				group.setAllowStudentsToQuit(request.getParameter("allowStudentsToQuit") != null);
				group.setSubmissionGroup(request.getParameter("submissionGroup") != null);
				group.setMaxStudents(Util.parseInteger(request.getParameter("maxStudents"), 0));
				group.setMembersVisibleToStudents(request.getParameter("membersvisible") != null);

				// add tutors
				if (request.getParameterValues("tutors") != null && request.getParameterValues("tutors").length > 0) {
					for (String newMember : request.getParameterValues("tutors")) {
						Participation memberParticipation = participationDAO.getParticipationLocked(Util.parseInteger(newMember, 0));
						if (memberParticipation != null && memberParticipation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0 && memberParticipation.getLecture().getId() == group.getLecture().getId()) {
							group.getTutors().add(memberParticipation);
						}
					}
				}
			}
			if (request.getParameterValues("members") != null && request.getParameterValues("members").length > 0) {
				for (String newMember : request.getParameterValues("members")) {
					Participation memberParticipation = participationDAO.getParticipationLocked(Util.parseInteger(newMember, 0));
					if (memberParticipation != null && memberParticipation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0 && memberParticipation.getLecture().getId() == group.getLecture().getId()) {
						memberParticipation.setGroup(group);
					}
				}
			}
			if (request.getParameter("membersmailadresses") != null && !request.getParameter("membersmailadresses").trim().isEmpty()) {
				UserDAOIf userDAO = DAOFactory.UserDAOIf(session);
				int count = 0;
				List<String> errors = new ArrayList<>();
				String mailadresses[] = request.getParameter("membersmailadresses").replaceAll("\r\n", "\n").split("\n");
				for (String mailaddress : mailadresses) {
					if (mailaddress.isEmpty()) {
						continue;
					}
					User user = userDAO.getUserByEmail(mailaddress);
					if (user == null) {
						errors.add("\"" + mailaddress + "\" nicht gefunden.");
						continue;
					}
					Participation memberParticipation = participationDAO.getParticipationLocked(user, group.getLecture());
					if (memberParticipation == null) {
						errors.add("\"" + mailaddress + "\" ist kein Teilnehmer der Veranstaltung.");
						continue;
					}
					if (!memberParticipation.getRoleType().equals(ParticipationRole.NORMAL)) {
						errors.add("\"" + mailaddress + "\" ist kein normaler Teilnehmer der Veranstaltung.");
						continue;
					}
					if (memberParticipation.getGroup() != null) {
						errors.add("\"" + mailaddress + "\" ist bereits in einer anderen Gruppe.");
						continue;
					}
					memberParticipation.setGroup(group);
					++count;
				}
				tx.commit();
				StringBuilder output = new StringBuilder();
				if (!errors.isEmpty()) {
					output.append("<h2>Fehler</h2><ul>");
					for (String string : errors) {
						output.append("<li>" + Util.escapeHTML(string) + "</li>");
					}
					output.append("</ul>");
				}
				output.append("<h2>Ergebnis</h2>");
				output.append("<p>Studierende zur Gruppe hinzugefügt: " + count + "</p>");
				output.append("<p class=mid><a href=\"" + Util.generateHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + group.getLecture().getId() + "#group" + group.getGid(), response) + "\">zurück zur Vorlesung</a></p>");
				request.setAttribute("title", "Batch-Ergebnisse");
				request.setAttribute("message", output.toString());
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			tx.commit(); // attention, there is a commit right before this
			response.sendRedirect(Util.generateRedirectURL(ShowLecture.class.getSimpleName() + "?lecture=" + group.getLecture().getId() + "#group" + group.getGid(), response));
			return;
		}
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "invalid request");
	}
}
