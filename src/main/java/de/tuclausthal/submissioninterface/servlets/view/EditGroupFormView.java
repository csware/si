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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for editing a group
 * @author Sven Strickroth
 */
public class EditGroupFormView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Group group = (Group) request.getAttribute("group");
		Participation participation = (Participation) request.getAttribute("participation");

		template.addJQuery();
		template.addKeepAlive();
		template.printTemplateHeader(group);
		PrintWriter out = response.getWriter();
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=action value=editGroup>");
		out.println("<input type=hidden name=groupid value=" + group.getGid() + ">");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Gruppe:</th>");
		out.println("<td><input type=text name=title required=required value=\"" + Util.escapeHTML(group.getName()) + "\" " + ((participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) ? "" : "readonly") + "></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studierende können sich eintragen:</th>");
		out.println("<td><input type=checkbox name=allowStudentsToSignup " + (group.isAllowStudentsToSignup() ? "checked" : "") + " " + ((participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) ? "" : "disabled") + "></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studierende können wechseln:</th>");
		out.println("<td><input type=checkbox name=allowStudentsToQuit " + (group.isAllowStudentsToQuit() ? "checked" : "") + " " + ((participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) ? "" : "disabled") + "></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Studierende:</th>");
		out.println("<td><input type=text name=maxStudents value=\"" + group.getMaxStudents() + "\" " + ((participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) ? "" : "readonly") + "></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Abgabegruppe:</th>");
		out.println("<td><input type=checkbox name=submissionGroup " + (group.isSubmissionGroup() ? "checked" : "") + " " + ((participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) ? "" : "disabled") + "> <a href=\"#\" onclick=\"$('#submissiongrouphelp').toggle(); return false;\">(?)</a><br><span style=\"display:none;\" id=submissiongrouphelp><b>Hilfe:</b><br>Wird dieses Flag gesetzt, werden alle Mitglieder dieser Gruppe bei der ersten Abgabe automatisch als PartnerInnen hinzugefügt, sofern keine Einzelabgabe bei einer Aufgabe gefordert wird. Zudem können Studierende dieser Gruppe bzw. andere Studierende mit Studierenden dieser Gruppe, selbst wenn gruppenübergreifenden Partnerabgaben bei einer Aufgabe erlaubt sind, keine beliebigen Partnerschaften bilden.</span></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Mitglieder für Studierende einsehbar:</th>");
		out.println("<td><input type=checkbox name=membersvisible " + (group.isMembersVisibleToStudents() ? "checked" : "") + " " + ((participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) ? "" : "disabled") + "></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studierende hinzufügen:</th>");
		out.println("<td><select multiple name=members>");
		Iterator<Participation> participationIterator = DAOFactory.ParticipationDAOIf(RequestAdapter.getSession(request)).getParticipationsWithoutGroup(group.getLecture()).iterator();
		while (participationIterator.hasNext()) {
			Participation thisParticipation = participationIterator.next();
			if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0 || thisParticipation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
				out.println("<option value=" + thisParticipation.getId() + ">" + Util.escapeHTML(thisParticipation.getUser().getFullName()) + "</option>");
			}
		}
		out.println("</select></td>");
		out.println("</tr>");
		if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) {
			out.println("<tr>");
			out.println("<th>Verantwortliche TutorInnen hinzufügen:</th>");
			out.println("<td><select multiple name=tutors>");
			participationIterator = DAOFactory.ParticipationDAOIf(RequestAdapter.getSession(request)).getMarkersAvailableParticipations(group).iterator();
			while (participationIterator.hasNext()) {
				Participation thisParticipation = participationIterator.next();
				if (!group.getTutors().contains(thisParticipation)) {
					out.println("<option value=" + thisParticipation.getId() + ">" + Util.escapeHTML(thisParticipation.getUser().getFullName()) + "</option>");
				}
			}
			out.println("</select></td>");
			out.println("</tr>");
		}
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=zuordnen> <a href=\"" + Util.generateHTMLLink("ShowLecture?lecture=" + group.getLecture().getId(), response) + "\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) {
			out.println("<p class=mid><a onclick=\"return confirmLink('Wirklich löschen?')\" href=\"" + Util.generateHTMLLink("AddGroup?lecture=" + group.getLecture().getId() + "&action=deleteGroup&gid=" + group.getGid(), response) + "\">Gruppe löschen</a></td>");
		}
		template.printTemplateFooter();
	}
}
