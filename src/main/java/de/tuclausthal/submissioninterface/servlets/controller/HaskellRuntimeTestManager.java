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

import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.HaskellRuntimeTestManagerView;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;

import java.io.IOException;
import java.io.Serial;

/**
 * Controller-Servlet for clustering haskell submissions based on common errors (dynamic/runtime analysis)
 *
 * @author Christian Wagner
 */
@GATEController
public class HaskellRuntimeTestManager extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Session session = RequestAdapter.getSession(request);

        // TODO@CHW missing implementation

        request.setAttribute("testattribute", "somevalue");
        getServletContext().getNamedDispatcher(HaskellRuntimeTestManagerView.class.getSimpleName()).forward(request, response);

    }
}
