/*
 * Copyright 2011 Joachim Schramm
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
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.Util;

public class PerformTestArgoUMLView extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		TestExecutorTestResult testResult = (TestExecutorTestResult) request.getAttribute("testresult");
		Task task = (Task) request.getAttribute("task");
		Test test = (Test) request.getAttribute("test");

		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<b>Titel:</b> " + Util.makeCleanHTML(test.getTestTitle()) + "<br>");
		out.println("<b>Beschreibung:</b><br>" + Util.escapeHTML(test.getTestDescription()) + "<br>");
		//out.println("<b>Bestanden:</b> " + Util.boolToHTML(testResult.isTestPassed()) + "<br>");
		if (!testResult.getTestOutput().isEmpty()) {
			out.println("<b>Ausgabe:</b><br><pre>" + Util.makeCleanHTML(testResult.getTestOutput()) + "</pre>");
		}
		out.println("</html>");

	}
}
