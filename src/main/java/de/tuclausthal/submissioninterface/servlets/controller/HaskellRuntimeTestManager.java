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
import de.tuclausthal.submissioninterface.persistence.datamodel.*;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
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

/**
 * Controller-Servlet for clustering haskell submissions based on common errors (dynamic/runtime analysis).
 * This servlet allows advisors to automatically generate and modify test steps.
 *
 * @author Christian Wagner
 */
@GATEController
public class HaskellRuntimeTestManager extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        getServletContext().getNamedDispatcher(DockerTestManager.class.getSimpleName()).forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
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

        if ("generateNewTestSteps".equals(request.getParameter("action"))) {
            int numberOfTestSteps = Util.parseInteger(request.getParameter("numberOfTestSteps"), 0);
            String[][] testcases = new String[numberOfTestSteps][3];

            // TODO@CHW: this is just a placeholder for the actual testcase generator
            for (int testStepId = 0; testStepId < numberOfTestSteps; testStepId++) {
                testcases[testStepId][0] = "Testcase " + testStepId;
                testcases[testStepId][1] = "ghci -e " + testStepId + "+" + testStepId;
                testcases[testStepId][2] = "" + (testStepId + testStepId);
            }

            Transaction tx = session.beginTransaction();
            for (int testStepId = 0; testStepId < numberOfTestSteps; testStepId++) {
                String title = testcases[testStepId][0];
                String testCode = testcases[testStepId][1].replaceAll("\r\n", "\n");
                String expect = testcases[testStepId][2].replaceAll("\r\n", "\n");

                DockerTestStep newStep = new DockerTestStep(haskellRuntimeTest, title, testCode, expect);
                session.persist(newStep);
            }
            tx.commit();

            response.sendRedirect(Util.generateRedirectURL(HaskellRuntimeTestManager.class.getSimpleName() + "?testid=" + haskellRuntimeTest.getId(), response));
        } else {
            getServletContext().getNamedDispatcher(DockerTestManager.class.getSimpleName()).forward(request, response);
        }
    }
}
