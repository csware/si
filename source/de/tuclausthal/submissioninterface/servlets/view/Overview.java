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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying the startpage of the system
 * @author Sven Strickroth
 */
public class Overview extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();
		template.printTemplateHeader("Meine Veranstaltungen", "Meine Veranstaltungen");

		Session session = HibernateSessionHelper.getSession();
		User user = new SessionAdapter(request).getUser(session);

		if (user instanceof Student) {
			Student student = (Student) user;
			if (student.getStudiengang() == null) {
				out.println("<p><form class=\"highlightborder mid\" action=\"" + response.encodeURL("AlterUser") + "\" method=post>");

				String studiengang = "";
				if (student.getStudiengang() != null) {
					studiengang = Util.mknohtml(student.getStudiengang());
				}
				out.println("Bitte nennen Sie Ihren Studiengang: <input type=text name=studiengang size=40 value=\"" + studiengang + "\"> <input type=submit value=\"speichern...\">");
				out.println("</form></p><br>");
			}
		}

		if (user.getLectureParticipant().size() > 0) {
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Veranstaltung</th>");
			out.println("<th>Semester</th>");
			out.println("</tr>");
			for (Participation participation : user.getLectureParticipant()) {
				out.println("<tr>");
				out.println("<td><a href=\"" + response.encodeURL("ShowLecture?lecture=" + participation.getLecture().getId()) + "\">" + Util.mknohtml(participation.getLecture().getName()) + "</a></td>");
				out.println("<td>" + participation.getLecture().getReadableSemester() + "</td>");
				out.println("</tr>");
			}
			out.println("</table><p>");
		}
		out.println("<div class=mid><a href=\"" + response.encodeURL("SubscribeToLecture") + "\">In eine Veranstaltung eintragen...</a></div>");

		template.printTemplateFooter();
	}
}
