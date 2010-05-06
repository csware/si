/*
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for editing a group
 * @author Sven Strickroth
 */
public class EditGroupFormView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Group group = (Group) request.getAttribute("group");
		Participation participation = (Participation) request.getAttribute("participation");

		template.printTemplateHeader(group);

		out.println("<form action=\"" + response.encodeURL("?") + "\" method=post>");
		out.println("<input type=hidden name=action value=assignGroup>");
		out.println("<input type=hidden name=groupid value=" + group.getGid() + ">");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Gruppe:</th>");
		out.println("<td><input type=text name=title value=\"" + Util.mknohtml(group.getName()) + "\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studenten können sich eintragen:</th>");
		out.println("<td><input type=checkbox name=allowStudentsToSignup " + (group.isAllowStudentsToSignup() ? "checked" : "") + "></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Studenten können wechseln:</th>");
		out.println("<td><input type=checkbox name=allowStudentsToQuit " + (group.isAllowStudentsToQuit() ? "checked" : "") + "></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Studenten:</th>");
		out.println("<td><input type=text name=maxStudents value=\"" + group.getMaxStudents() + "\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Teilnehmer hinzufügen:</th>");
		out.println("<td><select multiple name=members>");
		Iterator<Participation> participationIterator = DAOFactory.ParticipationDAOIf(HibernateSessionHelper.getSessionFactory().openSession()).getParticipationsWithoutGroup(group.getLecture()).iterator();
		while (participationIterator.hasNext()) {
			Participation thisParticipation = participationIterator.next();
			if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0 || thisParticipation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
				out.println("<option value=" + thisParticipation.getId() + ">" + Util.mknohtml(thisParticipation.getUser().getFullName()) + "</option>");
			}
		}
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=zuordnen> <a href=\"" + response.encodeURL("ShowLecture?lecture=" + group.getLecture().getId()) + "\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0) {
			out.println("<p class=mid><a href=\"" + response.encodeURL("AddGroup?lecture=" + group.getLecture().getId() + "&amp;action=deleteGroup&amp;gid=" + group.getGid()) + "\">Gruppe löschen</a></td>");
		}
		template.printTemplateFooter();
	}
}
