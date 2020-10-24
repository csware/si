/*
 * Copyright 2010-2012, 2017, 2020 Sven Strickroth <email@cs-ware.de>
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.PointHistory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying the startpage of the system
 * @author Sven Strickroth
 */
public class ShowMarkHistoryView extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Submission submission = (Submission) request.getAttribute("submission");
		@SuppressWarnings("unchecked")
		List<PointHistory> data = (List<PointHistory>) request.getAttribute("data");

		template.printTemplateHeader("Aktivitätslog", submission);
		PrintWriter out = response.getWriter();

		out.println("<h1>Aktivitätslog</h1>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Wer</th>");
		out.println("<th>Wann</th>");
		out.println("<th>Feld</th>");
		out.println("<th>Entfernt</th>");
		out.println("<th>Hinzugefügt</th>");
		out.println("</tr>");

		List<PointHistory> ph = new ArrayList<>();
		for (PointHistory pointHistory : data) {
			if (ph.size() > 0) {
				if (!pointHistory.getDate().equals(ph.get(ph.size() - 1).getDate())) {
					printRow(out, ph);
				}
			}
			ph.add(pointHistory);
		}
		printRow(out, ph);

		out.println("</table>");
		template.printTemplateFooter();
	}

	private void printRow(PrintWriter out, List<PointHistory> ph) {
		boolean isFirst = true;
		for (PointHistory entry : ph) {
			out.println("<tr>");
			if (isFirst) {
				out.println("<td valign=top rowspan=" + ph.size() + "><a href=\"mailto:" + Util.escapeHTML(entry.getWho().getUser().getEmail()) + "\">" + Util.escapeHTML(entry.getWho().getUser().getFullName()) + "</a></td>");
				out.println("<td valign=top rowspan=" + ph.size() + ">" + Util.escapeHTML(dateFormatter.format(entry.getDate())) + "</td>");
				isFirst = false;
			}
			out.println("<td valign=top>" + Util.escapeHTML(entry.getField()) + "</td>");
			out.println("<td valign=top>" + Util.textToHTML(entry.getRemoved()) + "</td>");
			out.println("<td valign=top>" + Util.textToHTML(entry.getAdded()) + "</td>");
			out.println("</tr>");
		}
		ph.clear();
	}
}
