/*
 * Copyright 2022 Sven Strickroth <email@cs-ware.de>
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
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for en/disabling privacy mode
 * @author Sven Strickroth
 */
@GATEController
public class Privacy extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		RequestAdapter requestAdapter = new RequestAdapter(request);
		Template template = TemplateFactory.getTemplate(request, response);
		if (requestAdapter.isPrivacyMode()) {
			Cookie privacyCookie = new Cookie("privacy", "0");
			privacyCookie.setMaxAge(0);
			response.addCookie(privacyCookie);
			template.printTemplateHeader("Privacy-Mode deaktiviert");
		} else {
			response.addCookie(new Cookie("privacy", "1"));
			template.printTemplateHeader("Privacy-Mode aktiviert");
		}
		PrintWriter out = response.getWriter();
		out.println("<div class=mid><a href=\"" + Util.generateAbsoluteServletsHTMLLink(Overview.class.getSimpleName(), request, response) + "\">zur Übersicht</a></div>");
		template.printTemplateFooter();
	}
}
