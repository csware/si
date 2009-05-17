/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for editing a lecture
 * @author Sven Strickroth
 */
public class AdminMenueEditLectureView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();
		Lecture lecture = (Lecture) request.getAttribute("lecture");
		if (lecture == null) {
			template.printTemplateHeader("Veranstaltung nicht gefunden");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
		} else {
			template.printTemplateHeader("Veranstaltung \"" + Util.mknohtml(lecture.getName()) + "\" bearbeiten", "<a href=\"Overview\">Meine Veranstaltungen</a> - <a href=\"AdminMenue\">Admin-Menü</a> &gt; Veranstaltung \"" + Util.mknohtml(lecture.getName()) + "\" bearbeiten");
			out.println("<p class=mid><a href=\"" + response.encodeURL("?action=deleteLecture&amp;lecture=" + lecture.getId()) + "\">Veranstaltung löschen</a></p>");
			out.println("<h2>Betreuer</h2>");
			Iterator<Participation> advisorIterator = lecture.getParticipants().iterator();
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Benutzer</th>");
			out.println("<th>Entfernen</th>");
			out.println("</tr>");
			while (advisorIterator.hasNext()) {
				Participation participation = advisorIterator.next();
				if (participation.getRoleType() == ParticipationRole.ADVISOR) {
					User user = participation.getUser();
					out.println("<tr>");
					out.println("<td>" + user.getFullName() + "</td>");
					out.println("<td><a href=\"" + response.encodeURL("?action=removeUser&amp;lecture=" + lecture.getId() + "&amp;userid=" + user.getUid()) + "\">degradieren</a></td>");
					out.println("</tr>");
				}
			}
			out.println("<tr>");
			out.println("<td colspan=2>");
			printAddUserForm(out, lecture, "advisor");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table><p>");

			out.println("<h2>Tutoren</h2>");
			Iterator<Participation> tutorIterator = lecture.getParticipants().iterator();
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Benutzer</th>");
			out.println("<th>Entfernen</th>");
			out.println("</tr>");
			while (tutorIterator.hasNext()) {
				Participation participation = tutorIterator.next();
				if (participation.getRoleType() == ParticipationRole.TUTOR) {
					User user = participation.getUser();
					out.println("<tr>");
					out.println("<td>" + user.getFullName() + "</td>");
					out.println("<td><a href=\"" + response.encodeURL("?action=removeUser&amp;lecture=" + lecture.getId() + "&amp;userid=" + user.getUid()) + "\">degradieren</a></td>");
					out.println("</tr>");
				}
			}
			out.println("<tr>");
			out.println("<td colspan=2>");
			printAddUserForm(out, lecture, "tutor");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table><p>");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
		}
		template.printTemplateFooter();
	}

	public void printAddUserForm(PrintWriter out, Lecture lecture, String type) {
		Iterator<Participation> iterator = lecture.getParticipants().iterator();
		out.println("<form action=\"?\">");
		out.println("<input type=hidden name=action value=addUser>");
		out.println("<input type=hidden name=type value=" + type + ">");
		out.println("<input type=hidden name=lecture value=" + lecture.getId() + ">");
		out.println("<select name=userid>");
		while (iterator.hasNext()) {
			Participation participation = iterator.next();
			if (participation.getRoleType() == ParticipationRole.NORMAL) {
				User user = participation.getUser();
				out.println("<option value=" + user.getUid() + ">" + user.getFullName());
			}
		}
		out.println("</select>");
		out.println("<input type=submit value=\"hinzufügen\">");
		out.println("</form>");
	}
}
