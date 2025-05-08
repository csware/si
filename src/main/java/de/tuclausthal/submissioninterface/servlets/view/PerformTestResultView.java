/*
 * Copyright 2009-2012, 2017, 2020-2025 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.servlets.view.fragments.ShowHaskellSyntaxTestResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.HaskellSyntaxTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.view.fragments.ShowDockerTestResult;
import de.tuclausthal.submissioninterface.servlets.view.fragments.ShowJavaAdvancedIOTestResult;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a testresult
 * @author Sven Strickroth
 */
@GATEView
public class PerformTestResultView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		TestExecutorTestResult testResult = (TestExecutorTestResult) request.getAttribute("testresult");
		Test test = (Test) request.getAttribute("test");
		Participation participation = (Participation) request.getAttribute("participation");

		Template template = TemplateFactory.getTemplate(request, response);
		template.addDiffJs();
		template.printTemplateHeader("Testergebnis", test.getTask());

		PrintWriter out = response.getWriter();
		out.println("<b>Titel:</b> " + Util.escapeHTML(test.getTestTitle()) + "<br>");
		if (test.getTestDescription() != null && !test.getTestDescription().isEmpty()) {
			out.println("<b>Beschreibung:</b><br>" + Util.textToHTML(test.getTestDescription()) + "<br>");
		}
		out.println("<b>Bestanden:</b> " + Util.boolToHTML(testResult.isTestPassed()) + "<br>");
		if (!testResult.getTestOutput().isEmpty()) {
			if (test instanceof JavaAdvancedIOTest jaiot) {
				ShowJavaAdvancedIOTestResult.printTestResults(out, jaiot, testResult.getTestOutput(), (participation == null || !participation.getRoleType().equals(ParticipationRole.ADVISOR)), null);
			} else if (test instanceof HaskellSyntaxTest hst) {
				ShowHaskellSyntaxTestResult.printTestResults(out, hst, testResult.getTestOutput(), (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0), null);
			} else if (test instanceof DockerTest dt) {
				ShowDockerTestResult.printTestResults(out, dt, testResult.getTestOutput(), (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0), null);
			} else {
				out.println("<b>Ausgabe:</b><br><pre>" + Util.escapeHTML(testResult.getTestOutput()) + "</pre>");
			}
		}

		template.printTemplateFooter();
	}
}
