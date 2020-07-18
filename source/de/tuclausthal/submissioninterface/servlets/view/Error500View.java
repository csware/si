/*
 * Copyright 2011-2012 Sven Strickroth <email@cs-ware.de>
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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Servlet implementation class Error500
 */
public class Error500View extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Template template = null;
		try {
			template = TemplateFactory.getTemplate(request, response);
		} catch (Exception e) {
		}
		if (template == null) {
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Strict//EN\">");
			out.println("<html><head><title>Internal Server Error (500)</title></head><body><h1>Internal Server Error (500)</h1>");
		} else {
			template.printTemplateHeader("Internal Server Error (500)");
		}

		PrintWriter out = response.getWriter();

		Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

		if (throwable != null) {
			out.println("Das Skript, auf das Sie versuchen zuzugreifen, hat einen schweren Fehler verursacht (" + Util.escapeHTML(throwable.toString()) + ").<br>");
		} else {
			out.println("Das Skript, auf das Sie versuchen zuzugreifen, hat einen schweren Fehler verursacht.<br>");
		}

		out.println("<br>");
		out.println("<b>Sollte dieser Fehler öfter auftreten, wenden Sie sich bitte mit der o.g. Fehlermeldung, der Adresse und Informationen, was Sie gerade versucht haben durchzuführen, an den <a href=\"mailto:" + new ContextAdapter(getServletContext()).getAdminMail() + "\">Webmaster</a>.</b><br>");

		if (template != null) {
			template.printTemplateFooter();
		} else {
			out.println("</body></html>");
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
