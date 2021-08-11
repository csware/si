/*
 * Copyright 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.template.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.controller.AdminMenue;
import de.tuclausthal.submissioninterface.servlets.controller.Logout;
import de.tuclausthal.submissioninterface.servlets.controller.SwitchLogin;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * A simple template for GATE
 * @author Sven Strickroth
 */
public class SimpleTemplate extends Template {
	public SimpleTemplate(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		super(servletRequest, servletResponse);
	}

	@Override
	public void printTemplateHeader(String title, String breadCrumb) throws IOException {
		servletResponse.setContentType("text/html");
		servletResponse.setCharacterEncoding("UTF-8");
		PrintWriter out = servletResponse.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html lang=\"de\">");
		out.println("<head>");
		printStyleSheets(out);
		out.println("<title>GATE: " + title + "</title>");
		getHeads();
		out.println("</head>");
		out.println("<body>");
		out.println("<div id=layout>");

		out.println("<div id=header>");
		out.println("<div id=logo>");
		out.println("<h1>GATE</h1>");
		out.println("</div>");

		out.println("<div id=options>");
		User user = requestAdapter.getUser();
		if (user != null) {
			out.println("Angemeldet als: " + Util.escapeHTML(user.getEmail()));
			if (user.isSuperUser()) {
				out.println(" - <a href=\"" + Util.generateAbsoluteServletsHTMLLink(AdminMenue.class.getSimpleName(), servletRequest, servletResponse) + "\">Admin-Menü</a>");
			}
			if (requestAdapter.isPrivacyMode()) {
				out.println(" - Privacy-Mode");
			} else if (requestAdapter.isIntranet()) {
				out.println(" - <a href=\"" + Util.generateAbsoluteServletsHTMLLink(SwitchLogin.class.getSimpleName() + "?uid=" + user.getUid(), servletRequest, servletResponse) + "\">Tutor Login</a>");
			}
			out.println(" - <a href=\"" + Util.generateAbsoluteServletsHTMLLink(Logout.class.getSimpleName(), servletRequest, servletResponse) + "\">LogOut</a>");
		} else {
			out.println("nicht eingeloggt");
		}
		out.println("</div>");
		out.println("<hr class=divider>");
		out.println("<div id=breadcrumb>");
		out.println(breadCrumb);
		out.println("</div>");
		out.println("</div>");
		out.println("<div id=\"content\">");
		out.println("<h1>" + title + "</h1>");
	}

	@Override
	public void printTemplateFooter() throws IOException {
		PrintWriter out = servletResponse.getWriter();
		out.println("</div>");
		out.println("<hr class=divider>");
		out.println("<div id=footer>");
		out.println("<a href=\"mailto:" + Configuration.getInstance().getAdminMail() + "\">Kontakt</a>");
		out.println("</div>");
		out.println("</div>");
		out.println("</body>");
		out.println("</html>");
	}

	@Override
	public void printStyleSheets(PrintWriter out) {
		out.println("<link rel=\"stylesheet\" href=\"" + prefix + "/template/simple/formate.css\">");
		out.println("<link rel=\"stylesheet\" href=\"" + prefix + "/template/simple/si.css\">");
	}

	@Override
	public List<String> getStyleSheetsForWYSIWYGEditor() {
		return Arrays.asList(prefix + "/template/simple/si.css");
	}
}
