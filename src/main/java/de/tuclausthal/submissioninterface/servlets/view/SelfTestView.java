/*
 * Copyright 2021-2023, 2025 Sven Strickroth <email@cs-ware.de>
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
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.SelfTest.TestResult;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View for the SelfTest
 * @author Sven Strickroth
 */
@GATEView
public class SelfTestView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		@SuppressWarnings("unchecked")
		List<TestResult> testresults = (List<TestResult>) request.getAttribute("testresults");

		Template template = TemplateFactory.getTemplate(request, response);
		template.printAdminMenueTemplateHeader("Selbsttest");
		PrintWriter out = response.getWriter();
		out.println("<table>");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<th>Test</th>");
		out.println("<th>OK?</th>");
		out.println("</tr>");
		out.println("</thead>");
		for (TestResult testresult : testresults) {
			out.println("<tr>");
			out.println("<td>" + testresult.test);
			if (testresult.details != null && !testresult.details.isBlank()) {
				out.println("<pre style=\"white-space: break-spaces;\">" + testresult.details + "</pre>");
			}
			out.println("</td>");
			out.println("<td>" + Util.boolToHTML(testresult.result, "n/a") + "</td>");
			out.println("</tr>");
		}
		out.println("</table>");
		template.printTemplateFooter();
	}
}
