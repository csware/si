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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for the submission of files
 * @author Sven Strickroth
 */
public class PerformTestTutorFormView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Task task = (Task) request.getAttribute("task");

		template.printTemplateHeader("Test durchführen", task);
		PrintWriter out = response.getWriter();
		out.println("<FORM class=mid ENCTYPE=\"multipart/form-data\" method=POST action=\"" + Util.generateHTMLLink("?", response) + "\">");
		out.println("<p>Test: <select name=\"testid\" size=1 required=required>");
		for (Test test : task.getTests()) {
			out.println("<option value=\"" + test.getId() + "\">" + Util.escapeHTML(test.getTestTitle()) + (test.isForTutors() ? " (Tutortest)" : "") + "</option>");
		}
		out.println("</select></p>");
		out.println("<p>Bitte wählen Sie eine oder mehrere Dateien aus, die Sie testen möchten.</p>");
		out.println("<INPUT TYPE=file NAME=file required multiple>");
		out.println("<INPUT TYPE=submit VALUE=upload>");
		out.println("</FORM>");

		template.printTemplateFooter();
	}
}
