/*
 * Copyright 2009-2010, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.impl.LogDAO;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry.LogAction;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for deleting a file in a submission
 * @author Sven Strickroth
 *
 */
public class DeleteFile extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(Util.parseInteger(request.getParameter("sid"), 0));

		ContextAdapter contextAdapter = new ContextAdapter(getServletContext());

		if (submission == null) {
			request.setAttribute("title", "Abgabe nicht gefunden");
			request.getRequestDispatcher("/" + contextAdapter.getServletsPath() + "/MessageView").forward(request, response);
			return;
		}

		Task task = submission.getTask();

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || !submission.getSubmitters().contains(participation)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.getDeadline().before(Util.correctTimezone(new Date())) || (task.isAllowPrematureSubmissionClosing() && submission.isClosed())) {
			request.setAttribute("title", "Es sind keine Veränderungen an dieser Abgabe mehr möglich.");
			request.getRequestDispatcher("/" + contextAdapter.getServletsPath() + "/MessageView").forward(request, response);
			return;
		}

		if (request.getPathInfo() == null) {
			request.setAttribute("title", "Ungültige Anfrage");
			request.getRequestDispatcher("/" + contextAdapter.getServletsPath() + "/MessageView").forward(request, response);
			return;
		}

		File path = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
		File file = new File(path, request.getPathInfo().substring(1));
		Transaction tx = session.beginTransaction();
		session.buildLockRequest(LockOptions.UPGRADE).lock(submission);
		if (file.exists() && file.isFile() && file.delete()) {
			if (!submissionDAO.deleteIfNoFiles(submission, path)) {
				submission.setLastModified(new Date());
				submissionDAO.saveSubmission(submission);
			}
			tx.commit();
			new LogDAO(session).createLogEntry(participation.getUser(), null, submission.getTask(), LogAction.DELETE_FILE, null, null, request.getPathInfo().substring(1), null);

			response.sendRedirect(Util.generateRedirectURL(request.getContextPath() + "/" + contextAdapter.getServletsPath() + "/ShowTask?taskid=" + task.getTaskid(), response));
			return;
		}
		tx.rollback();

		request.setAttribute("title", "Datei nicht gefunden");
		request.getRequestDispatcher("/" + contextAdapter.getServletsPath() + "/MessageView").forward(request, response);
		return;
	}
}
