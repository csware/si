/*
 * Copyright 2009-2012 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;

/**
 * View-Servlet for displaying a java-sourcecode file
 * @author Sven Strickroth
 */
public class PerformStudentTestRunningView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String url = (String) request.getAttribute("refreshurl");
		int redirectTime = (Integer) request.getAttribute("redirectTime");

		Template template = TemplateFactory.getTemplate(request, response);

		// http://en.wikipedia.org/w/index.php?title=URL_redirection&oldid=313386868#Refresh_Meta_tag_and_HTTP_refresh_header
		response.addHeader("Refresh", redirectTime + "; url=" + url);

		Task task = (Task) request.getAttribute("task");

		template.printTemplateHeader("Testen...", task);

		PrintWriter out = response.getWriter();
		out.println("Der Test wird im Hintergrund ausgeführt. Bitte warten.<br>");
		out.println("Sollte diese Seite nicht innerhalb von " + redirectTime + " Sekunden neu geladen werden, <a href=\"" + response.encodeURL(url) + "\">hier</a> gehts weiter.");

		template.printTemplateFooter();
	}
}
