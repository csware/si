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

import org.apache.commons.io.FilenameUtils;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.impl.LogDAO;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.DeleteFileView;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for deleting a file in a submission
 * @author Sven Strickroth
 *
 */
@GATEController(recursive = true)
public class DeleteFile extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(Util.parseInteger(request.getParameter("sid"), 0));

		if (submission == null) {
			request.setAttribute("title", "Abgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
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

		if (task.getDeadline().before(new Date()) || (task.isAllowPrematureSubmissionClosing() && submission.isClosed())) {
			request.setAttribute("title", "Es sind keine Veränderungen an dieser Abgabe mehr möglich.");
			request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), request, response) + "\">zurück zur Aufgabe</a></div>");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		if (request.getPathInfo() == null) {
			request.setAttribute("title", "Ungültige Anfrage");
			request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), request, response) + "\">zurück zur Aufgabe</a></div>");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		File path = new File(Configuration.getInstance().getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
		String relativeFile = FilenameUtils.normalize(request.getPathInfo().substring(1));
		if (relativeFile != null && !relativeFile.isEmpty()) {
			File file = new File(path, relativeFile);
			if (file.exists() && file.isFile()) {
				request.setAttribute("submission", submission);
				request.setAttribute("filename", relativeFile);
				getServletContext().getNamedDispatcher(DeleteFileView.class.getSimpleName()).forward(request, response);
				return;
			}
		}

		request.setAttribute("title", "Datei nicht gefunden");
		getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(Util.parseInteger(request.getParameter("sid"), 0));

		if (submission == null) {
			request.setAttribute("title", "Abgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
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

		if (task.getDeadline().before(new Date()) || (task.isAllowPrematureSubmissionClosing() && submission.isClosed())) {
			request.setAttribute("title", "Es sind keine Veränderungen an dieser Abgabe mehr möglich.");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		if (request.getPathInfo() == null) {
			request.setAttribute("title", "Ungültige Anfrage");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		File path = new File(Configuration.getInstance().getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
		String relativeFile = FilenameUtils.normalize(request.getPathInfo().substring(1));
		if (relativeFile != null && !relativeFile.isEmpty()) {
			File file = new File(path, relativeFile);
			Transaction tx = session.beginTransaction();
			session.buildLockRequest(LockOptions.UPGRADE).lock(submission);
			if (file.exists() && file.isFile() && file.delete()) {
				if (!submissionDAO.deleteIfNoFiles(submission, path)) {
					submission.setLastModified(new Date());
					submissionDAO.saveSubmission(submission);
				}
				new LogDAO(session).createLogDeleteEntryTransaction(participation.getUser(), submission.getTask(), relativeFile);
				tx.commit();

				response.sendRedirect(Util.generateAbsoluteServletsRedirectURL(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), request, response));
				return;
			}
			tx.rollback();
		}

		request.setAttribute("title", "Datei nicht gefunden");
		request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), request, response) + "\">zurück zur Aufgabe</a></div>");
		getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
	}
}
