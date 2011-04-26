/*
 * Copyright 2010 - 2011 Sven Strickroth <email@cs-ware.de>
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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a lecture in tutor/advisor view
 * @author Sven Strickroth
 */
public class MarkEmptyTaskView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		List<Participation> participations = (List<Participation>) request.getAttribute("participations");
		Task task = (Task) request.getAttribute("task");

		template.printTemplateHeader("Punkte vergeben...", task);

		out.println("<form action=\"?taskid=" + task.getTaskid() + "\" method=post>");
		out.println("<b>Student:</b> <select size=1 name=pid>");
		for (Participation participation : participations) {
			if (participation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
				out.println("<option value=\"" + participation.getId() + "\">" + Util.escapeHTML(participation.getUser().getFullName()) + "</option>");
			}
		}
		out.println("</select><br>");
		out.println("<b>Punkte:</b> <input type=text name=points size=3 value=\"\"> (max. " + Util.showPoints(task.getMaxPoints()) + ")<br>");
		out.println("<b>Öffentlicher Kommentar:</b><br><textarea cols=80 rows=8 name=publiccomment></textarea><br>");
		out.println("<b>Interner Kommentar:</b><br><textarea cols=80 rows=8 name=internalcomment></textarea><br>");
		out.println("<b>Abgenommen:</b> <input type=checkbox name=pointsok checked><br>");
		out.println("<input type=submit value=Speichern>");
		out.println("</form>");

		template.printTemplateFooter();
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
