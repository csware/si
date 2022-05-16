/*
 * Copyright 2009-2012, 2015, 2020-2022 Sven Strickroth <email@cs-ware.de>
 *
 * Copyright 2011 Joachim Schramm
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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.ChecklistTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommentsMetricTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.CompileTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.RegExpTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.UMLConstraintTest;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.servlets.view.TestManagerAddTestFormView;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for managing (add, edit, remove) function tests by advisors
 * @author Sven Strickroth
 */
@MultipartConfig(maxFileSize = Configuration.MAX_UPLOAD_SIZE)
@GATEController
public class TestManager extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ("newTest".equals(request.getParameter("action"))) {
			request.setAttribute("task", task);
			getServletContext().getNamedDispatcher(TestManagerAddTestFormView.class.getSimpleName()).forward(request, response);
		} else {
			request.setAttribute("title", "Ungültiger Aufruf");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ("saveNewTest".equals(request.getParameter("action")) && "advancedjavaio".equals(request.getParameter("type"))) {
			session.beginTransaction();
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			JavaAdvancedIOTest test = testDAO.createJavaAdvancedIOTest(task);

			int timesRunnableByStudents = Util.parseInteger(request.getParameter("timesRunnableByStudents"), 0);
			int timeout = Util.parseInteger(request.getParameter("timeout"), 15);
			boolean tutortest = request.getParameter("tutortest") != null;
			String title = request.getParameter("title");
			String description = request.getParameter("description");

			test.setTimesRunnableByStudents(timesRunnableByStudents);
			test.setForTutors(tutortest);
			test.setTestTitle(title);
			test.setTestDescription(description);
			test.setTimeout(timeout);
			test.setGiveDetailsToStudents(request.getParameter("giveDetailsToStudents") != null);
			session.getTransaction().commit();
			response.sendRedirect(Util.generateRedirectURL(JavaAdvancedIOTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
		} else if ("saveNewTest".equals(request.getParameter("action")) && "docker".equals(request.getParameter("type"))) {
			session.beginTransaction();
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			DockerTest test = testDAO.createDockerTest(task);

			int timesRunnableByStudents = Util.parseInteger(request.getParameter("timesRunnableByStudents"), 0);
			int timeout = Util.parseInteger(request.getParameter("timeout"), 15);
			boolean tutortest = request.getParameter("tutortest") != null;
			String title = request.getParameter("title");
			String description = request.getParameter("description");
			String preparationcode = request.getParameter("preparationcode");

			test.setTimesRunnableByStudents(timesRunnableByStudents);
			test.setForTutors(tutortest);
			test.setTestTitle(title);
			test.setTestDescription(description);
			test.setTimeout(timeout);
			test.setGiveDetailsToStudents(request.getParameter("giveDetailsToStudents") != null);
			test.setPreparationShellCode(preparationcode.replaceAll("\r\n", "\n"));
			session.getTransaction().commit();
			response.sendRedirect(Util.generateRedirectURL(DockerTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
		} else if ("saveNewTest".equals(request.getParameter("action")) && "checklist".equals(request.getParameter("type"))) {
			session.beginTransaction();
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			ChecklistTest test = testDAO.createChecklistTest(task);

			int timesRunnableByStudents = Util.parseInteger(request.getParameter("timesRunnableByStudents"), 0);
			String title = request.getParameter("title");
			String description = request.getParameter("description");

			test.setTimesRunnableByStudents(timesRunnableByStudents);
			test.setForTutors(false);
			test.setGiveDetailsToStudents(true);
			test.setTestTitle(title);
			test.setTestDescription(description);
			session.getTransaction().commit();
			response.sendRedirect(Util.generateRedirectURL(ChecklistTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
		} else if ("saveNewTest".equals(request.getParameter("action")) && "umlConstraint".equals(request.getParameter("type"))) {
			// Check that we have a file upload request

			session.beginTransaction();
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			UMLConstraintTest test = testDAO.createUMLConstraintTest(task);

			File path = new File(Configuration.getInstance().getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator"));
			if (path.exists() == false) {
				path.mkdirs();
			}
			int timesRunnableByStudents = Util.parseInteger(request.getParameter("timesRunnableByStudents"), 0);
			int timeout = Util.parseInteger(request.getParameter("timeout"), 15);
			boolean tutortest = request.getParameter("tutortest") != null;
			String title = request.getParameter("title");
			String description = request.getParameter("description");

			Part file = request.getPart("testcase");
			if (file == null || !Util.getUploadFileName(file).endsWith(".xmi")) {
				request.setAttribute("title", "Dateiname ungültig.");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				session.getTransaction().rollback();
				return;
			}
			File uploadedFile = new File(path, "musterloesung" + test.getId() + ".xmi");
			Util.copyInputStreamAndClose(file.getInputStream(), uploadedFile);

			test.setTimesRunnableByStudents(timesRunnableByStudents);
			test.setForTutors(tutortest);
			test.setTestTitle(title);
			test.setTestDescription(description);
			test.setTimeout(timeout);
			test.setGiveDetailsToStudents(true);
			session.getTransaction().commit();
			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?action=editTask&lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid(), response));
		} else if ("saveNewTest".equals(request.getParameter("action")) && "junit".equals(request.getParameter("type"))) {
			// Check that we have a file upload request

			session.beginTransaction();
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			JUnitTest test = testDAO.createJUnitTest(task);

			File path = new File(Configuration.getInstance().getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator"));
			if (path.exists() == false) {
				path.mkdirs();
			}
			int timesRunnableByStudents = Util.parseInteger(request.getParameter("timesRunnableByStudents"), 0);
			boolean giveDetailsToStudents = request.getParameter("giveDetailsToStudents") != null;
			int timeout = Util.parseInteger(request.getParameter("timeout"), 15);
			boolean tutortest = request.getParameter("tutortest") != null;
			String title = request.getParameter("title");
			String description = request.getParameter("description");
			String mainclass = request.getParameter("mainclass") == null ? "AllTests" : request.getParameter("mainclass");

			Part file = request.getPart("testcase");
			if (file == null || !Util.getUploadFileName(file).endsWith(".jar")) {
				request.setAttribute("title", "Dateiname ungültig.");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				session.getTransaction().rollback();
				return;
			}
			File uploadedFile = new File(path, "junittest" + test.getId() + ".jar");
			Util.copyInputStreamAndClose(file.getInputStream(), uploadedFile);

			test.setTimesRunnableByStudents(timesRunnableByStudents);
			test.setMainClass(mainclass);
			test.setForTutors(tutortest);
			test.setTestTitle(title);
			test.setTestDescription(description);
			test.setGiveDetailsToStudents(giveDetailsToStudents);
			test.setTimeout(timeout);
			session.getTransaction().commit();
			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?action=editTask&lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid(), response));
		} else if ("saveNewTest".equals(request.getParameter("action")) && "regexp".equals(request.getParameter("type"))) {
			//check regexp
			try {
				Pattern.compile(request.getParameter("regexp"));
			} catch (PatternSyntaxException e) {
				request.setAttribute("title", "Ungültiger regulärer Ausdruck");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
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
			test.setForTutors(request.getParameter("tutortest") != null);
			test.setGiveDetailsToStudents(request.getParameter("giveDetailsToStudents") != null);
			test.setTestTitle(request.getParameter("title"));
			test.setTestDescription(request.getParameter("description"));
			session.getTransaction().commit();
			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?action=editTask&lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid(), response));
		} else if ("saveNewTest".equals(request.getParameter("action")) && "compile".equals(request.getParameter("type"))) {
			// store it
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			session.beginTransaction();
			CompileTest test = testDAO.createCompileTest(task);
			test.setTimesRunnableByStudents(Util.parseInteger(request.getParameter("timesRunnableByStudents"), 0));
			test.setForTutors(request.getParameter("tutortest") != null);
			test.setGiveDetailsToStudents(request.getParameter("giveDetailsToStudents") != null);
			test.setTestTitle(request.getParameter("title"));
			test.setTestDescription(request.getParameter("description"));
			session.getTransaction().commit();
			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?action=editTask&lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid(), response));
		} else if ("saveNewTest".equals(request.getParameter("action")) && "commentmetric".equals(request.getParameter("type"))) {
			// store it
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			session.beginTransaction();
			CommentsMetricTest test = testDAO.createCommentsMetricTest(task);
			test.setTimesRunnableByStudents(Util.parseInteger(request.getParameter("timesRunnableByStudents"), 0));
			test.setForTutors(request.getParameter("tutortest") != null);
			test.setTestTitle(request.getParameter("title"));
			int minProzent = Util.parseInteger(request.getParameter("minProzent"), 5);
			if (minProzent < 1 || minProzent > 100) {
				minProzent = 5;
			}
			test.setMinProzent(minProzent);
			test.setExcludedFiles(request.getParameter("excludedFiles"));
			test.setTestDescription(request.getParameter("description"));
			test.setGiveDetailsToStudents(request.getParameter("giveDetailsToStudents") != null);
			session.getTransaction().commit();
			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?action=editTask&lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid(), response));
		} else if ("deleteTest".equals(request.getParameter("action"))) {
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			session.beginTransaction();
			Test test = testDAO.getTestLocked(Util.parseInteger(request.getParameter("testid"), 0));
			if (test != null && test.getTask().getTaskid() == task.getTaskid()) {
				testDAO.deleteTest(test);
			}
			session.getTransaction().commit();
			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?action=editTask&lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid(), response));
			return;
		} else if ("rerunTest".equals(request.getParameter("action"))) {
			TestDAOIf testDAO = DAOFactory.TestDAOIf(session);
			session.beginTransaction();
			Test test = testDAO.getTestLocked(Util.parseInteger(request.getParameter("testid"), 0));
			if (test != null && test.getTask().getTaskid() == task.getTaskid()) {
				test.setNeedsToRun(true);
			}
			session.getTransaction().commit();
			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?action=editTask&lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid(), response));
			return;
		} else {
			request.setAttribute("title", "Ungültiger Aufruf");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);

		}
	}
}
