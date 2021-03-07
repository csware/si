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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Servlet implementation class Error500
 */
public class ErrorHandlerView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	final static private Logger LOG = LoggerFactory.getLogger(ErrorHandlerView.class);

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Template template = null;
		try {
			template = TemplateFactory.getTemplate(request, response);
		} catch (Exception e) {
		}
		String title = null;
		String message = null;
		switch ((Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)) {
			case 403:
				title = "Zugriff verweigert (403)";
				message = "Sie haben keine ausreichende Berechtigung (" + Util.escapeHTML((String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE)) + ") für die angefragte Ressource \"" + Util.escapeHTML((String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)) + "\".<br>";
				break;
			case 404:
				title = "Datei nicht gefunden (404)";
				message = "Der Server hat keine aktuelle Darstellung für die angefragte Ressource \"" + Util.escapeHTML((String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)) + "\" gefunden oder ist nicht bereit, die Existenz einer solchen offenzulegen.<br>";
				break;
			case 405:
				title = "Methode nicht erlaubt (405)";
				message = "Die " + Util.escapeHTML(request.getMethod()) + "-Methode ist für die angeforderte Ressource \"" + Util.escapeHTML((String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)) + "\" nicht erlaubt.<br>";
				break;
			case 500:
				title = "Interner Serverfehler (500)";
				Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
				if (throwable != null) {
					message = "Das Skript, auf das Sie versuchen zuzugreifen, hat einen schweren Fehler verursacht (" + Util.escapeHTML(throwable.toString()) + ").<br>";
				} else {
					message = "Das Skript, auf das Sie versuchen zuzugreifen, hat einen schweren Fehler verursacht.<br>";
				}
				break;
			default:
				title = "Unbekannter Fehler (" + request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) + ")";
				message = "Beim Abruf der Ressource ist ein unbekannter Fehler aufgetreten.";
				LOG.error("ErrorHandlerView got called with unknown error: " + request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
				break;
		}
		if (template == null) {
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE html>");
			out.println("<html><head><title>" + Util.escapeHTML(title) + "</title></head><body><h1>" + Util.escapeHTML(title) + "</h1>");
		} else {
			template.printTemplateHeader(Util.escapeHTML(title));
		}

		PrintWriter out = response.getWriter();
		out.println(message);
		out.println("<br>");
		out.println("<b>Sollte dieser Fehler öfter auftreten, wenden Sie sich bitte mit der o.g. Fehlermeldung, der Adresse und Informationen, was Sie gerade versucht haben durchzuführen, an den <a href=\"mailto:" + Configuration.getInstance().getAdminMail() + "\">Webmaster</a>.</b><br>");

		if (template != null) {
			template.printTemplateFooter();
		} else {
			out.println("</body></html>");
		}
	}
}
