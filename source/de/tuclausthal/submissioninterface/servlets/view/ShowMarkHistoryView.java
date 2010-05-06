/*
 * Copyright 2010 Sven Strickroth <email@cs-ware.de>
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
import java.util.LinkedList;
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
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Submission submission = (Submission) request.getAttribute("submission");
		List<PointHistory> data = (List<PointHistory>) request.getAttribute("data");

		template.printTemplateHeader("Aktivitätslog", submission);

		out.println("<h1>Aktivitätslog</h1>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Wer</th>");
		out.println("<th>Wann</th>");
		out.println("<th>Feld</th>");
		out.println("<th>Entfernt</th>");
		out.println("<th>Hinzugefügt</th>");
		out.println("</tr>");

		LinkedList<PointHistory> ph = new LinkedList<PointHistory>();
		for (PointHistory pointHistory : data) {
			if (ph.size() > 0) {
				if (!pointHistory.getDate().equals(ph.getLast().getDate())) {
					printRow(out, ph);
				}
			}
			ph.add(pointHistory);
		}
		printRow(out, ph);

		out.println("</table>");
		template.printTemplateFooter();
	}

	private void printRow(PrintWriter out, LinkedList<PointHistory> ph) {
		boolean isFirst = true;
		for (PointHistory entry : ph) {
			out.println("<tr>");
			if (isFirst) {
				out.println("<td valign=top rowspan=" + ph.size() + "><a href=\"mailto:" + Util.mknohtml(entry.getWho().getUser().getFullEmail()) + "\">" + Util.mknohtml(entry.getWho().getUser().getFullName()) + "</a></td>");
				out.println("<td valign=top rowspan=" + ph.size() + ">" + Util.mknohtml(entry.getDate().toLocaleString()) + "</td>");
				isFirst = false;
			}
			out.println("<td valign=top>" + Util.mknohtml(entry.getField()) + "</td>");
			out.println("<td valign=top>" + Util.mknohtml(entry.getRemoved()).replace("\n", "<br>") + "</td>");
			out.println("<td valign=top>" + Util.mknohtml(entry.getAdded()).replace("\n", "<br>") + "</td>");
			out.println("</tr>");
		}
		ph.clear();
	}
}
