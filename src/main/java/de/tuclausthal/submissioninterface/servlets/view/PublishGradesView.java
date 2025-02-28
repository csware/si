/*
 * Copyright 2011-2012, 2020-2021, 2024-2025 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a task in tutor view
 * @author Sven Strickroth
 */
@GATEView
public class PublishGradesView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Task task = (Task) request.getAttribute("task");
		template.addKeepAlive();
		template.printTemplateHeader("Punkte freischalten", task);
		PrintWriter out = response.getWriter();
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		int unbewertet = 0;
		int nichtAbgenommen = 0;
		for (Submission submission : task.getSubmissions()) {
			if (submission.getPoints() == null || submission.getPoints().getPointStatus() == PointStatus.NICHT_BEWERTET.ordinal()) {
				unbewertet++;
			} else if (submission.getPoints().getPointStatus() == PointStatus.NICHT_ABGENOMMEN.ordinal()) {
				nichtAbgenommen++;
			}
		}
		if (unbewertet > 0) {
			out.println("<p class=red>" + unbewertet + " unbewertete Abgaben.</p>");
		}
		if (nichtAbgenommen > 0) {
			out.println("<p class=red>" + nichtAbgenommen + " nicht abgenommene Abgaben.</p>");
		}
		out.println("<p><input type=checkbox id=mail name=mail> <label for=mail>Hinweismail an Studierende senden</label></p>");
		out.println("<input type=submit value= \"Punkte freischalten\"></p></form>");

		template.printTemplateFooter();
	}
}
