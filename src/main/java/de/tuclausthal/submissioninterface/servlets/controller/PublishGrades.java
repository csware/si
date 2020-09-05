/*
 * Copyright 2011, 2020 Sven Strickroth <email@cs-ware.de>
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
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.MailSender;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for publishing points
 * @author Sven Strickroth
 */
public class PublishGrades extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.ADVISOR) != 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.getDeadline().after(Util.correctTimezone(new Date())) || task.getShowPoints() != null) {
			request.setAttribute("title", "Ungültige Anfrage");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		request.setAttribute("task", task);
		request.getRequestDispatcher("PublishGradesView").forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.ADVISOR) != 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.getDeadline().after(Util.correctTimezone(new Date())) || task.getShowPoints() != null) {
			request.setAttribute("title", "Ungültige Anfrage");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		Transaction tx = session.beginTransaction();
		session.buildLockRequest(LockOptions.UPGRADE).lock(task);
		if (request.getParameter("mail") != null) {
			String baseURI = Configuration.getInstance().getFullServletsURI();
			for (Submission submission : task.getSubmissions()) {
				for (Participation submitterParticipation : submission.getSubmitters()) {
					if (submission.getPoints() != null && submission.getPoints().getPointsOk()) {
						MailSender.sendMail(submitterParticipation.getUser().getEmail(), "Bewertung erfolgt", "Hallo " + submitterParticipation.getUser().getFullName() + ",\n\neine Ihrer Abgaben wurde bewertet.\n\nEinsehen: <"+ baseURI + "/ShowTask?taskid=" + task.getTaskid() + ">.\n\n-- \nReply is not possible.");
					}
				}
			}
		}
		task.setShowPoints(task.getDeadline());
		session.saveOrUpdate(task);
		tx.commit();

		response.sendRedirect(response.encodeRedirectURL("ShowTask?taskid=" + task.getTaskid()));
	}
}
