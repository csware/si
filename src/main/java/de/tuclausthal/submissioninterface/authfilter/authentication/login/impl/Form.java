/*
 * Copyright 2009-2010, 2012, 2017, 2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.authfilter.authentication.login.impl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginData;
import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginIf;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;

/**
 * Form-based login method implementation
 * @author Sven Strickroth
 */
public class Form implements LoginIf {
	private static final String LOGONEVIDENCE = "JnffhJGug47fJHGuzf";

	public Form(FilterConfig filterConfig) {}

	@Override
	public void failNoData(HttpServletRequest request, HttpServletResponse response) throws IOException {
		failNoData("", request, response);
	}

	@Override
	public void failNoData(String error, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.addHeader("Cache-Control", "no-cache, must-revalidate");
		Template template = TemplateFactory.getTemplate(request, response);
		template.printTemplateHeader("Login erforderlich");
		PrintWriter out = response.getWriter();
		if (!error.isEmpty()) {
			out.println("<p class=\"red mid\">" + error + "</p>");
		}
		out.print("<form action=\"");
		out.println("\" method=POST name=login>");
		out.println("<input type=hidden name=" + LOGONEVIDENCE + " value='1234'>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>");
		out.println("Benutzername:");
		out.println("</th>");
		out.println("<td>");
		out.println("<input type=text size=20 name=username required=required autofocus=autofocus>");
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>");
		out.println("Passwort:");
		out.println("</th>");
		out.println("<td>");
		out.println("<input type=password size=20 name=password required=required>");
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid>");
		out.println("<input type=submit value=\"Log in\">");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		template.printTemplateFooter();
		out.close();
	}

	@Override
	public LoginData getLoginData(HttpServletRequest request) {
		if (request.getParameter("username") != null && !request.getParameter("username").isEmpty() && request.getParameter("password") != null && !request.getParameter("password").isEmpty()) {
			return new LoginData(request.getParameter("username"), request.getParameter("password"));
		}
		return null;
	}

	@Override
	public boolean requiresVerification() {
		return true;
	}

	@Override
	public boolean redirectAfterLogin() {
		return true;
	}

	@Override
	public boolean isSubsequentAuthRequest(HttpServletRequest request) {
		return request.getParameter(LOGONEVIDENCE) != null;
	}
}
