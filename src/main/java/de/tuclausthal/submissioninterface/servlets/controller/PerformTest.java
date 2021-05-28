/*
 * Copyright 2009-2015, 2017, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for performing a test (tutor or advisor)
 * @author Sven Strickroth
 */
@MultipartConfig(maxFileSize = Configuration.MAX_UPLOAD_SIZE)
public class PerformTest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("task", task);

		getServletContext().getNamedDispatcher("PerformTestTutorFormView").forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Template template = TemplateFactory.getTemplate(request, response);

		Test test = DAOFactory.TestDAOIf(session).getTest(Util.parseInteger(request.getParameter("testid"), 0));
		if (test == null) {
			template.printTemplateHeader("Ungültige Anfrage");
			PrintWriter out = response.getWriter();
			out.println("Test nicht gefunden.");
			template.printTemplateFooter();
			return;
		}

		Task task = test.getTask();

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.isShowTextArea() == false && "-".equals(task.getFilenameRegexp())) {
			template.printTemplateHeader("Ungültige Anfrage");
			PrintWriter out = response.getWriter();
			out.println("<div class=mid>Das Einsenden von Lösungen ist für diese Aufgabe deaktiviert.</div>");
			template.printTemplateFooter();
			return;
		}
		if ("-".equals(task.getFilenameRegexp())) {
			template.printTemplateHeader("Ungültige Anfrage");
			PrintWriter out = response.getWriter();
			out.println("<div class=mid>Dateiupload ist für diese Aufgabe deaktiviert.</div>");
			template.printTemplateFooter();
			return;
		}

		if (request.getParameter("sid") != null || (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) && request.getParameter("modelsolution") != null) {
			request.setAttribute("task", test.getTask());
			request.setAttribute("test", test);

			File path = Util.createTemporaryDirectory("tutortest");
			if (path == null) {
				throw new IOException("Failed to create tempdir!");
			}

			final File taskPath = new File(Configuration.getInstance().getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid());
			if (request.getParameter("sid") != null) {
				SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
				Submission submission = submissionDAO.getSubmission(Util.parseInteger(request.getParameter("sid"), 0));
				if (submission == null || submission.getTask().getTaskid() != task.getTaskid()) {
					request.setAttribute("title", "Abgabe nicht gefunden");
					getServletContext().getNamedDispatcher("MessageView").forward(request, response);
					return;
				}

				File submissionPath = new File(taskPath, String.valueOf(submission.getSubmissionid()));
				// prepare tempdir
				Util.recursiveCopy(submissionPath, path);
			} else {
				final File modelSolutionPath = new File(taskPath, "modelsolutionfiles");
				// prepare tempdir
				if (modelSolutionPath.isDirectory()) {
					Util.recursiveCopy(modelSolutionPath, path);
				}
			}

			TestTask testTask = new TestTask(test);
			TestExecutorTestResult testResult = new TestExecutorTestResult();
			testTask.performTaskInFolder(test, Configuration.getInstance().getDataPath(), path, testResult);

			Util.recursiveDelete(path);

			request.setAttribute("testresult", testResult);
			getServletContext().getNamedDispatcher("PerformTestResultView").forward(request, response);
			return;
		}

		if (request.getPart("file") == null) {
			template.printTemplateHeader("Invalid request");
			template.printTemplateFooter();
			return;
		}
		long fileParts = request.getParts().stream().filter(part -> "file".equals(part.getName())).count();
		if (fileParts == 0) {
			request.setAttribute("title", "Keine Datei gefunden.");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}
		if (fileParts > 1 && fileParts != request.getParts().stream().filter(part -> "file".equals(part.getName())).map(part -> Util.getUploadFileName(part)).collect(Collectors.toSet()).size()) {
			request.setAttribute("title", "Mehrere Dateien mit identischem Namen im Upload gefunden.");
			request.setAttribute("message", "<div class=mid><a href=\"javascript:window.history.back();\">zurück zur vorherigen Seite</a></div>");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		File path = Util.createTemporaryDirectory("tutortest");
		if (path == null) {
			throw new IOException("Failed to create tempdir!");
		}

		// Process the uploaded item
		for (Part file : request.getParts()) {
			if (!file.getName().equalsIgnoreCase("file")) {
				continue;
			}
			StringBuffer submittedFileName = new StringBuffer(Util.getUploadFileName(file));
			Util.lowerCaseExtension(submittedFileName);
			String fileName = null;
			for (Pattern pattern : SubmitSolution.getTaskFileNamePatterns(task, false)) {
				Matcher m = pattern.matcher(submittedFileName);
				if (!m.matches()) {
					template.printTemplateHeader("Dateiname ungültig", task);
					PrintWriter out = response.getWriter();
					out.println("Dateiname ungültig bzw. entspricht nicht der Vorgabe (ist ein Klassenname vorgegeben, so muss die Datei genauso heißen).<br>Tipp: Nur A-Z, a-z, 0-9, ., - und _ sind erlaubt. Evtl. muss der Dateiname mit einem Großbuchstaben beginnen und darf keine Leerzeichen enthalten.");
					out.println("<br>Für Experten: Der Dateiname muss dem folgenden regulären Ausdruck genügen: " + Util.escapeHTML(pattern.pattern()));
					out.println("<p><div class=mid><a href=\"javascript:window.history.back();\">zurück zur Abgabeseite</a></div>");
					template.printTemplateFooter();
					Util.recursiveDelete(path);
					return;
				}
				fileName = m.group(1);
			}
			try {
				SubmitSolution.handleUploadedFile(LOG, path, task, fileName, file);
			} catch (IOException e) {
				LOG.error("Problem on processing uploaded file.", e);
				template.printTemplateHeader("Ungültige Anfrage", task);
				PrintWriter out = response.getWriter();
				out.println("Problem beim Speichern der Daten.");
				out.println("<p><div class=mid><a href=\"javascript:window.history.back();\">zurück zur Abgabeseite</a></div>");
				template.printTemplateFooter();
				Util.recursiveDelete(path);
				return;
			}
		}

		request.setAttribute("task", test.getTask());
		request.setAttribute("test", test);

		TestTask testTask = new TestTask(test);
		TestExecutorTestResult testResult = new TestExecutorTestResult();
		testTask.performTaskInFolder(test, Configuration.getInstance().getDataPath(), path, testResult);

		Util.recursiveDelete(path);

		request.setAttribute("testresult", testResult);
		getServletContext().getNamedDispatcher("PerformTestResultView").forward(request, response);
	}
}
