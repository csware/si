/*
 * Copyright 2009 - 2012 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a testresult
 * @author Sven Strickroth
 */
public class PerformTestResultView extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		TestExecutorTestResult testResult = (TestExecutorTestResult) request.getAttribute("testresult");
		Test test = (Test) request.getAttribute("test");

		Template template = TemplateFactory.getTemplate(request, response);

		template.printTemplateHeader("Testergebnis", test.getTask());

		PrintWriter out = response.getWriter();
		out.println("<b>Titel:</b> " + Util.escapeHTML(test.getTestTitle()) + "<br>");
		out.println("<b>Beschreibung:</b><br>" + Util.textToHTML(test.getTestDescription()) + "<br>");
		out.println("<b>Bestanden:</b> " + Util.boolToHTML(testResult.isTestPassed()) + "<br>");
		if (!testResult.getTestOutput().isEmpty()) {
			out.println("<b>Ausgabe:</b><br><pre>" + Util.escapeHTML(testResult.getTestOutput()) + "</pre>");
		}

		template.printTemplateFooter();
	}
}
