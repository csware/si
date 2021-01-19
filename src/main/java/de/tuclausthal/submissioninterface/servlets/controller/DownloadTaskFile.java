/*
 * Copyright 2010-2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for displaying the source of a file of a submission
 * @author Sven Strickroth
 *
 */
public class DownloadTaskFile extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));

		ContextAdapter contextAdapter = new ContextAdapter(getServletContext());

		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			request.getRequestDispatcher("/" + contextAdapter.getServletsPath() + "/MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (request.getPathInfo() == null) {
			request.setAttribute("title", "Ungültige Anfrage");
			request.getRequestDispatcher("/" + contextAdapter.getServletsPath() + "/MessageView").forward(request, response);
			return;
		}

		File file = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + "advisorfiles" + System.getProperty("file.separator") + request.getPathInfo().substring(1));
		if (file.exists() && file.isFile()) {
			if ("delete".equals(request.getParameter("action"))) {
				if (participation.getRoleType() != ParticipationRole.ADVISOR) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
				} else {
					file.delete();
					response.sendRedirect(Util.generateRedirectURL(getServletContext().getContextPath() + "/" + contextAdapter.getServletsPath() + "/TaskManager?lecture=" + task.getTaskGroup().getLecture().getId() + "&action=editTask&taskid=" + task.getTaskid(), response));
					return;
				}
			}
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + MimeUtility.encodeWord(file.getName()) + "\"");
			OutputStream out = response.getOutputStream();
			byte[] buffer = new byte[8000]; // should be equal to the Tomcat buffersize
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			int len = 0;
			while ((len = inputStream.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			inputStream.close();
			return;
		}

		request.setAttribute("title", "Datei/Pfad nicht gefunden");
		request.getRequestDispatcher("/" + contextAdapter.getServletsPath() + "/MessageView").forward(request, response);
	}
}
