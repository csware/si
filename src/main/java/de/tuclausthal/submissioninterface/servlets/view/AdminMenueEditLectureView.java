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
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.AdminMenue;
import de.tuclausthal.submissioninterface.servlets.controller.Overview;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for editing a lecture
 * @author Sven Strickroth
 */
@GATEView
public class AdminMenueEditLectureView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Lecture lecture = (Lecture) request.getAttribute("lecture");
		if (lecture == null) {
			template.printTemplateHeader("Veranstaltung nicht gefunden");
			PrintWriter out = response.getWriter();
			out.println("<div class=mid><a href=\"" + Util.generateHTMLLink("?", response) + "\">zur Übersicht</a></div>");
		} else {
			@SuppressWarnings("unchecked")
			List<Participation> participants = (List<Participation>) request.getAttribute("participants");

			template.addKeepAlive();
			template.addTinyMCE("textarea#description");
			template.printTemplateHeader("Veranstaltung \"" + Util.escapeHTML(lecture.getName()) + "\" bearbeiten", "<a href=\"" + Util.generateHTMLLink(Overview.class.getSimpleName(), response) + "\">Meine Veranstaltungen</a> - <a href=\"" + Util.generateHTMLLink(AdminMenue.class.getSimpleName(), response) + "\">Admin-Menü</a> &gt; Veranstaltung \"" + Util.escapeHTML(lecture.getName()) + "\" bearbeiten");
			PrintWriter out = response.getWriter();
			out.println("<h2>Eigenschaften</h2>");
			out.println("<form method=post action=\"" + Util.generateHTMLLink("?", response) + "\">");
			out.println("<input type=hidden name=lecture value=" + lecture.getId() + ">");
			out.println("<input type=hidden name=action value=editLecture>");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Name der Veranstaltung:</th>");
			out.println("<td><input type=text name=name required value=\"" + Util.escapeHTML(lecture.getName()) + "\"></td>");
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
			out.println("<td><input type=checkbox name=groupWise></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Lösungen müssen abgenommen werden:</th>");
			out.println("<td><input type=checkbox name=requiresAbhnahme" + (lecture.isRequiresAbhnahme() ? " checked" : "") + "></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid>");
			out.println("<input type=submit value=\"Speichern\">");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");

			out.println("<p class=mid><a onclick=\"return sendAsPost(this, 'Wirklich löschen?')\" href=\"" + Util.generateHTMLLink("?action=deleteLecture&lecture=" + lecture.getId(), response) + "\">Veranstaltung löschen</a></p>");

			out.println("<h2>BetreuerInnen</h2>");
			Iterator<Participation> advisorIterator = participants.iterator();
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>BenutzerInnen</th>");
			out.println("<th>Entfernen</th>");
			out.println("</tr>");
			while (advisorIterator.hasNext()) {
				Participation participation = advisorIterator.next();
				if (participation.getRoleType() == ParticipationRole.ADVISOR) {
					User user = participation.getUser();
					out.println("<tr>");
					out.println("<td>" + Util.escapeHTML(user.getFullName()) + "</td>");
					out.println("<td><a onclick=\"return sendAsPost(this, 'Wirklich degradieren?')\" href=\"" + Util.generateHTMLLink("?action=removeUser&lecture=" + lecture.getId() + "&participationid=" + participation.getId(), response) + "\">degradieren</a></td>");
					out.println("</tr>");
				}
			}
			out.println("<tr>");
			out.println("<td colspan=2>");
			printAddUserForm(out, response, lecture, participants, "advisor");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table><p>");

			out.println("<h2>TutorInnen</h2>");
			Iterator<Participation> tutorIterator = participants.iterator();
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>BenutzerInnen</th>");
			out.println("<th>Entfernen</th>");
			out.println("</tr>");
			while (tutorIterator.hasNext()) {
				Participation participation = tutorIterator.next();
				if (participation.getRoleType() == ParticipationRole.TUTOR) {
					User user = participation.getUser();
					out.println("<tr>");
					out.println("<td>" + Util.escapeHTML(user.getFullName()) + "</td>");
					out.println("<td><a onclick=\"return sendAsPost(this, 'Wirklich degradieren?')\" href=\"" + Util.generateHTMLLink("?action=removeUser&lecture=" + lecture.getId() + "&participationid=" + participation.getId(), response) + "\">degradieren</a></td>");
					out.println("</tr>");
				}
			}
			out.println("<tr>");
			out.println("<td colspan=2>");
			printAddUserForm(out, response, lecture, participants, "tutor");
			out.println("</td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2>");
			out.println("<form method=post action=\"" + Util.generateHTMLLink("?", response) + "\">");
			out.println("<input type=hidden name=action value=addUserMulti>");
			out.println("<input type=hidden name=type value=tutor>");
			out.println("<input type=hidden name=lecture value=" + lecture.getId() + ">");
			out.println("<textarea name=mailadresses placeholder=\"E-Mail-Adressen, eine pro Zeile\"></textarea>");
			out.println("<input type=submit value=\"mehrere hinzufügen\">");
			out.println("</form>");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table><p>");
			out.println("<div class=mid><a href=\"" + Util.generateHTMLLink("?", response) + "\">zur Übersicht</a></div>");
		}
		template.printTemplateFooter();
	}

	public void printAddUserForm(PrintWriter out, HttpServletResponse response, Lecture lecture, List<Participation> participants, String type) {
		Iterator<Participation> iterator = participants.iterator();
		out.println("<form method=post action=\"" + Util.generateHTMLLink("?", response) + "\">");
		out.println("<input type=hidden name=action value=addUser>");
		out.println("<input type=hidden name=type value=" + type + ">");
		out.println("<input type=hidden name=lecture value=" + lecture.getId() + ">");
		out.println("<select name=participationid required>");
		while (iterator.hasNext()) {
			Participation participation = iterator.next();
			if (participation.getRoleType() == ParticipationRole.NORMAL) {
				User user = participation.getUser();
				out.println("<option value=" + participation.getId() + ">" + Util.escapeHTML(user.getFullName()));
			}
		}
		out.println("</select>");
		out.println("<input type=submit value=\"hinzufügen\">");
		out.println("</form>");
	}
}
