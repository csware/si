/*
 * Copyright 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2025 Christian Wagner <christian.wagner@campus.lmu.de>
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

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.HaskellRuntimeTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTestStep;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.HaskellRuntimeTestManagerView;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Util;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.Serial;
import java.util.Objects;

/**
 * Controller-Servlet for clustering haskell submissions based on common errors (dynamic/runtime analysis).
 * This servlet allows advisors to manage (add, edit, remove) test steps.
 *
 * @author Christian Wagner
 */
@GATEController
public class HaskellRuntimeTestManager extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // similar code in DockerTestManager
        Session session = RequestAdapter.getSession(request);

        TestDAOIf testDAOIf = DAOFactory.TestDAOIf(session);
        Test test = testDAOIf.getTest(Util.parseInteger(request.getParameter("testid"), 0));
        if (!(test instanceof HaskellRuntimeTest haskellRuntimeTest)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("title", "Test nicht gefunden");
            getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
            return;
        }

        ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
        Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), haskellRuntimeTest.getTask().getTaskGroup().getLecture());
        if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
            return;
        }

        request.setAttribute("test", haskellRuntimeTest);
        getServletContext().getNamedDispatcher(HaskellRuntimeTestManagerView.class.getSimpleName()).forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // similar code in DockerTestManager
        Session session = RequestAdapter.getSession(request);
        TestDAOIf testDAOIf = DAOFactory.TestDAOIf(session);
        Test test = testDAOIf.getTest(Util.parseInteger(request.getParameter("testid"), 0));
        if (!(test instanceof HaskellRuntimeTest haskellRuntimeTest)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("title", "Test nicht gefunden");
            getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
            return;
        }

        ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
        Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), haskellRuntimeTest.getTask().getTaskGroup().getLecture());
        if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
            return;
        }

        if ("edittest".equals(request.getParameter("action"))) {
            Transaction tx = session.beginTransaction();
            haskellRuntimeTest.setTestTitle(request.getParameter("title"));
            haskellRuntimeTest.setForTutors(request.getParameter("tutortest") != null);
            haskellRuntimeTest.setTimesRunnableByStudents(Util.parseInteger(request.getParameter("timesRunnableByStudents"), 0));
            haskellRuntimeTest.setGiveDetailsToStudents(request.getParameter("giveDetailsToStudents") != null);
            haskellRuntimeTest.setPreparationShellCode(request.getParameter("preparationcode").replaceAll("\r\n", "\n")); // TODO@CHW: can getParameter("preparationcode") be null?
            tx.commit();
            response.sendRedirect(Util.generateRedirectURL(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + haskellRuntimeTest.getId(), response));
            return;
        } else if ("addNewStep".equals(request.getParameter("action"))) {
            String title = request.getParameter("title");
            String testCode = request.getParameter("testcode").replaceAll("\r\n", "\n");
            String expect = request.getParameter("expect").replaceAll("\r\n", "\n");
            DockerTestStep newStep = new DockerTestStep(haskellRuntimeTest, title, testCode, expect);
            Transaction tx = session.beginTransaction();
            session.persist(newStep);
            tx.commit();
            response.sendRedirect(Util.generateRedirectURL(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + haskellRuntimeTest.getId(), response));
            return;
        } else if ("updateStep".equals(request.getParameter("action"))) {
            DockerTestStep step = null;
            for (DockerTestStep istep : haskellRuntimeTest.getTestSteps()) {
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
                tx.commit();
            }
            response.sendRedirect(Util.generateRedirectURL(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + haskellRuntimeTest.getId(), response));
            return;
        } else if ("deleteStep".equals(request.getParameter("action"))) {
            DockerTestStep step = null;
            for (DockerTestStep istep : haskellRuntimeTest.getTestSteps()) {
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
            response.sendRedirect(Util.generateRedirectURL(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + haskellRuntimeTest.getId(), response));
            return;
        }

        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "invalid request");
    }
}
