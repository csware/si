/*
 * Copyright 2021 Sven Strickroth <email@cs-ware.de>
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.ChecklistTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.ChecklistTestCheckItem;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.ChecklistTestManagerOverView;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for managing checklist checklists by advisors
 * @author Sven Strickroth
 */
@GATEController
public class ChecklistTestManager extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TestDAOIf testDAOIf = DAOFactory.TestDAOIf(session);
		Test tst = testDAOIf.getTest(Util.parseInteger(request.getParameter("testid"), 0));
		if (tst == null || !(tst instanceof ChecklistTest)) {
			request.setAttribute("title", "Test nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}
		ChecklistTest test = (ChecklistTest) tst;

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), test.getTask().getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("test", test);
		getServletContext().getNamedDispatcher(ChecklistTestManagerOverView.class.getSimpleName()).forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TestDAOIf testDAOIf = DAOFactory.TestDAOIf(session);
		Test tst = testDAOIf.getTest(Util.parseInteger(request.getParameter("testid"), 0));
		if (tst == null || !(tst instanceof ChecklistTest)) {
			request.setAttribute("title", "Test nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}
		ChecklistTest test = (ChecklistTest) tst;

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), test.getTask().getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ("edittest".equals(request.getParameter("action"))) {
			Transaction tx = session.beginTransaction();
			test.setTestTitle(request.getParameter("title"));
			session.update(test);
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(ChecklistTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
			return;
		} else if ("addNewCheckItem".equals(request.getParameter("action"))) {
			ChecklistTestCheckItem newCheckItem = new ChecklistTestCheckItem();
			newCheckItem.setTest(test);
			newCheckItem.setTitle(request.getParameter("title"));
			Transaction tx = session.beginTransaction();
			session.save(newCheckItem);
			test.getCheckItems().add(newCheckItem);
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(ChecklistTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
			return;
		} else if ("updateCheckItem".equals(request.getParameter("action"))) {
			ChecklistTestCheckItem checkItem = null;
			for (ChecklistTestCheckItem istep : test.getCheckItems()) {
				if (istep.getCheckitemid() == Util.parseInteger(request.getParameter("checkitemid"), -1)) {
					checkItem = istep;
					break;
				}
			}
			if (checkItem != null) {
				Transaction tx = session.beginTransaction();
				checkItem.setTitle(request.getParameter("title"));
				session.saveOrUpdate(checkItem);
				tx.commit();
			}
			response.sendRedirect(Util.generateRedirectURL(ChecklistTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
			return;
		} else if ("deleteCheckItem".equals(request.getParameter("action"))) {
			ChecklistTestCheckItem checkItem = null;
			for (ChecklistTestCheckItem istep : test.getCheckItems()) {
				if (istep.getCheckitemid() == Util.parseInteger(request.getParameter("checkitemid"), -1)) {
					checkItem = istep;
					break;
				}
			}
			if (checkItem != null) {
				Transaction tx = session.beginTransaction();
				test.getCheckItems().remove(checkItem);
				session.delete(checkItem);
				tx.commit();
			}
			response.sendRedirect(Util.generateRedirectURL(ChecklistTestManager.class.getSimpleName() + "?testid=" + test.getId(), response));
			return;
		}

		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "invalid request");
	}
}
