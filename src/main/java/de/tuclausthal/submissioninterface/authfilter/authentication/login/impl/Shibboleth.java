/*
 * Copyright 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.authfilter.authentication.login.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginData;
import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginIf;
import de.tuclausthal.submissioninterface.servlets.controller.Noop;
import de.tuclausthal.submissioninterface.servlets.controller.Overview;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Shibboleth-based login method implementation
 * @author Sven Strickroth
 */
public class Shibboleth implements LoginIf {
	static final private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final public String REDIR_PARAMETER = "redir";
	static final public String SHIBBOLETH_LOGIN_KEY = "SHIBBOLETH_LOGIN";
	static final private String LOOP_DETECTION_KEY = "SHIBBOLETH_LOOP_DETECTION";

	private String userAttribute;

	public Shibboleth(FilterConfig filterConfig) {
		this.userAttribute = filterConfig.getInitParameter("userAttribute");
	}

	@Override
	public void failNoData(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// don't do a redirect for the Noop servlet
		if (request.getRequestURI().equals(Util.generateAbsoluteServletsRedirectURL(Noop.class.getSimpleName(), request, response))) {
			response.setContentType("text/plain");
			response.setStatus(401);
			response.getWriter().println("not logged in");
			return;
		}

		String redirector = Util.generateAbsoluteServletsRedirectURL(Overview.class.getSimpleName(), request, response);
		boolean isRedirector = request.getRequestURI().equals(redirector);
		if (isRedirector && request.getSession().getAttribute(LOOP_DETECTION_KEY) != null) {
			LOG.error("Got no data from Shibboleth service provider; login loop detected.");
			request.getSession().removeAttribute(LOOP_DETECTION_KEY);
			response.setStatus(401);
			failNoData("Shibboleth-Login fehlgeschlagen. Keine Login-Daten vom Shibboleth-ServiceProvider erhalten.", request, response);
			return;
		}

		String redirString = "";
		if (!isRedirector) {
			String queryString = "";
			if (request.getQueryString() != null) {
				queryString = "?" + request.getQueryString();
			}
			redirString = "?" + REDIR_PARAMETER + "=" + URLEncoder.encode((request.getRequestURI() + queryString), StandardCharsets.UTF_8.toString());
		} else {
			request.getSession().setAttribute(LOOP_DETECTION_KEY, true);
			if (request.getParameter(REDIR_PARAMETER) != null) {
				redirString = "?" + REDIR_PARAMETER + "=" + URLEncoder.encode(request.getParameter(REDIR_PARAMETER), StandardCharsets.UTF_8.toString());
			}
		}
		response.sendRedirect(redirector + redirString);
	}

	@Override
	public void failNoData(String error, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.addHeader("Cache-Control", "no-cache, must-revalidate");
		Template template = TemplateFactory.getTemplate(request, response);
		template.printTemplateHeader("Login fehlgeschlagen", "Login fehlgeschlagen");
		PrintWriter out = response.getWriter();
		if (!error.isEmpty()) {
			out.println("<p class=\"red mid\">" + error + "</p>");
		}
		template.printTemplateFooter();
		out.close();
	}

	@Override
	public LoginData getLoginData(HttpServletRequest request) {
		if (request.getAttribute("Shib-Identity-Provider") == null) {
			return null;
		}
		String username = getAttribute(request, userAttribute);
		if (username == null) {
			return null;
		}
		request.getSession().setAttribute(SHIBBOLETH_LOGIN_KEY, true);
		return new LoginData(username, null);
	}

	@Override
	public boolean requiresVerification() {
		return true;
	}

	@Override
	public boolean redirectAfterLogin() {
		return false;
	}

	@Override
	public boolean isSubsequentAuthRequest(HttpServletRequest request) {
		return false;
	}

	static public String getAttribute(ServletRequest request, String key) {
		String headerValue = (String) request.getAttribute(key);
		// Shibboleth Identity Service provides user data in ISO8859-1
		if (headerValue != null) {
			byte[] bytes = headerValue.getBytes(StandardCharsets.ISO_8859_1);
			headerValue = new String(bytes, StandardCharsets.UTF_8);
		}
		return headerValue;
	}
}
