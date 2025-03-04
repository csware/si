/*
 * Copyright 2011-2012, 2020-2021, 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2011 Joachim Schramm
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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.Util;

@GATEView
public class PerformStudentTestArgoUMLView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		TestExecutorTestResult testResult = (TestExecutorTestResult) request.getAttribute("testresult");
		Test test = (Test) request.getAttribute("test");

		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<b>Titel:</b> " + Util.escapeHTML(test.getTestTitle()) + "<br>");
		out.println("<b>Beschreibung:</b><br>" + Util.escapeHTML(test.getTestDescription()) + "<br>");
		if (!testResult.getTestOutput().isEmpty()) {
			out.println("<b>Ausgabe:</b><br><pre>" + Util.escapeHTML(testResult.getTestOutput()) + "</pre>");
		}
		out.println("</html>");
	}
}
