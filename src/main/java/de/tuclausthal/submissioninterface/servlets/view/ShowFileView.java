/*
 * Copyright 2009 - 2011,2013 Sven Strickroth <email@cs-ware.de>
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

import com.uwyn.jhighlight.renderer.Renderer;
import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.StripCommentsNormalizer;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a java-sourcecode file
 * @author Sven Strickroth
 */
public class ShowFileView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Submission submission = (Submission) request.getAttribute("submission");
		String fileName = (String) request.getAttribute("fileName");
		StringBuffer code = (StringBuffer) request.getAttribute("code");

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		PrintWriter out = response.getWriter();

		StringBuffer options = new StringBuffer();

		options.append("<script type=\"text/javascript\">\n//<![CDATA[\nif (window.name.match(\"^iframe\")==\"iframe\") { document.write('<a href=\"#\" onclick=\"this.href=document.location\" target=\"_blank\">(new window)</a>'); }\n//]]>\n</script>");

		StringBuffer renderedCode = new StringBuffer();
		if (fileName.toLowerCase().endsWith(".java")) {
			if ("off".equals(request.getParameter("comments"))) {
				StripCommentsNormalizer scn = new StripCommentsNormalizer();
				code = scn.normalize(code);
				options.append(" <a href=\"" + response.encodeURL("?sid=" + submission.getSubmissionid()) + "\">(toggle comments)</a>");
			} else {
				options.append(" <a href=\"" + response.encodeURL("?sid=" + submission.getSubmissionid() + "&amp;comments=off") + "\">(toggle comments)</a>");
			}
			showWithRenderer(renderedCode, fileName, code, XhtmlRendererFactory.JAVA);
		} else if (fileName.toLowerCase().endsWith(".htm") || fileName.toLowerCase().endsWith(".html")) {
			showWithRenderer(renderedCode, fileName, code, XhtmlRendererFactory.HTML);
		} else if (fileName.toLowerCase().endsWith(".c") || fileName.toLowerCase().endsWith(".cpp")) {
			showWithRenderer(renderedCode, fileName, code, XhtmlRendererFactory.CPP);
		} else if (fileName.toLowerCase().endsWith(".xml") || fileName.toLowerCase().endsWith(".classpath") || fileName.toLowerCase().endsWith(".project")) {
			showWithRenderer(renderedCode, fileName, code, XhtmlRendererFactory.XML);
		}

		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n");
		out.println("<head>");
		Template template = TemplateFactory.getTemplate(request, response);
		template.printStyleSheets(out);
		out.println("<title>" + Util.escapeHTML(fileName) + "</title>");
		out.println("<script type=\"text/javascript\" language=\"JavaScript\" src=\"" + request.getContextPath() + "/scripts.js\"></script>");
		out.println("</head>");
		out.println("<body class=\"filepreview\">");

		if (renderedCode.length() == 0) {
			//http://www.css4you.de/Texteigenschaften/white-space.html
			//http://myy.helia.fi/~karte/pre-wrap-css3-mozilla-opera-ie.html
			if ("yes".equals(request.getParameter("wrap"))) {
				renderedCode.append("<pre class=\"wrap\">");
			} else {
				renderedCode.append("<pre>");
			}
			renderedCode.append(Util.textToHTML(code.toString()).replace("<br>", "<br />") + "</pre>"); // XHTML here!

			if ("yes".equals(request.getParameter("wrap"))) {
				options.append(" <a href=\"" + response.encodeURL("?sid=" + submission.getSubmissionid()) + "\">(toggle wrapping)</a>");
			} else {
				options.append(" <a href=\"" + response.encodeURL("?sid=" + submission.getSubmissionid() + "&amp;wrap=yes") + "\">(toggle wrapping)</a>");
			}
		}
		options.append(" <a href='#' onclick=\"selectAll('fileContents'); return false;\">(select all)</a>");
		if (options.length() > 0) {
			out.println("<div class=\"previewmenubox inlinemenu\">" + options.toString() + "</div>");
		}

		out.println("<h1>" + Util.escapeHTML(fileName) + "</h1>");
		out.println("<div id=\"fileContents\">" + renderedCode.toString() + "</div>");
		out.println("</body></html>");
	}

	private void showWithRenderer(StringBuffer renderedCode, String fileName, StringBuffer code, String renderertype) throws IOException {
		Renderer renderer = XhtmlRendererFactory.getRenderer(renderertype);
		renderedCode.append("<code>" + renderer.highlight(fileName, code.toString(), "UTF-8", true) + "</code>");
	}
}
