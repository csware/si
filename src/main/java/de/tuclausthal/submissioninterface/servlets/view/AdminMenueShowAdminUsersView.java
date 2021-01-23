/*
 * Copyright 2009-2010, 2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying the admin users and an add form for new ones
 * @author Sven Strickroth
 */
public class AdminMenueShowAdminUsersView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		template.printTemplateHeader("Super User", "<a href=\"" + Util.generateHTMLLink("Overview", response) + "\">Meine Veranstaltungen</a> - <a href=\"" + Util.generateHTMLLink("AdminMenue", response) + "\">Admin-Menü</a> &gt; Super User");
		PrintWriter out = response.getWriter();

		@SuppressWarnings("unchecked")
		Iterator<User> userIterator = ((List<User>) request.getAttribute("superusers")).iterator();
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>BenutzerInnen</th>");
		out.println("<th>Entfernen</th>");
		out.println("</tr>");
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			out.println("<tr>");
			out.println("<td>" + Util.escapeHTML(user.getFullName()) + "</td>");
			out.println("<td><a onclick=\"return sendAsPost(this, 'Wirklich entfernen?')\" href=\"" + Util.generateHTMLLink("?action=removeSuperUser&userid=" + user.getUid(), response) + "\">degradieren</a></td>");
			out.println("</tr>");
		}
		out.println("<tr>");
		out.println("<td colspan=2>");
		userIterator = DAOFactory.UserDAOIf(RequestAdapter.getSession(request)).getUsers().iterator();
		out.println("<form method=post action=\"" + Util.generateHTMLLink("?", response) + "\">");
		out.println("<input type=hidden name=action value=addSuperUser>");
		out.println("<select name=userid>");
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			if (!user.isSuperUser()) {
				out.println("<option value=" + user.getUid() + ">" + Util.escapeHTML(user.getFullName()));
			}
		}
		out.println("</select>");
		out.println("<input type=submit value=\"hinzufügen\">");
		out.println("</form>");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table><p>");

		out.println("<div class=mid><a href=\"" + Util.generateHTMLLink("?", response) + "\">zur Übersicht</a></div>");
		template.printTemplateFooter();
	}
}
