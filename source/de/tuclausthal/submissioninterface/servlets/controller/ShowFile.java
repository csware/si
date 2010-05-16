/*
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for displaying the source of a file of a submission
 * @author Sven Strickroth
 *
 */
public class ShowFile extends HttpServlet {
	private final static String[] plainTextFiles = new String[] { "xml", "htm", "html", "jsp", "txt", "css", "js", "java", "c", "cpp", "h", "project", "classpath", "patch", "diff", "sql", "php", "pl", "rb", "tex", "log", "bib", "cfg" };
	private final static String[] inlineFiles = new String[] { "jpg", "jpeg", "png", "gif", "pdf", "svg", "svgz" };

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = HibernateSessionHelper.getSessionFactory().openSession();
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
		Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(session), submission.getTask().getLecture());
		if (participation == null || (participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0 && !submission.getSubmitters().contains(participation))) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (request.getPathInfo() == null) {
			request.setAttribute("title", "Ungültige Anfrage");
			request.getRequestDispatcher("/" + contextAdapter.getServletsPath() + "/MessageView").forward(request, response);
			return;
		}

		File file = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator") + request.getPathInfo().substring(1));
		if (file.exists() && file.isFile()) {
			if (isPlainTextFile(file.getName().toLowerCase()) && !"true".equals(request.getParameter("download"))) {
				// code for loading/displaying text-files
				BufferedReader freader = new BufferedReader(new FileReader(file));
				String line;
				StringBuffer code = new StringBuffer();
				while ((line = freader.readLine()) != null) {
					code.append(line + "\n");
				}
				freader.close();

				request.setAttribute("submission", submission);
				request.setAttribute("code", code);
				request.setAttribute("fileName", Util.mknohtml(file.getName()));
				request.getRequestDispatcher("/" + contextAdapter.getServletsPath() + "/ShowFileView").forward(request, response);
			} else {
				if (file.getName().toLowerCase().endsWith(".pdf")) {
					response.setContentType("application/pdf");
				} else if (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpeg")) {
					response.setContentType("image/jpg");
				} else if (file.getName().toLowerCase().endsWith(".gif")) {
					response.setContentType("image/gif");
				} else if (file.getName().toLowerCase().endsWith(".png")) {
					response.setContentType("image/png");
				} else if (file.getName().toLowerCase().endsWith(".svg") || file.getName().toLowerCase().endsWith(".svgz")) {
					response.setContentType("image/svg+xml");
				} else {
					response.setContentType("application/x-download");
					response.setHeader("Content-Disposition", "attachment; filename=" + file.getName()); // TODO: escape!?, if good regexps for filenames are used, not necessary
				}
				OutputStream out = response.getOutputStream();
				byte[] buffer = new byte[8000]; // should be equal to the Tomcat buffersize
				BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
				int len = 0;
				while ((len = inputStream.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
				inputStream.close();
			}
			return;
		}

		request.setAttribute("title", "Datei/Pfad nicht gefunden");
		request.getRequestDispatcher("/" + contextAdapter.getServletsPath() + "/MessageView").forward(request, response);
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
