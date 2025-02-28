/*
 * Copyright 2021-2023, 2025 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.ShowLecture;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for editing a lecture for advisors
 * @author Sven Strickroth
 */
@GATEView
public class EditLectureView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Lecture lecture = (Lecture) request.getAttribute("lecture");

		Template template = TemplateFactory.getTemplate(request, response);
		template.addKeepAlive();
		template.addTinyMCE("textarea#description");
		template.printTemplateHeader("Veranstaltung bearbeiten", lecture);
		PrintWriter out = response.getWriter();
		out.println("<form method=post action=\"" + Util.generateHTMLLink("?", response) + "\">");
		out.println("<input type=hidden name=lecture value=" + lecture.getId() + ">");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Name der Veranstaltung:</th>");
		out.println("<td><input type=text name=name required value=\"" + Util.escapeHTML(lecture.getName()) + "\" disabled></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Semester:</th>");
		out.println("<td>" + lecture.getReadableSemester() + "</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Beschreibung/Hinweise:</th>");
		out.println("<td><textarea cols=60 rows=20 id=description name=description>" + Util.escapeHTML(lecture.getDescription()) + "</textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Gruppenweise Bewertung:</th>");
		out.println("<td><input type=checkbox name=groupWise" + ("groupWise".equals(lecture.getGradingMethod()) ? " checked" : "") + " disabled></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Lösungen müssen abgenommen werden:</th>");
		out.println("<td><input type=checkbox name=requiresAbhnahme" + (lecture.isRequiresAbhnahme() ? " checked" : "") + " disabled></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Selbstanmeldung durch Studierende erlauben:</th>");
		out.println("<td><input type=checkbox name=allowselfsubscribe" + (lecture.isAllowSelfSubscribe() ? " checked" : "") + "> <a href=\"#\" onclick=\"toggleVisibility('selfsubscribehelp'); return false;\">(?)</a><br><span style=\"display:none;\" id=selfsubscribehelp><b>Hilfe:</b><br>Wenn deaktiviert, können sich Studierende nicht mehr selbst in die Veranstaltung eintragen. Nach dem Speichern können Studierende über ihre E-Mail-Adressen hier manuell zur Veranstaltung hinzugefügt werden. Es können ausschließlich Studierende hinzugefügt werden, die bereits einen Account in GATE besitzen.</span></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid>");
		out.println("<input type=submit value=\"speichern\"> <a href=\"" + Util.generateHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + lecture.getId(), response) + "\">Abbrechen</a></td>");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		out.println("<h2>Teilnehmende hinzufügen</h2>");
		out.println("<form method=post action=\"" + Util.generateHTMLLink("?", response) + "\">");
		out.println("<input type=hidden name=action value=addParticipants>");
		out.println("<input type=hidden name=lecture value=" + lecture.getId() + ">");
		out.println("<textarea name=mailadresses cols=60 placeholder=\"E-Mail-Adressen, eine pro Zeile\"></textarea><br>");
		out.println("<input type=checkbox name=failonerror checked id=failonerror> <label for=failonerror>Bei Fehler abbrechen</label><br>");
		out.println("<input type=submit value=\"Teilnehmende hinzufügen\">");
		out.println("</form>");

		template.printTemplateFooter();
	}
}
