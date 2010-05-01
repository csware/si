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

/**
 * View-Servlet for displaying a java-sourcecode file
 * @author Sven Strickroth
 */
public class ShowFileView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String fileName = (String) request.getAttribute("fileName");
		String code = (String) request.getAttribute("code");

		if (fileName.toLowerCase().endsWith(".java")) {
			showWithRenderer(response, fileName, code, XhtmlRendererFactory.JAVA);
		} else if (fileName.toLowerCase().endsWith(".htm") || fileName.toLowerCase().endsWith(".html")) {
			showWithRenderer(response, fileName, code, XhtmlRendererFactory.HTML);
		} else if (fileName.toLowerCase().endsWith(".c") || fileName.toLowerCase().endsWith(".cpp")) {
			showWithRenderer(response, fileName, code, XhtmlRendererFactory.CPP);
		} else if (fileName.toLowerCase().endsWith("xml")) {
			showWithRenderer(response, fileName, code, XhtmlRendererFactory.XML);
		} else {
			response.setContentType("text/plain");
			response.setCharacterEncoding("iso-8859-1");
			PrintWriter out = response.getWriter();
			out.println(code);
		}
	}

	private void showWithRenderer(HttpServletResponse response, String fileName, String code, String renderertype) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("iso-8859-1");
		PrintWriter out = response.getWriter();
		Renderer renderer = XhtmlRendererFactory.getRenderer(renderertype);
		out.write(renderer.highlight(fileName, code, "iso-8859-1", false));
	}
}
