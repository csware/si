/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a lecture in student view
 * @author Sven Strickroth
 */
public class ShowLectureStudentView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Participation participation = (Participation) request.getAttribute("participation");
		Lecture lecture = participation.getLecture();
		SessionAdapter sessionAdapter = new SessionAdapter(request);

		// list all tasks for a lecture
		template.printTemplateHeader(lecture);

		// todo: wenn keine abrufbaren tasks da sind, nichts anzeigen
		Iterator<Task> taskIterator = lecture.getTasks().iterator();
		if (taskIterator.hasNext()) {
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Aufgabe</th>");
			out.println("<th>Max. Punkte</th>");
			out.println("<th>Meine Punkte</th>");
			out.println("</tr>");
			while (taskIterator.hasNext()) {
				Task task = taskIterator.next();
				if (task.getStart().before(Util.correctTimezone(new Date())) || participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
					out.println("<tr>");
					out.println("<td><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">" + Util.mknohtml(task.getTitle()) + "</a></td>");
					out.println("<td class=points>" + task.getMaxPoints() + "</td>");
					Submission submission = DAOFactory.SubmissionDAOIf(HibernateSessionHelper.getSessionFactory().openSession()).getSubmission(task, sessionAdapter.getUser(HibernateSessionHelper.getSession()));
					if (submission != null && submission.getPoints() != null && submission.getTask().getShowPoints().before(Util.correctTimezone(new Date()))) {
						out.println("<td class=points>" + submission.getPoints().getPoints() + "</td>");
					} else {
						out.println("<td class=points>n/a</td>");
					}
					out.println("</tr>");
				}
			}
			out.println("</table>");
		} else {
			out.println("<div class=mid>keine Aufgaben gefunden.</div>");
		}
		template.printTemplateFooter();
	}
}
