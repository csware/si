/*
 * Copyright 2009-2011, 2020-2025 Sven Strickroth <email@cs-ware.de>
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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.ModelSolutionProvisionType;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.servlets.view.ShowFileView;
import de.tuclausthal.submissioninterface.servlets.view.ShowTaskDescriptionView;
import de.tuclausthal.submissioninterface.servlets.view.ShowTaskStudentCommonErrorOverView;
import de.tuclausthal.submissioninterface.servlets.view.ShowTaskStudentView;
import de.tuclausthal.submissioninterface.servlets.view.ShowTaskTutorCSVView;
import de.tuclausthal.submissioninterface.servlets.view.ShowTaskTutorPrintView;
import de.tuclausthal.submissioninterface.servlets.view.ShowTaskTutorTestOverView;
import de.tuclausthal.submissioninterface.servlets.view.ShowTaskTutorView;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.testanalyzer.CommonErrorAnalyzer;
import de.tuclausthal.submissioninterface.testframework.executor.impl.LocalExecutor;
import de.tuclausthal.submissioninterface.testframework.executor.impl.TestExecutorWorker;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.TaskPath;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for displaying a task
 * loads a task and differs between the student and tutor view 
 * @author Sven Strickroth
 */
@GATEController
public class ShowTask extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null) {
			if (task.getTaskGroup().getLecture().getSemester() == Util.getCurrentSemester()) {
				request.setAttribute("title", "Zugriff verweigert (403)");
				request.setAttribute("message", "<p>Sie versuchen auf eine Aufgabe einer Vorlesung zuzugreifen, für die Sie nicht angemeldet sind.</p><p>Sie können Sie <a href=\"" + Util.generateHTMLLink(SubscribeToLecture.class.getSimpleName(), response) + "\" target=\"_blank\">hier</a> für die Vorlesung anmelden.</p>");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.getStart().isAfter(ZonedDateTime.now()) && participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		request.setAttribute("participation", participation);

		final Path taskPath = Util.constructPath(Configuration.getInstance().getDataPath(), task);
		request.setAttribute("advisorFiles", Util.listFilesAsRelativeStringListSorted(taskPath.resolve(TaskPath.ADVISORFILES.getPathComponent())));
		request.setAttribute("task", task);
		if (request.getParameter("onlydescription") != null) {
			SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
			Submission submission = submissionDAO.getSubmission(task, RequestAdapter.getUser(request));
			if (submission != null) {
				response.addHeader("SID", String.valueOf(submission.getSubmissionid()));
			}
			getServletContext().getNamedDispatcher(ShowTaskDescriptionView.class.getSimpleName()).forward(request, response);
		} else if (participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
			if ("dotests".equals(request.getParameter("action"))) {
				if (!task.getTaskGroup().getLecture().getName().contains("Live-Coding")) {
					return;
				}
				final Template template = TemplateFactory.getTemplate(request, response);

				template.printTemplateHeader("Live Testen...", task);
				final PrintWriter out = response.getWriter();
				Transaction tx = session.beginTransaction();
				if (task.getDeadline().isAfter(ZonedDateTime.now())) {
					task.setDeadline(ZonedDateTime.now());
				}
				for (final Test test : task.getTests()) {
					if (test.isNeedsToRun()) {
						test.setNeedsToRun(false);
					}
				}
				tx.commit();
				LocalExecutor.CORES = (Runtime.getRuntime().availableProcessors() > 4) ? Runtime.getRuntime().availableProcessors() - 2 : 2;
				final ExecutorService executorService = Executors.newFixedThreadPool(LocalExecutor.CORES);
				LocalExecutor.dataPath = Configuration.getInstance().getDataPath();

				for (final Test test : task.getTests()) {
					for (final Submission submission : test.getTask().getSubmissions()) {
						executorService.submit(new TestExecutorWorker(LocalExecutor.dataPath, new TestTask(test, submission, true)));
					}
				}
				out.println("Alle Tests angefordert...<br>");
				out.flush();
				executorService.shutdown();
				try {
					while (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
						out.println("Waiting for all checks to finish: " + executorService.toString()+"<br>");
						out.flush();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}

				out.println("Testen beendet.<br>");
				out.println("Starte Analyse...<br>");
				out.flush();
				tx = session.beginTransaction();
				DAOFactory.CommonErrorDAOIf(session).reset(task);
				final CommonErrorAnalyzer analyzer = new CommonErrorAnalyzer(session);
				for (final Submission sub : task.getSubmissions()) {
					for (final TestResult testResult : sub.getTestResults()) {
						analyzer.runAnalysis(testResult);
					}
				}
				tx.commit();
				out.println("Analyse beendet.<br>");
				out.println("<a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid()+ "&show=testoverview", response) + "\">" + Util.escapeHTML(task.getTitle()) + "</a>");
				template.printTemplateFooter();
				return;
			}
			if ("testoverview".equals(request.getParameter("show"))) {
				final Group group = DAOFactory.GroupDAOIf(session).getGroup(Util.parseInteger(request.getParameter("groupid"), 0));
				if (group != null && group.getLecture().getId() == participation.getLecture().getId()) {
					request.setAttribute("group", group);
				}
				getServletContext().getNamedDispatcher(ShowTaskTutorTestOverView.class.getSimpleName()).forward(request, response);
				return;
			}
			if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) >= 0 && "markingcsv".equals(request.getParameter("show"))) {
				getServletContext().getNamedDispatcher(ShowTaskTutorCSVView.class.getSimpleName()).forward(request, response);
				return;
			}
			if ("grouplist".equals(request.getParameter("action"))) {
				Group group = DAOFactory.GroupDAOIf(session).getGroup(Util.parseInteger(request.getParameter("groupid"), 0));
				if (group != null && group.getLecture().getId() == participation.getLecture().getId()) {
					request.setAttribute("group", group);
				}
				getServletContext().getNamedDispatcher(ShowTaskTutorPrintView.class.getSimpleName()).forward(request, response);
			} else {
				request.setAttribute("modelSolutionFiles", Util.listFilesAsRelativeStringListSorted(taskPath.resolve(TaskPath.MODELSOLUTIONFILES.getPathComponent())));
				getServletContext().getNamedDispatcher(ShowTaskTutorView.class.getSimpleName()).forward(request, response);
			}
		} else {
			SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
			Submission submission = submissionDAO.getSubmission(task, RequestAdapter.getUser(request));
			if (submission != null) {
				final Path path = taskPath.resolve(String.valueOf(submission.getSubmissionid()));
				request.setAttribute("submittedFiles", Util.listFilesAsRelativeStringListSorted(path));
				if (task.isADynamicTask()) {
					task.setDescription(task.getDynamicTaskStrategie(session).getTranslatedDescription(submission));
				}
			} else {
				if (task.isADynamicTask()) {
					task.setDescription(task.getDynamicTaskStrategie(session).getTranslatedDescription(participation));
				}
			}
			if (ModelSolutionProvisionType.canStudentAccessModelSolution(task, submission, null, null)) {
				request.setAttribute("modelSolutionFiles", Util.listFilesAsRelativeStringListSorted(taskPath.resolve(String.valueOf(TaskPath.MODELSOLUTIONFILES.getPathComponent()))));
			}

			request.setAttribute("submission", submission);
			if ("commonerroroverview".equals(request.getParameter("show"))) {
				getServletContext().getNamedDispatcher(ShowTaskStudentCommonErrorOverView.class.getSimpleName()).forward(request, response);
			} else if ("file".equals(request.getParameter("show"))) {
				getServletContext().getNamedDispatcher(ShowFileView.class.getSimpleName()).forward(request, response);
			} else {
				getServletContext().getNamedDispatcher(ShowTaskStudentView.class.getSimpleName()).forward(request, response);
			}
		}
	}
}
