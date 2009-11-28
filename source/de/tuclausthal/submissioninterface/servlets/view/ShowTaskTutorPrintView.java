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

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a task in tutor view
 * @author Sven Strickroth
 */
public class ShowTaskTutorPrintView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = HibernateSessionHelper.getSessionFactory().openSession();

		Task task = (Task) request.getAttribute("task");
		Group group = (Group) request.getAttribute("group");

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		PrintWriter out = response.getWriter();

		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Strict//EN\">");
		out.println("<html>");
		out.println("<body>");

		out.println("<H1>" + Util.mknohtml(task.getTitle()) + "</H1>");
		out.println("<h3>Gruppe: " + Util.mknohtml(group.getName()) + "</h3>");

		if (task.getSubmissions() != null && task.getSubmissions().size() > 0) {
			out.println("<table border=1>");
			out.println("<tr>");
			out.println("<th>Benutzer</th>");
			out.println("<th>Bemerkungen</th>");
			out.println("<th>Punkte</th>");
			out.println("<th>OK?</th>");
			out.println("</tr>");
			Iterator<Submission> submissionIterator = DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOfGroupOrdered(task, group).iterator();

			int lastSID = 0;
			while (submissionIterator.hasNext()) {
				Submission submission = submissionIterator.next();
				if (lastSID != submission.getSubmissionid()) {
					out.println("<tr>");
					out.println("<td>" + Util.mknohtml(submission.getSubmitterNames()) + "</td>");
					lastSID = submission.getSubmissionid();
					if (submission.getPoints() != null) {
						out.println("<td>" + Util.mkTextToHTML(submission.getPoints().getComment()) + "</td>");
						out.println("<td align=right>" + submission.getPoints().getPoints() + "</td>");
						if (submission.getPoints().getPointsOk()) {
							out.println("<td>ok</td>");
						} else {
							out.println("<td></td>");
						}
					} else {
						out.println("<td></td>");
						out.println("<td>n/a</td>");
						out.println("<td></td>");
					}
					out.println("</tr>");
				}
			}
		}
		out.println("</table><p>");
		out.println("</body></html>");
	}
}
