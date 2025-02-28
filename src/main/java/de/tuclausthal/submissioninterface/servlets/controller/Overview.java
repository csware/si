/*
 * Copyright 2020-2021, 2025 Sven Strickroth <email@cs-ware.de>
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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.authfilter.authentication.login.impl.Shibboleth;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.view.OverviewView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller servlet for displaying the startpage of the system
 * @author Sven Strickroth
 */
@GATEController
public class Overview extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// redirect handler for Shibboleth, not yet perfect but works
		if (request.getParameter(Shibboleth.REDIR_PARAMETER) != null && request.getParameter(Shibboleth.REDIR_PARAMETER).startsWith(Util.generateAbsoluteServletsRedirectURL("", request, response))) {
			response.sendRedirect(Util.generateRedirectURL(request.getParameter(Shibboleth.REDIR_PARAMETER).replace("\r", "%0d").replace("\n", "%0a"), response));
			return;
		}

		getServletContext().getNamedDispatcher(OverviewView.class.getSimpleName()).forward(request, response);
	}
}
