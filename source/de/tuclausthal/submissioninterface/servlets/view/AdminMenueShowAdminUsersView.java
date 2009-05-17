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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;

/**
 * View-Servlet for displaying the admin users and an add form for new ones
 * @author Sven Strickroth
 */
public class AdminMenueShowAdminUsersView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();
		template.printTemplateHeader("Super User", "<a href=\"Overview\">Meine Veranstaltungen</a> - <a href=\"AdminMenue\">Admin-Menü</a> &gt; Super User");

		Iterator<User> userIterator = ((List<User>) request.getAttribute("superusers")).iterator();
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Benutzer</th>");
		out.println("<th>Entfernen</th>");
		out.println("</tr>");
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			out.println("<tr>");
			out.println("<td>" + user.getFullName() + "</td>");
			out.println("<td><a href=\"" + response.encodeURL("?action=removeSuperUser&amp;userid=" + user.getUid()) + "\">degradieren</a></td>");
			out.println("</tr>");
		}
		out.println("<tr>");
		out.println("<td colspan=2>");
		userIterator = DAOFactory.UserDAOIf().getUsers().iterator();
		out.println("<form action=\"?\">");
		out.println("<input type=hidden name=action value=addSuperUser>");
		out.println("<select name=userid>");
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			if (!user.isSuperUser()) {
				out.println("<option value=" + user.getUid() + ">" + user.getFullName());
			}
		}
		out.println("</select>");
		out.println("<input type=submit value=\"hinzufügen\">");
		out.println("</form>");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table><p>");

		out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
		template.printTemplateFooter();
	}
}
