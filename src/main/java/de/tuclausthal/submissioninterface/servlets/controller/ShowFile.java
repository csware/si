/*
 * Copyright 2009-2012, 2017, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jakarta.mail.internet.MimeUtility;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for displaying the source of a file of a submission
 * @author Sven Strickroth
 *
 */
public class ShowFile extends HttpServlet {
	private final static long serialVersionUID = 1L;
	private final static String[] plainTextFiles = new String[] { "xml", "htm", "html", "jsp", "txt", "css", "js", "java", "c", "cpp", "h", "project", "classpath", "patch", "diff", "sql", "php", "pl", "rb", "tex", "log", "bib", "cfg", "sml", "lcirc" };
	private final static String[] inlineFiles = new String[] { "jpg", "jpeg", "png", "gif", "pdf" };

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(Util.parseInteger(request.getParameter("sid"), 0));

		if (submission == null) {
			request.setAttribute("title", "Abgabe nicht gefunden");
			request.getRequestDispatcher("/" + Configuration.getInstance().getServletsPath() + "/MessageView").forward(request, response);
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
			request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateHTMLLink("/" + Configuration.getInstance().getServletsPath() + "/ShowTask?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
			request.getRequestDispatcher("/" + Configuration.getInstance().getServletsPath() + "/MessageView").forward(request, response);
			return;
		}

		File file = new File(Configuration.getInstance().getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator") + request.getPathInfo().substring(1));
		if (file.exists() && file.isFile()) {
			if (isPlainTextFile(file.getName().toLowerCase()) && !"true".equals(request.getParameter("download"))) {
				// code for loading/displaying text-files
				StringBuffer code = Util.loadFile(file);

				request.setAttribute("submission", submission);
				request.setAttribute("code", code);
				request.setAttribute("fileName", Util.escapeHTML(file.getName()));
				request.getRequestDispatcher("/" + Configuration.getInstance().getServletsPath() + "/ShowFileView").forward(request, response);
			} else {
				if (file.getName().toLowerCase().endsWith(".pdf")) {
					response.setContentType("application/pdf");
				} else if (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpeg")) {
					response.setContentType("image/jpg");
				} else if (file.getName().toLowerCase().endsWith(".gif")) {
					response.setContentType("image/gif");
				} else if (file.getName().toLowerCase().endsWith(".png")) {
					response.setContentType("image/png");
				} else {
					response.setContentType("application/x-download");
					response.setHeader("Content-Disposition", "attachment; filename=\"" + MimeUtility.encodeWord(file.getName()) + "\"");
				}
				byte[] buffer = new byte[8000]; // should be equal to the Tomcat buffersize
				try (OutputStream out = response.getOutputStream(); BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
					int len = 0;
					while ((len = inputStream.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
				}
			}
			return;
		}

		request.setAttribute("title", "Datei/Pfad nicht gefunden");
		request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateHTMLLink("/" + Configuration.getInstance().getServletsPath() + "/ShowTask?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
		request.getRequestDispatcher("/" + Configuration.getInstance().getServletsPath() + "/MessageView").forward(request, response);
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
}
