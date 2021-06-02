/*
 * Copyright 2009-2013, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.authfilter.authentication.login.impl.Shibboleth;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying the startpage of the system
 * @author Sven Strickroth
 */
public class Overview extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// redirect handler for Shibboleth, not yet perfect but works
		if (request.getParameter(Shibboleth.REDIR_PARAMETER) != null && request.getParameter(Shibboleth.REDIR_PARAMETER).startsWith(Util.generateAbsoluteServletsRedirectURL("", request, response))) {
			response.sendRedirect(Util.generateRedirectURL(request.getParameter(Shibboleth.REDIR_PARAMETER).replace("\r", "%0d").replace("\n", "%0a"), response));
			return;
		}

		Template template = TemplateFactory.getTemplate(request, response);

		User user = RequestAdapter.getUser(request);

		template.printTemplateHeader("Meine Veranstaltungen", "Meine Veranstaltungen");
		PrintWriter out = response.getWriter();

		if (Configuration.getInstance().isMatrikelNumberMustBeEnteredManuallyIfMissing() && !(user instanceof Student)) {
			out.println("<p><form class=\"highlightborder mid\" action=\"" + Util.generateHTMLLink("AlterUser", response) + "\" method=post>");
			out.println("Bitte nennen Sie Ihre Matrikelnummer: <input type=number required=\"required\" name=matrikelno id=matrikelno pattern=\"[0-9]+\" autocomplete=\"off\" size=15\"> <input type=submit value=\"speichern...\">");
			out.println("</form></p><br>");
		}
		if (user instanceof Student) {
			Student student = (Student) user;
			if (student.getStudiengang() == null) {
				out.println("<p><form class=\"highlightborder mid\" action=\"" + Util.generateHTMLLink("AlterUser", response) + "\" method=post>");
				out.println("Bitte nennen Sie Ihren Studiengang: <select required name=studiengang>");
				out.println("<option value=\"\"></option>");
				for (String aStudiengang : Configuration.getInstance().getStudiengaenge()) {
					out.println("<option value=\"" + Util.escapeHTML(aStudiengang) + "\"" + (aStudiengang.equals(student.getStudiengang()) ? " selected" : "") + ">" + Util.escapeHTML(aStudiengang) + "</option>");
				}
				out.println("</select>");
				out.println("<input type=submit value=\"speichern...\"></form></p><br>");
			}
		}

		if (!user.getLectureParticipant().isEmpty()) {
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Veranstaltung</th>");
			out.println("<th>Semester</th>");
			out.println("</tr>");
			for (Participation participation : user.getLectureParticipant()) {
				out.println("<tr>");
				out.println("<td><a href=\"" + Util.generateHTMLLink("ShowLecture?lecture=" + participation.getLecture().getId(), response) + "\">" + Util.escapeHTML(participation.getLecture().getName()) + "</a></td>");
				out.println("<td>" + participation.getLecture().getReadableSemester() + "</td>");
				out.println("</tr>");
			}
			out.println("</table><p>");
		}
		out.println("<div class=mid><a href=\"" + Util.generateHTMLLink("SubscribeToLecture", response) + "\">In eine Veranstaltung eintragen...</a></div>");

		template.printTemplateFooter();
	}
}
