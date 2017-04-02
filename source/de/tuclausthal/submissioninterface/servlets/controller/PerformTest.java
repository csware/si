/*
 * Copyright 2009-2015, 2017 Sven Strickroth <email@cs-ware.de>
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for performing a test (tutor or advisor)
 * @author Sven Strickroth
 */
@MultipartConfig
public class PerformTest extends HttpServlet {
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
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("task", task);

		request.getRequestDispatcher("PerformTestTutorFormView").forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Template template = TemplateFactory.getTemplate(request, response);

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			template.printTemplateHeader("Aufgabe nicht gefunden");
			PrintWriter out = response.getWriter();
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			template.printTemplateFooter();
			return;
		}

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
		if (request.getPart("file") == null) {
			template.printTemplateHeader("Invalid request");
			template.printTemplateFooter();
			return;
		}

		int testId = Util.parseInteger(request.getParameter("testid"), 0);

		File path = Util.createTemporaryDirectory("tutortest", null);
		if (path == null) {
			throw new IOException("Failed to create tempdir!");
		}
		if (path.exists() == false) {
			path.mkdirs();
		}

		// Process the uploaded item
		Part file = request.getPart("file");
				StringBuffer submittedFileName = new StringBuffer(Util.getUploadFileName(file));
				Util.lowerCaseExtension(submittedFileName);
				String fileName = null;
				for (Pattern pattern : SubmitSolution.getTaskFileNamePatterns(task, false)) {
					Matcher m = pattern.matcher(submittedFileName);
					if (!m.matches()) {
						template.printTemplateHeader("Ungültige Anfrage");
						PrintWriter out = response.getWriter();
						out.println("Dateiname ungültig bzw. entspricht nicht der Vorgabe (ist ein Klassenname vorgegeben, so muss die Datei genauso heißen).<br>Tipp: Nur A-Z, a-z, 0-9, ., - und _ sind erlaubt. Evtl. muss der Dateiname mit einem Großbuchstaben beginnen und darf keine Leerzeichen enthalten.");
						out.println("<br>Für Experten: Der Dateiname muss dem folgenden regulären Ausdruck genügen: " + Util.escapeHTML(pattern.pattern()));
						template.printTemplateFooter();
						return;
					}
					fileName = m.group(1);
				}
				try {
					SubmitSolution.handleUploadedFile(path, task, fileName, file);
				} catch (IOException e) {
					System.err.println("SubmitSolutionProblem1");
					System.err.println(e.getMessage());
					e.printStackTrace();
					template.printTemplateHeader("Ungültige Anfrage");
					PrintWriter out = response.getWriter();
					out.println("Problem beim Entpacken des Archives.");
					template.printTemplateFooter();
					return;
				}

				Test test = DAOFactory.TestDAOIf(session).getTest(testId);
				if (test == null) {
					template.printTemplateHeader("Ungültige Anfrage");
					PrintWriter out = response.getWriter();
					out.println("Test nicht gefunden.");
					template.printTemplateFooter();
					return;
				}

				request.setAttribute("task", test.getTask());
				request.setAttribute("test", test);

				ContextAdapter contextAdapter = new ContextAdapter(getServletContext());

				TestTask testTask = new TestTask(test);
				TestExecutorTestResult testResult = new TestExecutorTestResult();
				testTask.performTaskInFolder(test, contextAdapter.getDataPath(), path, testResult);

				Util.recursiveDelete(path);

				request.setAttribute("testresult", testResult);
				request.getRequestDispatcher("PerformTestResultView").forward(request, response);
				return;
	}
}
