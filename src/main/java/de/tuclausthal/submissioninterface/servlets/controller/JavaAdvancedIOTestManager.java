/*
 * Copyright 2020-2025 Sven Strickroth <email@cs-ware.de>
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
import java.util.Objects;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTestStep;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.JavaAdvancedIOTestManagerOverView;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for managing (add, edit, remove) advanced Java IO tests steps by advisors
 * @author Sven Strickroth
 */
@MultipartConfig(maxFileSize = Configuration.MAX_UPLOAD_SIZE)
@GATEController
public class JavaAdvancedIOTestManager extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TestDAOIf testDAOIf = DAOFactory.TestDAOIf(session);
		Test tst = testDAOIf.getTest(Util.parseInteger(request.getParameter("testid"), 0));
		if (tst == null || !(tst instanceof JavaAdvancedIOTest test)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Test nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), test.getTask().getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("test", test);
		getServletContext().getNamedDispatcher(JavaAdvancedIOTestManagerOverView.class.getSimpleName()).forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TestDAOIf testDAOIf = DAOFactory.TestDAOIf(session);
		Test tst = testDAOIf.getTest(Util.parseInteger(request.getParameter("testid"), 0));
		if (tst == null || !(tst instanceof JavaAdvancedIOTest test)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Test nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), test.getTask().getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ("edittest".equals(request.getParameter("action"))) {
			Transaction tx = session.beginTransaction();
			test.setTestTitle(request.getParameter("title"));
			test.setForTutors(request.getParameter("tutortest") != null);
			test.setTimesRunnableByStudents(Util.parseInteger(request.getParameter("timesRunnableByStudents"), 0));
			test.setGiveDetailsToStudents(request.getParameter("giveDetailsToStudents") != null);
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(JavaAdvancedIOTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
			return;
		} else if ("addNewStep".equals(request.getParameter("action"))) {
			String title = request.getParameter("title");
			String testCode = request.getParameter("testcode");
			String expect = request.getParameter("expect").replaceAll("\r\n", "\n");
			JavaAdvancedIOTestStep newStep = new JavaAdvancedIOTestStep(test, title, testCode, expect);
			Transaction tx = session.beginTransaction();
			session.persist(newStep);
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(JavaAdvancedIOTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
			return;
		} else if ("updateStep".equals(request.getParameter("action"))) {
			JavaAdvancedIOTestStep step = null;
			for (JavaAdvancedIOTestStep istep : test.getTestSteps()) {
				if (istep.getTeststepid() == Util.parseInteger(request.getParameter("teststepid"), -1)) {
					step = istep;
					break;
				}
			}
			if (step != null) {
				Transaction tx = session.beginTransaction();
				String title = request.getParameter("title");
				String testCode = request.getParameter("testcode");
				step.setTitle(title);
				step.setTestcode(testCode);
				step.setExpect(Objects.toString(request.getParameter("expect"), "").replaceAll("\r\n", "\n"));
				tx.commit();
			}
			response.sendRedirect(Util.generateRedirectURL(JavaAdvancedIOTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
			return;
		} else if ("deleteStep".equals(request.getParameter("action"))) {
			JavaAdvancedIOTestStep step = null;
			for (JavaAdvancedIOTestStep istep : test.getTestSteps()) {
				if (istep.getTeststepid() == Util.parseInteger(request.getParameter("teststepid"), -1)) {
					step = istep;
					break;
				}
			}
			if (step != null) {
				Transaction tx = session.beginTransaction();
				session.remove(step);
				tx.commit();
			}
			response.sendRedirect(Util.generateRedirectURL(JavaAdvancedIOTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
			return;
		}

		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "invalid request");
	}
}
