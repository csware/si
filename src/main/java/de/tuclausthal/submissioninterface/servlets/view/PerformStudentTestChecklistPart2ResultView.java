/*
 * Copyright 2022 Sven Strickroth <email@cs-ware.de>
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.ChecklistTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.ChecklistTestCheckItem;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a testresult
 * @author Sven Strickroth
 */
@GATEView
public class PerformStudentTestChecklistPart2ResultView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		ChecklistTest test = (ChecklistTest) request.getAttribute("test");
		@SuppressWarnings("unchecked")
		List<Integer> checkedByStudent = (List<Integer>) request.getAttribute("checkedByStudent");

		Template template = TemplateFactory.getTemplate(request, response);
		template.printTemplateHeader("Testergebnis", test.getTask());

		PrintWriter out = response.getWriter();
		out.println("<b>Titel:</b> " + Util.escapeHTML(test.getTestTitle()) + "<br>");

		boolean passedTest = true;

		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Test</th>");
		out.println("<th>Richtige<br>Antwort</th>");
		out.println("<th>Ihre<br>Antwort</th>");
		out.println("<th>OK?</th>");
		out.println("</tr>");
		for (ChecklistTestCheckItem checkItem : test.getCheckItems()) {
			boolean checked = checkedByStudent.contains(checkItem.getCheckitemid());
			boolean correct = checked == checkItem.isCorrect();
			passedTest &= correct;
			out.println("<tr>");
			out.print("<td>" + Util.makeCleanHTML(checkItem.getTitle()));
			if (!correct && !checkItem.getFeedback().isBlank()) {
				out.println("<br><br><b>Hinweis:</b><br>" + Util.makeCleanHTML(checkItem.getFeedback()));
			}
			out.println("</td>");
			out.println("<td>" + (checkItem.isCorrect() ? "ja" : "nein") + "</td>");
			out.println("<td>" + (checked ? "ja" : "nein") + "</td>");
			out.println("<td>" + Util.boolToHTML(correct) + "</td>");
			out.println("</tr>");
		}
		out.println("</table>");

		out.println("<b>Bestanden:</b> " + Util.boolToHTML(passedTest) + "<br>");

		template.printTemplateFooter();
	}
}
