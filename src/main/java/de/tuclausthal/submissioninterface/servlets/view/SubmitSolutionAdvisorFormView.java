/*
 * Copyright 2009-2012, 2020-2022, 2025 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for the submission of files
 * @author Sven Strickroth
 */
@GATEView
public class SubmitSolutionAdvisorFormView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Task task = (Task) request.getAttribute("task");
		@SuppressWarnings("unchecked")
		List<Participation> participants = (List<Participation>) request.getAttribute("participants");

		template.printTemplateHeader("Abgabe für Studierenden starten", task);
		PrintWriter out = response.getWriter();

		StringBuffer setWithUser = new StringBuffer();
		setWithUser.append("<p>Abgabe erstellen für: <select name=uploadFor size=1 required=required>");
		for (Participation part : participants) {
			if (part.getRoleType().equals(ParticipationRole.NORMAL)) {
				setWithUser.append("<option value=" + part.getId() + ">" + Util.escapeHTML(part.getUser().getLastNameFirstName()) + "</option>");
			}
		}
		setWithUser.append("</select><p>");

		if (task.getMaxSubmitters() > 1) {
			out.println("<p>PartnerInnen können im zweiten Schritt bei der Abgabe eingestellt werden.</p>");
		}
		out.println("<FORM class=mid ENCTYPE=\"multipart/form-data\" method=POST action=\"" + Util.generateHTMLLink("?taskid=" + task.getTaskid(), response) + "\">");
		out.println(setWithUser.toString());
		out.println("<p>Bitte wählen Sie eine oder mehrere Dateien aus, die Sie einsenden möchten.</p>");
		out.println("<INPUT TYPE=file NAME=file required multiple>");
		out.println("<INPUT TYPE=submit VALUE=upload>");
		out.println("</FORM>");

		template.printTemplateFooter();
	}
}
