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

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestCountDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.impl.LogDAO;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry.LogAction;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
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
public class PerformTest extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Test test = DAOFactory.TestDAOIf(session).getTest(Util.parseInteger(request.getParameter("testid"), 0));
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(Util.parseInteger(request.getParameter("sid"), 0));
		if (submission == null || test == null) {
			request.setAttribute("title", "Abgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		Task task = submission.getTask();

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || test.getTimesRunnableByStudents() == 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.getDeadline().before(Util.correctTimezone(new Date())) || participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
			request.setAttribute("title", "Testen nicht mehr möglich");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		SessionAdapter sa = RequestAdapter.getSessionAdapter(request);

		TestCountDAOIf testCountDAO = DAOFactory.TestCountDAOIf(session);

		request.setAttribute("task", task);
		request.setAttribute("test", test);

		if (test instanceof UMLConstraintTest) {
			if (testCountDAO.canStillRunXTimes(test, submission) == 0) {
				request.setAttribute("title", "Dieser Test kann nicht mehr ausgeführt werden. Limit erreicht.");
				request.getRequestDispatcher("MessageView").forward(request, response);
				return;
			}
			sa.setQueuedTest(TestExecutor.executeTask(new TestTask(test, submission)));
			while (!sa.getQueuedTest().isDone()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			TestExecutorTestResult result = null;
			try {
				result = sa.getQueuedTest().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

			if (!testCountDAO.canSeeResultAndIncrementCounter(test, submission)) {
				sa.setQueuedTest(null);
				request.setAttribute("title", "Dieser Test kann nicht mehr ausgeführt werden. Limit erreicht.");
				request.getRequestDispatcher("MessageView").forward(request, response);
				return;
			}

			sa.setQueuedTest(null);

			new LogDAO(session).createLogEntry(participation.getUser(), test, test.getTask(), LogAction.PERFORMED_TEST, result.isTestPassed(), result.getTestOutput());
			request.setAttribute("testresult", result);
			request.getRequestDispatcher("PerformTestArgoUMLView").forward(request, response);
			return;
		}

		if (sa.getQueuedTest() == null) {
			if (request.getParameter("refresh") == null) {
				// prevent user from redo a test by mistake

				if (testCountDAO.canStillRunXTimes(test, submission) == 0) {
					request.setAttribute("title", "Dieser Test kann nicht mehr ausgeführt werden. Limit erreicht.");
					request.getRequestDispatcher("MessageView").forward(request, response);
					return;
				}

				sa.setQueuedTest(TestExecutor.executeTask(new TestTask(test, submission)));
				gotoWaitingView(request, response, "sid=" + submission.getSubmissionid() + "&testid=" + test.getId());
			} else {
				request.setAttribute("title", "Ungültige Anfrage");
				request.getRequestDispatcher("MessageView").forward(request, response);
			}
		} else {
			if (sa.getQueuedTest().isDone()) {
				TestExecutorTestResult result = null;
				try {
					result = sa.getQueuedTest().get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}

				if (!testCountDAO.canSeeResultAndIncrementCounter(test, submission)) {
					sa.setQueuedTest(null);
					request.setAttribute("title", "Dieser Test kann nicht mehr ausgeführt werden. Limit erreicht.");
					request.getRequestDispatcher("MessageView").forward(request, response);
					return;
				}

				sa.setQueuedTest(null);

				new LogDAO(session).createLogEntry(participation.getUser(), test, test.getTask(), LogAction.PERFORMED_TEST, result.isTestPassed(), result.getTestOutput());
				request.setAttribute("testresult", result);

				request.getRequestDispatcher("PerformTestResultView").forward(request, response);
			} else {
				gotoWaitingView(request, response, "sid=" + submission.getSubmissionid() + "&testid=" + test.getId());
			}
		}
	}

	private void gotoWaitingView(HttpServletRequest request, HttpServletResponse response, String url) throws IOException, ServletException {
		request.setAttribute("refreshurl", response.encodeURL(request.getRequestURL() + "?refresh=true&" + url));
		request.setAttribute("redirectTime", 5);
		request.getRequestDispatcher("PerformTestRunningView").forward(request, response);
	}
}
