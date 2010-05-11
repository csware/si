/*
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a java-sourcecode file
 * @author Sven Strickroth
 */
public class ShowFileView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String fileName = (String) request.getAttribute("fileName");
		String code = (String) request.getAttribute("code");

		response.setContentType("text/html");
		response.setCharacterEncoding("iso-8859-1");

		if (fileName.toLowerCase().endsWith(".java")) {
			showWithRenderer(response, fileName, code, XhtmlRendererFactory.JAVA);
		} else if (fileName.toLowerCase().endsWith(".htm") || fileName.toLowerCase().endsWith(".html")) {
			showWithRenderer(response, fileName, code, XhtmlRendererFactory.HTML);
		} else if (fileName.toLowerCase().endsWith(".c") || fileName.toLowerCase().endsWith(".cpp")) {
			showWithRenderer(response, fileName, code, XhtmlRendererFactory.CPP);
		} else if (fileName.toLowerCase().endsWith(".xml") || fileName.toLowerCase().endsWith(".classpath") || fileName.toLowerCase().endsWith(".project")) {
			showWithRenderer(response, fileName, code, XhtmlRendererFactory.XML);
		} else {
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Strict//EN\">");
			out.println("<html><head><title>" + Util.mknohtml(fileName) + "</title></head><body><h1 style='font-family: sans-serif; font-size: 16pt; font-weight: bold; color: rgb(0,0,0); background: rgb(210,210,210); border: solid 1px black; padding: 5px; text-align: center;'>" + Util.mknohtml(fileName));
			out.println(" <a href=\"javascript:document.getElementById('code').setAttribute('wrap', 'hard')\">Text umbrechen</a>");
			out.println("</h1><pre id=code style='font-family: \"Courier New\",Courier,monospace; font-size: 80%'>");
			out.println(Util.mkTextToHTML(code));
			out.println("</pre></body></html>");
		}
	}

	private void showWithRenderer(HttpServletResponse response, String fileName, String code, String renderertype) throws IOException {
		PrintWriter out = response.getWriter();
		Renderer renderer = XhtmlRendererFactory.getRenderer(renderertype);
		out.write(renderer.highlight(fileName, code, "iso-8859-1", false));
	}
}
