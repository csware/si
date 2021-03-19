/*
 * Copyright 2021 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.ChecklistTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.ChecklistTestCheckItem;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for adding/editing/deleting checklist entries
 * @author Sven Strickroth
 */
@GATEView
public class ChecklistTestManagerOverView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		ChecklistTest test = (ChecklistTest) request.getAttribute("test");

		template.addKeepAlive();
		template.printTemplateHeader("Checklist Test bearbeiten", test.getTask());

		PrintWriter out = response.getWriter();

		// similar code in TestManagerAddTestFormView
		out.println("<h2>" + Util.escapeHTML(test.getTestTitle()) + "</h2>");
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=testid value=\"" + test.getId() + "\">");
		out.println("<input type=hidden name=action value=edittest>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title maxlength=250 size=60 value=\"" + Util.escapeHTML(test.getTestTitle()) + "\" required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.println(Util.generateHTMLLink("TaskManager?action=editTask&taskid=" + test.getTask().getTaskid() + "&lecture=" + test.getTask().getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		out.println("<hr>");
		out.println("<h2>Checklisten-Einträge</h2>");

		for (ChecklistTestCheckItem checkItem : test.getCheckItems()) {
			out.println("<h3>" + Util.makeCleanHTML(checkItem.getTitle()) + "</h2>");
			out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
			out.println("<input type=hidden name=testid value=\"" + test.getId() + "\">");
			out.println("<input type=hidden name=checkitemid value=\"" + checkItem.getCheckitemid() + "\">");
			out.println("<input type=hidden name=action value=updateCheckItem>");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Eintrag:</th>");
			out.println("<td><input type=text name=title maxlength=250 size=60 value=\"" + Util.escapeHTML(checkItem.getTitle()) + "\" required></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid><input type=submit value=speichern></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid>");
			out.println("<a onclick=\"return sendAsPost(this, 'Wirklich löschen?')\" href=\"");
			out.println(Util.generateHTMLLink("ChecklistTestManager?testid=" + test.getId() + "&action=deleteCheckItem&checkitemid=" + checkItem.getCheckitemid(), response));
			out.println("\">Löschen</a></td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");
		}

		out.println("<hr>");

		out.println("<h3>Neuer Checklisten-Eintrag <a href=\"#\" onclick=\"toggleVisibility('teststephelp'); return false;\">(?)</a></h2>");

		out.println("<div style=\"display:none;\" id=teststephelp><b>Hilfe:</b><br>");
		out.println("<p>Diese Art von Test erlaubt es den Studierenden eine Checkliste von zu prüfenden Punkten bereitzustellen.</p>");
		out.println("<p>Eintrag z. B.:<br>"+
				"<textarea cols=120 rows=1 name=testcode disabled>Der Aufruf von <pre>ghci -e \"reverse'' [3,1,7,6]\" reverse.hs</pre> gibt <pre>[6, 7, 1, 3]</pre> zurück.</textarea></p>");
		out.println("<p>Ausgabe bei Testanforderung z. B.:<br>" +
				"<p><strong>Bitte überprüfen Sie Ihre Lösung hinsichtlich der folgenden Punkte und haken alle erfolgreichen Tests an:</strong></p>" + 
				"<table class=\"border\">\n"
				+ "<tbody><tr>\n"
				+ "<th>Test</th>\n"
				+ "<th>OK?</th>\n"
				+ "<tr>\n"
				+ "<td><label for=\"checkitem0\">Befindet sich der Funktionsname auch im Funktionsrumpf?</label></td><td><input type=\"checkbox\" disabled name=\"checkitem0\" id=\"checkitem2\"></td>\n"
				+ "</tr>\n"
				+ "</tr>\n"
				+ "<tr>\n"
				+ "<td><label for=\"checkitem1\">Der Aufruf von <pre>ghci -e \"reverse' [3,1,7,6]\" reverse.hs</pre> gibt <pre>[6, 7, 1, 3]</pre> zurück.</label></td><td><input type=\"checkbox\" disabled name=\"checkitem1\" id=\"checkitem1\"></td>\n"
				+ "</tr>\n"
				+ "<tr>\n"
				+ "<td><label for=\"checkitem2\">Der Aufruf von <pre>ghci -e \"reverse'' [3,1,7,6]\" reverse.hs</pre> gibt <pre>[6, 7, 1, 3]</pre> zurück.</label></td><td><input type=\"checkbox\" disabled name=\"checkitem2\" id=\"checkitem2\"></td>\n"
				+ "</tr>\n"
				+ "<tr>\n"
				+ "<td colspan=\"2\"><input type=\"submit\" disabled value=\"Ergebnis meiner Überprüfung speichern\"></td>\n"
				+ "</tr>\n"
				+ "</tbody></table></p>");
		out.println("<p>Einfaches HTML ist erlaubt (z.B. \"&lt;pre&gt;\").</p>");
		out.println("</div>");

		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=testid value=\"" + test.getId() + "\">");
		out.println("<input type=hidden name=action value=addNewCheckItem>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Eintrag:</th>");
		out.println("<td><input type=text name=title maxlength=250 value=\"\" size=60 required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		out.println(Util.generateHTMLLink("TaskManager?action=editTask&taskid=" + test.getTask().getTaskid() + "&lecture=" + test.getTask().getTaskGroup().getLecture().getId(), response));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		template.printTemplateFooter();
	}
}
