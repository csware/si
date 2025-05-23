/*
 * Copyright 2011, 2020-2023, 2025 Sven Strickroth <email@cs-ware.de>
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
import java.time.ZonedDateTime;
import java.util.Iterator;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.servlets.view.PublishGradesView;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.MailSender;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for publishing points
 * @author Sven Strickroth
 */
@GATEController
public class PublishGrades extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.ADVISOR) != 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.getDeadline().isAfter(ZonedDateTime.now()) || task.getShowPoints() != null) {
			request.setAttribute("title", "Ungültige Anfrage");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		request.setAttribute("task", task);
		getServletContext().getNamedDispatcher(PublishGradesView.class.getSimpleName()).forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.ADVISOR) != 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		Transaction tx = session.beginTransaction();
		session.lock(task, LockMode.PESSIMISTIC_WRITE);

		if (task.getDeadline().isAfter(ZonedDateTime.now()) || task.getShowPoints() != null) {
			tx.rollback();
			request.setAttribute("title", "Ungültige Anfrage");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		if (request.getParameter("mail") != null) {
			String baseURI = Configuration.getInstance().getFullServletsURI();
			MailSender.MailOptions mailOptions = MailSender.newMailOptions().enableAutoSubmittedAutoGenerated();
			Iterator<Submission> submissionIterator = DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOrdered(task, false).iterator();
			while (submissionIterator.hasNext()) {
				Submission submission = submissionIterator.next();
				for (Participation submitterParticipation : submission.getSubmitters()) {
					if (submission.getPoints() != null && submission.getPoints().getPointsOk()) {
						MailSender.sendMail(submitterParticipation.getUser().getEmail(), "Bewertung erfolgt", "Hallo " + submitterParticipation.getUser().getFullName() + ",\n\neine Ihrer Abgaben wurde bewertet.\n\nEinsehen:\n<" + baseURI + ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid() + ">.\n\n-- \nReply is not possible.", mailOptions);
					}
				}
			}
		}
		task.setShowPoints(ZonedDateTime.now());
		tx.commit();

		response.sendRedirect(Util.generateRedirectURL(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response));
	}
}
