/*
 * Copyright 2009-2012, 2017, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jakarta.mail.internet.MimeUtility;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.servlets.view.ShowFileView;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for displaying the source of a file of a submission
 * @author Sven Strickroth
 *
 */
@GATEController(recursive = true)
public class ShowFile extends HttpServlet {
	private final static long serialVersionUID = 1L;
	private final static String[] plainTextFiles = new String[] { "xml", "htm", "html", "jsp", "txt", "css", "js", "java", "c", "cpp", "h", "hs", "project", "classpath", "patch", "diff", "sql", "php", "pl", "py", "rb", "tex", "log", "bib", "cfg", "sml", "lcirc" };
	private final static String[] inlineFiles = new String[] { "jpg", "jpeg", "png", "gif", "pdf" };

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
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), submission.getTask().getTaskGroup().getLecture());
		if (participation == null || (participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0 && !submission.getSubmitters().contains(participation))) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (request.getPathInfo() == null) {
			request.setAttribute("title", "Ungültige Anfrage");
			request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), request, response) + "\">zurück zur Aufgabe</a></div>");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		File path = new File(Configuration.getInstance().getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid());
		String relativeFile = FilenameUtils.normalize(request.getPathInfo().substring(1));
		if (relativeFile != null && !relativeFile.isEmpty()) {
			File file = new File(path, relativeFile);
			if (file.exists() && file.isFile()) {
				if (isPlainTextFile(file.getName().toLowerCase()) && !"true".equals(request.getParameter("download"))) {
					// code for loading/displaying text-files
					StringBuffer code = Util.loadFile(file);

					request.setAttribute("submission", submission);
					request.setAttribute("code", code);
					request.setAttribute("fileName", Util.escapeHTML(file.getName()));
					getServletContext().getNamedDispatcher(ShowFileView.class.getSimpleName()).forward(request, response);
					return;
				}
				setContentTypeBasedonFilenameExtension(response, file.getName(), "true".equals(request.getParameter("download")));
				try (OutputStream out = response.getOutputStream()) {
					FileUtils.copyFile(file, out);
				}
				return;
			}
		}

		request.setAttribute("title", "Datei/Pfad nicht gefunden");
		request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), request, response) + "\">zurück zur Aufgabe</a></div>");
		getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
	}

	private boolean isPlainTextFile(String lowercaseFilename) {
		for (String extension : plainTextFiles) {
			if (lowercaseFilename.endsWith("." + extension)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isInlineAble(String lowercaseFilename) {
		for (String extension : plainTextFiles) {
			if (lowercaseFilename.endsWith("." + extension)) {
				return true;
			}
		}
		for (String extension : inlineFiles) {
			if (lowercaseFilename.endsWith("." + extension)) {
				return true;
			}
		}
		return false;
	}

	public static void setContentTypeBasedonFilenameExtension(HttpServletResponse response, String filename, boolean forceDownload) throws UnsupportedEncodingException {
		String filenameLowerCase = filename.toLowerCase();
		if (filenameLowerCase.endsWith(".pdf")) {
			response.setContentType("application/pdf");
		} else if (filenameLowerCase.endsWith(".jpg") || filenameLowerCase.endsWith(".jpeg")) {
			response.setContentType("image/jpg");
		} else if (filenameLowerCase.endsWith(".gif")) {
			response.setContentType("image/gif");
		} else if (filenameLowerCase.endsWith(".png")) {
			response.setContentType("image/png");
		} else if (filenameLowerCase.endsWith(".zip")) {
			response.setContentType("application/zip");
		} else if (filenameLowerCase.endsWith(".txt")) {
			response.setContentType("text/plain");
		} else if (filenameLowerCase.endsWith(".csv")) {
			response.setContentType("text/csv");
		} else {
			response.setContentType("application/octet-stream");
			forceDownload = true;
		}
		if (forceDownload) {
			response.setHeader("Content-Disposition", "attachment; filename=\"" + MimeUtility.encodeWord(filename) + "\"");
		}
	}
}
