/*
 * Copyright 2009-2011, 2013, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.StripCommentsNormalizer;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a file as HTML document
 * @author Sven Strickroth
 */
@GATEView
public class ShowFileView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Submission submission = (Submission) request.getAttribute("submission");
		String fileName = (String) request.getAttribute("fileName");
		StringBuffer code = (StringBuffer) request.getAttribute("code");

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		PrintWriter out = response.getWriter();

		StringBuilder options = new StringBuilder();

		options.append("<script>\nif (window.name.match(\"^iframe\")==\"iframe\") { document.write('<a href=\"#\" onclick=\"this.href=document.location\" target=\"_blank\">(new window)</a>'); }\n</script>");

		String rendererType = "none";
		if (fileName.toLowerCase().endsWith(".java")) {
			if ("off".equals(request.getParameter("comments"))) {
				StripCommentsNormalizer scn = new StripCommentsNormalizer();
				code = scn.normalize(code);
				options.append(" <a href=\"" + Util.generateHTMLLink("?sid=" + submission.getSubmissionid(), response) + "\">(comments)</a>");
			} else {
				options.append(" <a href=\"" + Util.generateHTMLLink("?sid=" + submission.getSubmissionid() + "&comments=off", response) + "\">(no comments)</a>");
			}
			rendererType = "java";
		} else if (fileName.toLowerCase().endsWith(".htm") || fileName.toLowerCase().endsWith(".html")) {
			rendererType = "html";
		} else if (fileName.toLowerCase().endsWith(".css")) {
			rendererType = "css";
		} else if (fileName.toLowerCase().endsWith(".hs")) {
			rendererType = "haskell";
		} else if (fileName.toLowerCase().endsWith(".c")) {
			rendererType = "c";
		} else if (fileName.toLowerCase().endsWith(".cpp")) {
			rendererType = "cpp";
		} else if (fileName.toLowerCase().endsWith(".xml") || fileName.toLowerCase().endsWith(".classpath") || fileName.toLowerCase().endsWith(".project")) {
			rendererType = "xml";
		} else if (fileName.toLowerCase().endsWith(".py")) {
			rendererType = "python";
		} else if (fileName.toLowerCase().endsWith(".pl")) {
			rendererType = "prolog";
		} else if (fileName.toLowerCase().endsWith(".js")) {
			rendererType = "javascript";
		}

		if ("yes".equals(request.getParameter("wrap"))) {
			options.append(" <a href=\"" + Util.generateHTMLLink("?sid=" + submission.getSubmissionid(), response) + "\">(no wrap)</a>");
		} else {
			options.append(" <a href=\"" + Util.generateHTMLLink("?sid=" + submission.getSubmissionid() + "&wrap=yes", response) + "\">(wrap)</a>");
		}

		out.println("<!DOCTYPE html>");
		out.println("<html lang=\"de\">");
		out.println("<head>");
		Template template = TemplateFactory.getTemplate(request, response);
		template.printStyleSheets(out);
		out.println("<title>" + Util.escapeHTML(fileName) + "</title>");
		out.println("<script src=\"" + request.getContextPath() + "/assets/scripts.js\"></script>");
		out.println("<link href=\"" + request.getContextPath() + "/assets/prism/prism.css\" rel=\"stylesheet\">");
		out.println("</head>");
		out.println("<body class=\"filepreview\">");
		options.append(" <a href='#' onclick=\"selectAll('fileContents'); return false;\">(select all)</a>");
		if (options.length() > 0) {
			out.println("<div class=\"previewmenubox inlinemenu\">" + options.toString() + "</div>");
		}

		out.println("<h1>" + Util.escapeHTML(fileName) + "</h1>");
		out.print("<pre class=\"line-numbers" + ("yes".equals(request.getParameter("wrap")) ? " wrap" : "") + "\"><code id=fileContents class=\"language-" + rendererType + "\">");
		out.print(Util.escapeHTML(code.toString()));
		out.println("\n</code></pre>");
		out.println("<script src=\"" + request.getContextPath() + "/assets/prism/prism.js\" defer></script>");
		out.println("</body></html>");
	}
}
