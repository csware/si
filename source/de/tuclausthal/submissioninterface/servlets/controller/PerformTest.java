/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.TestExecutor;
import de.tuclausthal.submissioninterface.testframework.tests.impl.TestLogicImpl;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for performing a test
 * @author Sven Strickroth
 */
public class PerformTest extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Test test = DAOFactory.TestDAOIf().getTest(Util.parseInteger(request.getParameter("testid"), 0));
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf();
		Submission submission = submissionDAO.getSubmission(Util.parseInteger(request.getParameter("sid"), 0));
		if (submission == null || test == null) {
			request.setAttribute("title", "Abgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		Task task = submission.getTask();

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(), task.getLecture());
		if (participation == null || test.getTimesRunnableByStudents() == 0) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.getDeadline().before(Util.correctTimezone(new Date())) || participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
			request.setAttribute("title", "Testen nicht mehr möglich");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		SessionAdapter sa = new SessionAdapter(request);

		request.setAttribute("task", task);
		request.setAttribute("test", test);

		if (sa.getQueuedTest() == null) {
			sa.setQueuedTest(TestExecutor.executeTask(new TestLogicImpl(test, submission)));
			gotoWaitingView(request, response);
		} else {
			if (sa.getQueuedTest().isDone()) {
				try {
					request.setAttribute("testresult", sa.getQueuedTest().get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}

				sa.setQueuedTest(null);

				request.getRequestDispatcher("PerformTestResultView").forward(request, response);
			} else {
				gotoWaitingView(request, response);
			}
		}
	}

	private void gotoWaitingView(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		request.setAttribute("refreshurl", "");
		request.setAttribute("redirectTime", 5);
		request.getRequestDispatcher("PerformTestRunningView").forward(request, response);
	}
}
