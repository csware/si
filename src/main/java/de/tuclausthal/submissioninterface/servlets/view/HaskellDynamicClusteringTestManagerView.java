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

package de.tuclausthal.submissioninterface.servlets.view;


import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;

import static de.tuclausthal.submissioninterface.servlets.view.TestManagerAddTestFormView.printHaskellDynamicClusteringTestForm;

/**
 * View-Servlet for clustering haskell submissions based on common errors (dynamic/runtime analysis)
 *
 * @author Christian Wagner
 */
@GATEView
public class HaskellDynamicClusteringTestManagerView extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Template template = TemplateFactory.getTemplate(request, response);

        template.addKeepAlive();
        template.printTemplateHeader("Haskell dynamisches Error Clustering bearbeiten");

        PrintWriter out = response.getWriter();
        out.println("...");
        // printHaskellDynamicClusteringTestForm(); // TODO@CHW needs task as parameter, look at Docker test implementation

        // TODO@CHW implement HTML to setup the haskell dynamic clustering in detail

        template.printTemplateFooter();
    }
}
