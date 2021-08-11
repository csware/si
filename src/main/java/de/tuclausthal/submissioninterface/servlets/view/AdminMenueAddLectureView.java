/*
 * Copyright 2009-2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.AdminMenue;
import de.tuclausthal.submissioninterface.servlets.controller.Overview;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for adding a new lecture
 * @author Sven Strickroth
 */
@GATEView
public class AdminMenueAddLectureView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		template.addKeepAlive();
		template.addTinyMCE("textarea#description");
		template.printTemplateHeader("neue Veranstaltung", "<a href=\"" + Util.generateHTMLLink(Overview.class.getSimpleName(), response) + "\">Meine Veranstaltungen</a> - <a href=\"" + Util.generateHTMLLink(AdminMenue.class.getSimpleName(), response) + "\">Admin-Menü</a> &gt; neue Veranstaltung");
		PrintWriter out = response.getWriter();

		Lecture dummyLecture = new Lecture();
		dummyLecture.setSemester(Util.getCurrentSemester());
		out.println("<form action=\"" + Util.generateHTMLLink("?action=saveLecture", response) + "\" method=post>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Name der Veranstaltung:</th>");
		out.println("<td><input type=text name=name required=required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Lösungen müssen abgenommen werden:</th>");
		out.println("<td><input type=checkbox name=requiresAbhnahme></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Semester:</th>");
		out.println("<td>" + dummyLecture.getReadableSemester() + "</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Gruppenweise Bewertung:</th>");
		out.println("<td><input type=checkbox name=groupWise></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Beschreibung/Hinweise:</th>");
		out.println("<td><textarea cols=60 rows=20 id=description name=description></textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Selbstanmeldung durch Studierende erlauben:</th>");
		out.println("<td><input type=checkbox name=allowselfsubscribe checked> <a href=\"#\" onclick=\"toggleVisibility('selfsubscribehelp'); return false;\">(?)</a><br><span style=\"display:none;\" id=selfsubscribehelp><b>Hilfe:</b><br>Wenn deaktiviert, können sich Studierende nicht selbst in die Veranstaltung eintragen. Studierende können über ihre E-Mail-Adressen manuell zur Veranstaltung hinzugefügt werden. Es können ausschließlich Studierende hinzugefügt werden, die bereits einen Account in GATE besitzen.</span></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=anlegen> <a href=\"" + Util.generateHTMLLink("?", response) + "\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		template.printTemplateFooter();
	}
}
