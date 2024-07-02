/*
 * Copyright 2009-2012, 2020-2024 Sven Strickroth <email@cs-ware.de>
 * Copyright 2023 Marvin Hager
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

import de.tuclausthal.submissioninterface.persistence.datamodel.CommonError;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.ShowFile;
import de.tuclausthal.submissioninterface.servlets.view.fragments.ShowDockerTestResult;
import de.tuclausthal.submissioninterface.servlets.view.fragments.ShowJavaAdvancedIOTestResult;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a submission to a student
 *
 * @author Marvin Hager
 */
@GATEView
public class ShowSubmissionStudentView extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		template.addDiffJs();
		template.addKeepAlive();
		template.addHead("<script>function hideCodePreview(id) { document.getElementById('codepreview' + id).style.display='none';document.getElementById('showbtn' + id).style.display='block'; }</script>");

		Submission submission = (Submission) request.getAttribute("submission");
		CommonError commonError = (CommonError) request.getAttribute("commonError");
		@SuppressWarnings("unchecked")
		List<String> submittedFiles = (List<String>) request.getAttribute("submittedFiles");

		template.printTemplateHeader(commonError, submission, "Testübersicht");

		PrintWriter out = response.getWriter();
		StringBuilder javaScript = new StringBuilder();

		if (!submission.getTestResults().isEmpty()) {
			out.println("<h2>Tests: <a href=\"#\" onclick=\"toggleVisibility('tests'); return false;\">(+/-)</a></h2>");
			out.println("<ul id=tests>");
			for (TestResult testResult : submission.getTestResults()) {
				out.println("<li>" + Util.escapeHTML(testResult.getTest().getTestTitle()) + "<br>");
				out.println("<b>Erfolgreich:</b> " + Util.boolToHTML(testResult.getPassedTest()));
				if (!testResult.getTestOutput().isEmpty()) {
					if (testResult.getTest() instanceof JavaAdvancedIOTest) {
						out.println("<br>");
						ShowJavaAdvancedIOTestResult.printTestResults(out, (JavaAdvancedIOTest) testResult.getTest(), testResult.getTestOutput(), true, javaScript);
					} else if (testResult.getTest() instanceof DockerTest) {
						out.println("<br>");
						ShowDockerTestResult.printTestResults(out, (DockerTest) testResult.getTest(), testResult.getTestOutput(), true, javaScript);
					} else {
						out.println("<br><textarea id=\"testresult" + testResult.getId() + "\" cols=80 rows=15>" + Util.escapeHTML(testResult.getTestOutput()) + "</textarea>");
					}
				}
				out.println("</li>");
			}
			out.println("</ul>");
		}

		if (!submittedFiles.isEmpty()) {
			out.println("<h2>Dateien: <a href=\"#\" onclick=\"toggleVisibility('files'); return false;\">(+/-)</a></h2>");
			out.println("<div id=files class=mid>");
			int id = 0;
			for (String file : submittedFiles) {
				file = file.replace(System.getProperty("file.separator"), "/");
				if (ShowFile.isInlineAble(file.toLowerCase())) {
					out.println("<h3 class=files>" + Util.escapeHTML(file) + " <a id=\"showbtn" + id + "\" style=\"display: none;\" href=\"#\" onclick='document.getElementById(\"codepreview" + id + "\").style.display=\"block\";document.getElementById(\"showbtn" + id + "\").style.display=\"none\";return false;'>(show)</a></h3>");
					out.println("<div id=\"codepreview" + id + "\" class=\"mid\">");
					out.println("<div class=\"inlinemenu\">");
					out.println("<a id=\"hidebtn" + id + "\" href=\"#\" onclick='hideCodePreview(\"" + id + "\");return false;'>(hide)</a>");
					out.println("</div>");
					out.println("<div id=\"resizablecodepreview" + id + "\" class=\"mid inlinefile resizer\">");
					out.println("<iframe name=\"iframe" + id + "\" id=\"iframe" + id + "\" scrolling=\"yes\" width=\"100%\" height=\"100%\" src=\"" + Util.generateHTMLLink(ShowFile.class.getSimpleName() + "/" + Util.encodeURLPathComponent(file) + "?sid=" + submission.getSubmissionid(), response) + "\"></iframe></div>");
					out.println("</div>");
				} else {
					out.println("<h3 class=files>" + Util.escapeHTML(file) + "</h3>");
				}
				out.println("<a href=\"" + Util.generateHTMLLink(ShowFile.class.getSimpleName() + "/" + Util.encodeURLPathComponent(file) + "?download=true&sid=" + submission.getSubmissionid(), response) + "\">Download " + Util.escapeHTML(file) + "</a><p>");
				id++;
			}
			out.println("</div>");
		}

		if (javaScript.length() != 0) {
			out.println("<script>" + javaScript.toString() + "</script>");
		}

		template.printTemplateFooter();
	}
}
