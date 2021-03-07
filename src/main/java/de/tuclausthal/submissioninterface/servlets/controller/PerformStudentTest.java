/*
 * Copyright 2009-2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.persistence.LockModeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.authfilter.SessionAdapter.QueuedTest;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestCountDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.impl.LogDAO;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.UMLConstraintTest;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.testframework.TestExecutor;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for performing a test
 * @author Sven Strickroth
 */
public class PerformStudentTest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final static private Logger LOG = LoggerFactory.getLogger(PerformStudentTest.class);

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		SessionAdapter sa = RequestAdapter.getSessionAdapter(request);

		Test test = DAOFactory.TestDAOIf(session).getTest(Util.parseInteger(request.getParameter("testid"), 0));
		if (test == null) {
			QueuedTest resultFuture = sa.getQueuedTest();
			if (resultFuture != null && resultFuture.testId == Util.parseInteger(request.getParameter("testid"), 0)) {
				sa.setQueuedTest(null);
			}
			request.setAttribute("title", "Test nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		Task task = test.getTask();

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || test.getTimesRunnableByStudents() == 0) {
			QueuedTest resultFuture = sa.getQueuedTest();
			if (resultFuture != null && resultFuture.testId == test.getId()) {
				sa.setQueuedTest(null);
			}
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(task, RequestAdapter.getUser(request));

		if ((task.getDeadline().before(Util.correctTimezone(new Date())) || submission == null || (task.isAllowPrematureSubmissionClosing() && submission.isClosed()))) {
			request.setAttribute("title", "Testen bzw. Abruf des Ergebnisses nicht mehr möglich");
			request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateHTMLLink("ShowTask?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			QueuedTest resultFuture = sa.getQueuedTest();
			if (resultFuture != null && resultFuture.testId == test.getId()) {
				sa.setQueuedTest(null);
			}
			return;
		}

		TestCountDAOIf testCountDAO = DAOFactory.TestCountDAOIf(session);

		request.setAttribute("task", task);
		request.setAttribute("test", test);

		if (test instanceof UMLConstraintTest && request.getParameter("argouml") != null) {
			if (testCountDAO.canStillRunXTimes(test, submission) == 0) {
				request.setAttribute("title", "Dieser Test kann nicht mehr ausgeführt werden. Limit erreicht.");
				getServletContext().getNamedDispatcher("MessageArgoUMLView").forward(request, response);
				return;
			}
			Future<TestExecutorTestResult> resultFuture = TestExecutor.executeTask(new TestTask(test, submission));
			while (!resultFuture.isDone()) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					LOG.warn("Was interrupted while waiting for test to finish", e);
				}
			}
			TestExecutorTestResult result = null;
			try {
				result = resultFuture.get();
			} catch (InterruptedException e) {
				LOG.warn("Was interrupted while waiting for test to finish", e);
			} catch (ExecutionException e) {
				LOG.error("Got ExecutionException while accessing test result", e);
			}

			Transaction tx = session.beginTransaction();
			session.buildLockRequest(LockOptions.UPGRADE).lock(submission);
			if (!testCountDAO.canSeeResultAndIncrementCounterTransaction(test, submission)) {
				tx.commit();
				request.setAttribute("title", "Dieser Test kann nicht mehr ausgeführt werden. Limit erreicht.");
				getServletContext().getNamedDispatcher("MessageArgoUMLView").forward(request, response);
				return;
			}
			tx.commit();

			new LogDAO(session).createLogEntryForStudentTest(participation.getUser(), test, test.getTask(), result.isTestPassed(), result.getTestOutput());
			request.setAttribute("testresult", result);
			getServletContext().getNamedDispatcher("PerformStudentTestArgoUMLView").forward(request, response);
			return;
		}

		QueuedTest resultFuture = sa.getQueuedTest();
		if (resultFuture == null) {
			request.setAttribute("title", "Testergebnis nicht (mehr) verfügbar");
			request.setAttribute("message", "<div class=mid>Das Testergebnis konnte nicht abgerufen werden. Entweder wurde es bereits abgerufen oder es wurde eine neue Sitzung gestartet.<p><a href=\"" + Util.generateHTMLLink("ShowTask?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		if (test.getId() != resultFuture.testId) {
			LOG.warn("Mismatching testid in session " + resultFuture.testId + " and on request " + test.getId());
			request.setAttribute("title", "Es kann immer nur ein Test zu einer Zeit angefordert werden.");
			request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateHTMLLink("PerformStudentTest?testid=" + resultFuture.testId, response) + "\">weiter zum bereits angefragten Test</a></div>");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		if (resultFuture.testResult.isDone()) {
			TestExecutorTestResult result = null;
			try {
				result = resultFuture.testResult.get();
			} catch (InterruptedException e) {
				LOG.warn("Was interrupted while waiting for test to finish", e);
			} catch (ExecutionException e) {
				LOG.error("Got ExecutionException while accessing test result", e);
			}

			sa.setQueuedTest(null);

			Transaction tx = session.beginTransaction();
			session.refresh(submission, LockModeType.PESSIMISTIC_WRITE);
			if (!resultFuture.submissionLastChanged.equals(submission.getLastModified()) || submission.isClosed()) {
				tx.commit();
				request.setAttribute("title", "Das Testergebnis wurde durch eine zwischenzeitlich modifizierte Abgabe ungültig.");
				request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateHTMLLink("ShowTask?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
				getServletContext().getNamedDispatcher("MessageView").forward(request, response);
				return;
			}
			if (!testCountDAO.canSeeResultAndIncrementCounterTransaction(test, submission)) {
				tx.commit();
				request.setAttribute("title", "Dieser Test kann nicht mehr ausgeführt werden. Limit erreicht.");
				request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateHTMLLink("ShowTask?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
				getServletContext().getNamedDispatcher("MessageView").forward(request, response);
				return;
			}
			tx.commit();

			new LogDAO(session).createLogEntryForStudentTest(participation.getUser(), test, test.getTask(), result.isTestPassed(), result.getTestOutput());
			request.setAttribute("testresult", result);

			getServletContext().getNamedDispatcher("PerformStudentTestResultView").forward(request, response);
		} else {
			request.setAttribute("refreshurl", Util.generateRedirectURL("PerformStudentTest?testid=" + resultFuture.testId, response));
			request.setAttribute("redirectTime", 5);
			getServletContext().getNamedDispatcher("PerformStudentTestRunningView").forward(request, response);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Test test = DAOFactory.TestDAOIf(session).getTest(Util.parseInteger(request.getParameter("testid"), 0));
		if (test == null) {
			request.setAttribute("title", "Test nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		Task task = test.getTask();

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || test.getTimesRunnableByStudents() == 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(task, RequestAdapter.getUser(request));
		if (submission == null) {
			request.setAttribute("title", "Abgabe nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		if ((task.getDeadline().before(Util.correctTimezone(new Date())) || (task.isAllowPrematureSubmissionClosing() && submission.isClosed()))) {
			request.setAttribute("title", "Testen nicht mehr möglich");
			request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateHTMLLink("ShowTask?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		TestCountDAOIf testCountDAO = DAOFactory.TestCountDAOIf(session);

		request.setAttribute("task", task);
		request.setAttribute("test", test);

		if (testCountDAO.canStillRunXTimes(test, submission) == 0) {
			request.setAttribute("title", "Dieser Test kann nicht mehr ausgeführt werden. Limit erreicht.");
			request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateHTMLLink("ShowTask?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		SessionAdapter sa = RequestAdapter.getSessionAdapter(request);
		Transaction tx = session.beginTransaction();
		session.buildLockRequest(LockOptions.UPGRADE).lock(submission);
		QueuedTest resultFuture = sa.getQueuedTest();
		if (resultFuture != null) {
			tx.commit();
			request.setAttribute("title", "Es kann immer nur ein Test zu einer Zeit angefragt werden.");
			request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateHTMLLink("PerformStudentTest?testid=" + resultFuture.testId, response) + "\">weiter zum bereits angefragten Test</a></div>");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}
		sa.setQueuedTest(new QueuedTest(test.getId(), submission.getLastModified(), TestExecutor.executeTask(new TestTask(test, submission))));
		tx.commit();
		response.sendRedirect(Util.generateRedirectURL("PerformStudentTest?testid=" + test.getId(), response));
	}
}
