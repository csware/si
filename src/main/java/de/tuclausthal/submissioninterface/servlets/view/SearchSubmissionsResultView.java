/*
 * Copyright 2011-2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.ShowSubmission;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying search results
 * @author Sven Strickroth
 */
@GATEView
public class SearchSubmissionsResultView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Task task = (Task) request.getAttribute("task");
		@SuppressWarnings("unchecked")
		Set<Submission> foundSubmissions = (Set<Submission>) request.getAttribute("results");

		template.printTemplateHeader("Suchergebnisse", task);
		PrintWriter out = response.getWriter();
		if (!foundSubmissions.isEmpty()) {
			out.println("<table>");
			for (Submission submission : foundSubmissions) {
				out.println("<tr>");
				out.println("<td><a href=\"" + Util.generateHTMLLink(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">" + Util.escapeHTML(submission.getSubmitterNames()) + "</a></td>");
				//out.println("<td><a href=\"\">" + submission.getSubmissionid() + "</a></td>");
				out.println("</tr>");
			}
			out.println("</table>");
		}
		template.printTemplateFooter();
	}
}
