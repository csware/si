/*
 * Copyright 2021 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for deleting a file
 * @author Sven Strickroth
 */
@GATEView
public class DeleteFileView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Submission submission = (Submission) request.getAttribute("submission");
		String filename = (String) request.getAttribute("filename");

		Template template = TemplateFactory.getTemplate(request, response);
		template.printTemplateHeader("Datei löschen", submission.getTask());

		PrintWriter out = response.getWriter();
		out.println("<FORM class=mid method=POST action=\"" + Util.generateHTMLLink("?sid=" + submission.getSubmissionid(), response) + "\">");
		out.println("<p>Sie Sie sicher, dass Sie die Datei \"" + Util.escapeHTML(filename) + "\" löschen möchten?</p>");
		out.println("<p>Dateien gleichen Namens werden auch durch erneutes Hochladen überschrieben.</p>");
		out.println("<INPUT TYPE=submit VALUE=\"Datei löschen\"> <a href=\"" + Util.generateAbsoluteServletsHTMLLink("ShowTask?taskid=" + submission.getTask().getTaskid(), request, response) + "\">Abbrechen</a>");
		out.println("</FORM>");
		template.printTemplateFooter();
	}
}
