/*
 * Copyright 2021-2022 Sven Strickroth <email@cs-ware.de>
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.ShowTask;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for closing a submission
 * @author Sven Strickroth
 */
@GATEView
public class CloseSubmissionView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Submission submission = (Submission) request.getAttribute("submission");

		Template template = TemplateFactory.getTemplate(request, response);
		template.printTemplateHeader("Final abgeben", submission.getTask());

		PrintWriter out = response.getWriter();
		out.println("<FORM class=mid method=POST action=\"" + Util.generateHTMLLink("?sid=" + submission.getSubmissionid(), response) + "\">");
		out.println("<p>Sind Sie sicher, dass Sie Ihre Lösung wirklich final abgeben möchten?</p>");
		out.println("<p><span class=b>Wichtig</span>: Nach der finalen Abgabe sind keine weiteren Änderungen mehr möglich.<br>Dieser Vorgang kann von Ihnen nicht rückgängig gemacht werden.</span></p>");
		out.println("<INPUT TYPE=submit VALUE=\"final abgeben\"> <a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + submission.getTask().getTaskid(), response) + "\">Abbrechen</a>");
		out.println("</FORM>");
		template.printTemplateFooter();
	}
}
