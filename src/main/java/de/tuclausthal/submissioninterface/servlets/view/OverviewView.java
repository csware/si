/*
 * Copyright 2009-2013, 2020-2025 Sven Strickroth <email@cs-ware.de>
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

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.AlterUser;
import de.tuclausthal.submissioninterface.servlets.controller.ShowLecture;
import de.tuclausthal.submissioninterface.servlets.controller.SubscribeToLecture;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying the startpage of the system
 * @author Sven Strickroth
 */
@GATEView
public class OverviewView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		User user = RequestAdapter.getUser(request);

		template.printTemplateHeader("Meine Veranstaltungen", "<li>Meine Veranstaltungen</li>");
		PrintWriter out = response.getWriter();

		if (Configuration.getInstance().isMatrikelNumberMustBeEnteredManuallyIfMissing() && !(user instanceof Student)) {
			out.println("<p><form class=\"highlightborder mid\" action=\"" + Util.generateHTMLLink(AlterUser.class.getSimpleName(), response) + "\" method=post>");
			out.println("Bitte nennen Sie Ihre Matrikelnummer: <input type=number required=\"required\" name=matrikelno id=matrikelno pattern=\"[0-9]+\" autocomplete=\"off\" size=15\"> <input type=submit value=\"speichern...\">");
			out.println("</form></p><br>");
		}
		if (user instanceof Student student) {
			if (student.getStudiengang() == null) {
				out.println("<p><form class=\"highlightborder mid\" action=\"" + Util.generateHTMLLink(AlterUser.class.getSimpleName(), response) + "\" method=post>");
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
			out.println("<table>");
			out.println("<thead>");
			out.println("<tr>");
			out.println("<th>Veranstaltung</th>");
			out.println("<th>Semester</th>");
			out.println("</tr>");
			out.println("</thead>");
			for (Participation participation : user.getLectureParticipant()) {
				out.println("<tr>");
				out.println("<td><a href=\"" + Util.generateHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + participation.getLecture().getId(), response) + "\">" + Util.escapeHTML(participation.getLecture().getName()) + "</a></td>");
				out.println("<td>" + participation.getLecture().getReadableSemester() + "</td>");
				out.println("</tr>");
			}
			out.println("</table><p>");
		}
		out.println("<div class=mid><a href=\"" + Util.generateHTMLLink(SubscribeToLecture.class.getSimpleName(), response) + "\">In eine Veranstaltung eintragen...</a></div>");

		template.printTemplateFooter();
	}
}
