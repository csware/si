/*
 * Copyright 2009-2012, 2020-2021, 2023, 2025 Sven Strickroth <email@cs-ware.de>
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
import java.util.Iterator;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a task in tutor view
 * @author Sven Strickroth
 */
@GATEView
public class ShowTaskTutorPrintView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		Task task = (Task) request.getAttribute("task");
		Group group = (Group) request.getAttribute("group");

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		PrintWriter out = response.getWriter();

		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Strict//EN\">");
		out.println("<html>");
		out.println("<body>");

		out.println("<H1>" + Util.escapeHTML(task.getTitle()) + "</H1>");
		if (group == null) {
			out.println("<h3>Gruppe: ohne Gruppe</h3>");
		} else {
			out.println("<h3>Gruppe: " + Util.escapeHTML(group.getName()) + "</h3>");
		}

		if (task.getSubmissions() != null && !task.getSubmissions().isEmpty()) {
			out.println("<table border=1>");
			out.println("<thead>");
			out.println("<tr>");
			out.println("<th>Abgabe von</th>");
			out.println("<th>Bemerkungen</th>");
			out.println("<th>Punkte</th>");
			out.println("<th>OK?</th>");
			out.println("</tr>");
			out.println("</thead>");
			Iterator<Submission> submissionIterator = DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOfGroupOrdered(task, group).iterator();

			while (submissionIterator.hasNext()) {
				Submission submission = submissionIterator.next();
				out.println("<tr>");
				out.println("<td>" + Util.escapeHTML(submission.getSubmitterNames()) + "</td>");
				if (submission.getPoints() != null && submission.getPoints().getPointStatus() != PointStatus.NICHT_BEWERTET.ordinal()) {
					out.println("<td class=feedback>" + Util.escapeHTML(submission.getPoints().getPublicComment()) + "</td>");
					out.println("<td class=similarity>" + Util.showPoints(submission.getPoints().getPlagiarismPoints(task.getMinPointStep())) + "</td>");
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
		out.println("</table><p>");
		out.println("</body></html>");
	}
}
