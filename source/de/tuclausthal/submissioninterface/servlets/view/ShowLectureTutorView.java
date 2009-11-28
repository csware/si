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

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a lecture in tutor/advisor view
 * @author Sven Strickroth
 */
public class ShowLectureTutorView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Participation participation = (Participation) request.getAttribute("participation");
		Lecture lecture = participation.getLecture();
		SessionAdapter sessionAdapter = new SessionAdapter(request);
		Session session = HibernateSessionHelper.getSessionFactory().openSession();
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);

		// list all tasks for a lecture
		template.printTemplateHeader(lecture);

		// todo: wenn keine abrufbaren tasks da sind, nichts anzeigen
		Iterator<Task> taskIterator = lecture.getTasks().iterator();
		if (taskIterator.hasNext()) {
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Aufgabe</th>");
			out.println("<th>Max. Punkte</th>");
			out.println("</tr>");
			while (taskIterator.hasNext()) {
				Task task = taskIterator.next();
				if (task.getStart().before(Util.correctTimezone(new Date())) || participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
					out.println("<tr>");
					out.println("<td><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">" + Util.mknohtml(task.getTitle()) + "</a></td>");
					out.println("<td class=points>" + task.getMaxPoints() + "</td>");
					out.println("</tr>");
				}
			}
			out.println("</table>");
		} else {
			out.println("<div class=mid>keine Aufgaben gefunden.</div>");
		}
		if (participation.getRoleType() == ParticipationRole.ADVISOR) {
			out.println("<p><div class=mid><a href=\"" + response.encodeURL("TaskManager?lecture=" + lecture.getId() + "&amp;action=newTask") + "\">Neue Aufgabe</a></div>");
		}

		out.println("<p>");
		out.println("<h2>Teilnehmer</h2>");
		boolean isAdvisor = (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0);
		if (participationDAO.getParticipationsWithoutGroup(lecture).size() > 0) {
			out.println("<h3>Ohne Gruppe</h3>");
			listMembers(participationDAO.getParticipationsWithoutGroup(lecture).iterator(), response, isAdvisor, sessionAdapter.getUser(session));
			if (participation.getRoleType() == ParticipationRole.ADVISOR) {
				out.println("<p class=mid><a href=\"" + response.encodeURL("AddGroup?lecture=" + lecture.getId()) + "\">Neue Gruppe erstellen</a></p>");
			}
		}
		for (Group group : lecture.getGroups()) {
			out.println("<h3>Gruppe: " + Util.mknohtml(group.getName()) + "</h3>");
			if (participationDAO.getParticipationsWithoutGroup(lecture).size() > 0) {
				out.println("<p class=mid><a href=\"" + response.encodeURL("EditGroup?groupid=" + group.getGid()) + "\">Teilnehmer zuordnen</a></p>");
			}
			listMembers(participationDAO.getParticipationsOfGroup(group).iterator(), response, isAdvisor, sessionAdapter.getUser(session));
		}
		out.println("<h3>Gesamtdurchschnitt: " + DAOFactory.LectureDAOIf(session).getAveragePoints(lecture) + "</h3>");
		out.println("<p><div class=mid><a href=\"ShowLecture?lecture=" + lecture.getId() + "&show=list\">Gesamtliste</a></div>");
		template.printTemplateFooter();
	}

	public void listMembers(Iterator<Participation> participationIterator, HttpServletResponse response, boolean isAdvisor, User currentUser) throws IOException {
		if (participationIterator.hasNext()) {
			PrintWriter out = response.getWriter();
			int sumOfPoints = 0;
			int usersCount = 0;
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>MatNo</th>");
			out.println("<th>Teilnehmer</th>");
			out.println("<th>Rolle</th>");
			out.println("<th>Punkte</th>");
			out.println("</tr>");
			while (participationIterator.hasNext()) {
				Participation thisParticipation = participationIterator.next();
				out.println("<tr>");
				if (thisParticipation.getUser() instanceof Student) {
					out.println("<td>" + ((Student) thisParticipation.getUser()).getMatrikelno() + "</td>");
				} else {
					out.println("<td>n/a</td>");
				}
				out.println("<td><a href=\"mailto:" + Util.mknohtml(thisParticipation.getUser().getEmail()) + "@tu-clausthal.de\">" + Util.mknohtml(thisParticipation.getUser().getFullName()) + "</a></td>");
				if (thisParticipation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
					out.println("<td>" + Util.mknohtml(thisParticipation.getRoleType().toString()));
					if (isAdvisor) {
						out.println(" (<a href=\"" + response.encodeURL("EditParticipation?lectureid=" + thisParticipation.getLecture().getId() + "&amp;participationid=" + thisParticipation.getId()) + "&amp;type=tutor\">+</a>)");
					}
					if (thisParticipation.getGroup() != null) {
						out.println(" <a href=\"" + response.encodeURL("EditGroup?groupid=" + thisParticipation.getGroup().getGid() + "&amp;participationid=" + thisParticipation.getId()) + "&amp;action=removeFromGroup\"><img src=\"" + getServletContext().getContextPath() + "/log-out.png\"width=16 height=16 border=0 alt=\"aus Gruppe entfernen\" title=\"aus Gruppe entfernen\"></a>");
					}
				} else if (thisParticipation.getRoleType().compareTo(ParticipationRole.TUTOR) == 0) {
					out.println("<td>" + Util.mknohtml(thisParticipation.getRoleType().toString()));
					if (isAdvisor) {
						out.println(" (<a href=\"" + response.encodeURL("EditParticipation?lectureid=" + thisParticipation.getLecture().getId() + "&amp;participationid=" + thisParticipation.getId()) + "&amp;type=normal\">-</a>)");
					}
					if (thisParticipation.getGroup() != null && thisParticipation.getUser() != currentUser) {
						out.println(" <a href=\"" + response.encodeURL("EditGroup?groupid=" + thisParticipation.getGroup().getGid() + "&amp;participationid=" + thisParticipation.getId()) + "&amp;action=removeFromGroup\"><img src=\"" + getServletContext().getContextPath() + "/log-out.png\"width=16 height=16 border=0 alt=\"aus Gruppe entfernen\" title=\"aus Gruppe entfernen\"></a>");
					}
				} else {
					out.println("<td>" + Util.mknohtml(thisParticipation.getRoleType().toString()));
					if (isAdvisor && thisParticipation.getGroup() != null) {
						out.println(" <a href=\"" + response.encodeURL("EditGroup?groupid=" + thisParticipation.getGroup().getGid() + "&amp;participationid=" + thisParticipation.getId()) + "&amp;action=removeFromGroup\"><img src=\"" + getServletContext().getContextPath() + "/log-out.png\"width=16 height=16 border=0 alt=\"aus Gruppe entfernen\" title=\"aus Gruppe entfernen\"></a>");
					}
				}
				out.println("</td>");
				int points = getAllPoints(thisParticipation);
				// only count real users. Tutoren and advisors do not have submissions
				if (thisParticipation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
					usersCount++;
					sumOfPoints += points;
				}
				out.println("<td class=points>" + points + "</td>");

				out.println("</tr>");
			}
			if (sumOfPoints > 0 && usersCount > 0) {
				out.println("<tr>");
				out.println("<td colspan=3>Durchschnittspunkte:</td>");
				out.println("<td class=points>" + Float.valueOf(sumOfPoints / (float) usersCount).intValue() + "</td>");
				out.println("</tr>");
			}
			out.println("</table><p>");
		}
	}

	public int getAllPoints(Participation participation) {
		int points = 0;
		for (Submission submission : participation.getSubmissions()) {
			if (submission.getPoints() != null && submission.getPoints().getPointsOk()) {
				points += submission.getPoints().getPoints();
			}
		}
		return points;
	}
}
