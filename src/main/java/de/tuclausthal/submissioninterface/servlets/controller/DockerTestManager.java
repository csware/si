/*
 * Copyright 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTestStep;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.DockerTestManagerOverView;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for managing (add, edit, remove) Docker test steps by advisors
 * @author Sven Strickroth
 */
@GATEController
public class DockerTestManager extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TestDAOIf testDAOIf = DAOFactory.TestDAOIf(session);
		Test tst = testDAOIf.getTest(Util.parseInteger(request.getParameter("testid"), 0));
		if (tst == null || !(tst instanceof DockerTest)) {
			request.setAttribute("title", "Test nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}
		DockerTest test = (DockerTest) tst;

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), test.getTask().getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("test", test);
		getServletContext().getNamedDispatcher(DockerTestManagerOverView.class.getSimpleName()).forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TestDAOIf testDAOIf = DAOFactory.TestDAOIf(session);
		Test tst = testDAOIf.getTest(Util.parseInteger(request.getParameter("testid"), 0));
		if (tst == null || !(tst instanceof DockerTest)) {
			request.setAttribute("title", "Test nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}
		DockerTest test = (DockerTest) tst;

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), test.getTask().getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ("edittest".equals(request.getParameter("action"))) {
			Transaction tx = session.beginTransaction();
			test.setTestTitle(request.getParameter("title"));
			test.setPreparationShellCode(request.getParameter("preparationcode").replaceAll("\r\n", "\n"));
			session.update(test);
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(DockerTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
			return;
		} else if ("addNewStep".equals(request.getParameter("action"))) {
			String title = request.getParameter("title");
			String testCode = request.getParameter("testcode").replaceAll("\r\n", "\n");
			String expect = request.getParameter("expect").replaceAll("\r\n", "\n");
			DockerTestStep newStep = new DockerTestStep(test, title, testCode, expect);
			Transaction tx = session.beginTransaction();
			session.save(newStep);
			test.getTestSteps().add(newStep);
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(DockerTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
			return;
		} else if ("updateStep".equals(request.getParameter("action"))) {
			DockerTestStep step = null;
			for (DockerTestStep istep : test.getTestSteps()) {
				if (istep.getTeststepid() == Util.parseInteger(request.getParameter("teststepid"), -1)) {
					step = istep;
					break;
				}
			}
			if (step != null) {
				Transaction tx = session.beginTransaction();
				String title = request.getParameter("title");
				String testCode = request.getParameter("testcode").replaceAll("\r\n", "\n");
				step.setTitle(title);
				step.setTestcode(testCode);
				step.setExpect(Objects.toString(request.getParameter("expect"), "").replaceAll("\r\n", "\n"));
				session.saveOrUpdate(step);
				tx.commit();
			}
			response.sendRedirect(Util.generateRedirectURL(DockerTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
			return;
		} else if ("deleteStep".equals(request.getParameter("action"))) {
			DockerTestStep step = null;
			for (DockerTestStep istep : test.getTestSteps()) {
				if (istep.getTeststepid() == Util.parseInteger(request.getParameter("teststepid"), -1)) {
					step = istep;
					break;
				}
			}
			if (step != null) {
				Transaction tx = session.beginTransaction();
				test.getTestSteps().remove(step);
				session.delete(step);
				tx.commit();
			}
			response.sendRedirect(Util.generateRedirectURL(DockerTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
			return;
		}

		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "invalid request");
	}
}
