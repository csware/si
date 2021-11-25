/*
 * Copyright 2009-2010, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.authfilter.authentication.login.impl.Shibboleth;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for clearing up the session
 * @author Sven Strickroth
 *
 */
@GATEController
public class Logout extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		boolean wasShibbolethLogin = request.getSession().getAttribute(Shibboleth.SHIBBOLETH_LOGIN_KEY) != null;
		RequestAdapter.getSessionAdapter(request).setUser(null, null);
		Cookie privacyCookie = new Cookie("privacy", "0");
		privacyCookie.setMaxAge(0);
		response.addCookie(privacyCookie);

		String cookieName = null;
		if (getServletContext().getSessionCookieConfig() != null) {
			cookieName = getServletContext().getSessionCookieConfig().getName();
		}
		if (cookieName == null) {
			cookieName = "JSESSIONID";
		}
		Cookie jsessionid = new Cookie(cookieName, "");
		jsessionid.setMaxAge(0);
		if (getServletContext().getSessionCookieConfig() != null && getServletContext().getSessionCookieConfig().getPath() != null) {
			jsessionid.setPath(getServletContext().getSessionCookieConfig().getPath());
		} else {
			jsessionid.setPath(getServletContext().getContextPath());
		}
		response.addCookie(jsessionid);

		Template template = TemplateFactory.getTemplate(request, response);
		template.printTemplateHeader("Sind wurden von GATE abgemeldet.");
		PrintWriter out = response.getWriter();
		if (wasShibbolethLogin) {
			out.println("<p class=mid><a href=\"/Shibboleth.sso/Logout\">Von allen Diensten per Single Logout abmelden.</a></p>");
		}
		out.println("<p class=mid><a href=\"" + Util.generateHTMLLink(Overview.class.getSimpleName(), response) + "\">zurück zur Übersicht</a></p>");
		template.printTemplateFooter();
	}
}
