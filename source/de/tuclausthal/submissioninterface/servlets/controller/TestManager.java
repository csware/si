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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.CompileTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.RegExpTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for managing (add, edit, remove) function tests by advisors
 * @author Sven Strickroth
 */
public class TestManager extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = HibernateSessionHelper.getSession();
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(session), task.getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		ContextAdapter contextAdapter = new ContextAdapter(getServletContext());

		if ("newTest".equals(request.getParameter("action"))) {
			request.setAttribute("task", task);
			request.getRequestDispatcher("TestManagerAddTestFormView").forward(request, response);
		} else if ("saveNewTest".equals(request.getParameter("action")) && "junit".equals(request.getParameter("type"))) {
			// Check that we have a file upload request
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);

			session.beginTransaction();
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			JUnitTest test = testDAO.createJUnitTest(task);

			if (isMultipart) {

				// Create a factory for disk-based file items
				FileItemFactory factory = new DiskFileItemFactory();

				// Set factory constraints
				//factory.setSizeThreshold(yourMaxMemorySize);
				//factory.setRepository(yourTempDirectory);

				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(factory);

				// Parse the request
				List<FileItem> items = null;
				try {
					items = upload.parseRequest(request);
				} catch (FileUploadException e) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "filename invalid");
					session.getTransaction().rollback();
					return;
				}

				File path = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator"));
				if (path.exists() == false) {
					path.mkdirs();
				}
				int timesRunnableByStudents = 0;
				int timeout = 15;
				boolean tutortest = false;
				String title = "";
				String description = "";
				// Process the uploaded items
				Iterator<FileItem> iter = items.iterator();
				while (iter.hasNext()) {
					FileItem item = iter.next();

					// Process a file upload
					if (!item.isFormField()) {
						if (!item.getName().endsWith(".jar")) {
							request.setAttribute("title", "Dateiname ungültig.");
							request.getRequestDispatcher("MessageView").forward(request, response);
							session.getTransaction().rollback();
							return;
						}
						File uploadedFile = new File(path, "junittest" + test.getId() + ".jar");
						try {
							item.write(uploadedFile);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						if ("timesRunnableByStudents".equals(item.getFieldName())) {
							timesRunnableByStudents = Util.parseInteger(item.getString(), 0);
						} else if ("title".equals(item.getFieldName())) {
							title = item.getString();
						} else if ("description".equals(item.getFieldName())) {
							description = item.getString();
						} else if ("tutortest".equals(item.getFieldName())) {
							tutortest = true;
						} else if ("timeout".equals(item.getFieldName())) {
							timeout = Util.parseInteger(item.getString(), 15);
						}
					}
				}

				test.setTimesRunnableByStudents(timesRunnableByStudents);
				test.setForTutors(tutortest);
				test.setTestTitle(title);
				test.setTestDescription(description);
				test.setTimeout(timeout);
				testDAO.saveTest(test);
				session.getTransaction().commit();
				response.sendRedirect(response.encodeRedirectURL("TaskManager?action=editTask&lecture=" + task.getLecture().getId() + "&taskid=" + task.getTaskid()));
			} else {
				request.setAttribute("title", "Ungültiger Aufruf");
				request.getRequestDispatcher("MessageView").forward(request, response);
			}
		} else if ("saveNewTest".equals(request.getParameter("action")) && "regexp".equals(request.getParameter("type"))) {
			//check regexp
			try {
				Pattern.compile(request.getParameter("regexp"));
			} catch (PatternSyntaxException e) {
				request.setAttribute("title", "Ungültiger regulärer Ausdruck");
				request.getRequestDispatcher("MessageView").forward(request, response);
				return;
			}
			// store it
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			session.beginTransaction();
			RegExpTest test = testDAO.createRegExpTest(task);
			test.setMainClass(request.getParameter("mainclass"));
			test.setCommandLineParameter(request.getParameter("parameter"));
			test.setTimeout(Util.parseInteger(request.getParameter("timeout"), 15));
			test.setRegularExpression(request.getParameter("regexp"));
			test.setTimesRunnableByStudents(Util.parseInteger(request.getParameter("timesRunnableByStudents"), 0));
			test.setTimesRunnableByStudents(Util.parseInteger(request.getParameter("timesRunnableByStudents"), 0));
			test.setForTutors(request.getParameter("tutortest") != null);
			test.setTestTitle(request.getParameter("title"));
			test.setTestDescription(request.getParameter("description"));
			testDAO.saveTest(test);
			session.getTransaction().commit();
			response.sendRedirect(response.encodeRedirectURL("TaskManager?action=editTask&lecture=" + task.getLecture().getId() + "&taskid=" + task.getTaskid()));
		} else if ("saveNewTest".equals(request.getParameter("action")) && "compile".equals(request.getParameter("type"))) {
			// store it
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			session.beginTransaction();
			CompileTest test = testDAO.createCompileTest(task);
			test.setTimesRunnableByStudents(Util.parseInteger(request.getParameter("timesRunnableByStudents"), 0));
			test.setForTutors(request.getParameter("tutortest") != null);
			test.setTestTitle(request.getParameter("title"));
			test.setTestDescription(request.getParameter("description"));
			testDAO.saveTest(test);
			session.getTransaction().commit();
			response.sendRedirect(response.encodeRedirectURL("TaskManager?action=editTask&lecture=" + task.getLecture().getId() + "&taskid=" + task.getTaskid()));
		} else if ("deleteTest".equals(request.getParameter("action"))) {
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			session.beginTransaction();
			Test test = testDAO.getTestLocked(Util.parseInteger(request.getParameter("testid"), 0));
			if (test != null) {
				testDAO.deleteTest(test);
			}
			session.getTransaction().commit();
			response.sendRedirect(response.encodeRedirectURL("TaskManager?action=editTask&lecture=" + task.getLecture().getId() + "&taskid=" + task.getTaskid()));
			return;
		} else if ("rerunTest".equals(request.getParameter("action"))) {
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			session.beginTransaction();
			Test test = testDAO.getTestLocked(Util.parseInteger(request.getParameter("testid"), 0));
			if (test != null) {
				test.setNeedsToRun(true);
				testDAO.saveTest(test);
			}
			session.getTransaction().commit();
			response.sendRedirect(response.encodeRedirectURL("TaskManager?action=editTask&lecture=" + task.getLecture().getId() + "&taskid=" + task.getTaskid()));
			return;
		} else {
			request.setAttribute("title", "Ungültiger Aufruf");
			request.getRequestDispatcher("MessageView").forward(request, response);

		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
