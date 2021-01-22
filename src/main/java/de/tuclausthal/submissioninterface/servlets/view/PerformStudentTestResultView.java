/*
 * Copyright 2009-2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.view.fragments.ShowJavaAdvancedIOTestResult;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a testresult
 * @author Sven Strickroth
 */
public class PerformStudentTestResultView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		TestExecutorTestResult testResult = (TestExecutorTestResult) request.getAttribute("testresult");
		Task task = (Task) request.getAttribute("task");
		Test test = (Test) request.getAttribute("test");

		Template template = TemplateFactory.getTemplate(request, response);
		template.addDiffJs();
		template.printTemplateHeader("Testergebnis", task);

		PrintWriter out = response.getWriter();
		out.println("<b>Titel:</b> " + Util.escapeHTML(test.getTestTitle()) + "<br>");
		if (test.getTestDescription() != null && !test.getTestDescription().isEmpty()) {
			out.println("<b>Beschreibung:</b><br>" + Util.textToHTML(test.getTestDescription()) + "<br>");
		}
		out.println("<b>Bestanden:</b> " + Util.boolToHTML(testResult.isTestPassed()) + "<br>");
		if (test.isGiveDetailsToStudents() && !testResult.getTestOutput().isEmpty()) {
			if (test instanceof JavaAdvancedIOTest) {
				ShowJavaAdvancedIOTestResult.printTestResults(out, (JavaAdvancedIOTest) test, testResult.getTestOutput(), true, null);
			} else {
				out.println("<b>Ausgabe:</b><br><pre>" + Util.escapeHTML(testResult.getTestOutput()) + "</pre>");
			}
		}

		template.printTemplateFooter();
	}
}
