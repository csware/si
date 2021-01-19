/*
 * Copyright 2009-2013, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a lecture in tutor/advisor view
 * @author Sven Strickroth
 */
public class ShowLectureTutorView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);
		template.addJQuery();

		Participation participation = (Participation) request.getAttribute("participation");
		Lecture lecture = participation.getLecture();
		RequestAdapter requestAdapter = new RequestAdapter(request);
		Session session = requestAdapter.getSession();
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);

		boolean isAdvisor = (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0);
		boolean showMatNo = isAdvisor || Configuration.getInstance().isMatrikelNoAvailableToTutors(); 

		// list all tasks for a lecture
		template.printTemplateHeader(lecture);
		PrintWriter out = response.getWriter();

		Iterator<TaskGroup> taskGroupIterator = lecture.getTaskGroups().iterator();
		if (taskGroupIterator.hasNext()) {
			boolean isStartedTable = false;
			while (taskGroupIterator.hasNext()) {
				TaskGroup taskGroup = taskGroupIterator.next();
				Iterator<Task> taskIterator = taskGroup.getTasks().iterator();
				if (taskIterator.hasNext() || isAdvisor) {
					if (!isStartedTable) {
						isStartedTable = true;
						out.println("<table class=border>");
						out.println("<tr>");
						out.println("<th>Aufgabe</th>");
						out.println("<th>Max. Punkte</th>");
						out.println("</tr>");
					}
					out.println("<tr>");
					String editLink = "";
					if (isAdvisor) {
						editLink = " (<a href=\"" + Util.generateHTMLLink("TaskManager?lecture=" + lecture.getId() + "&action=editTaskGroup&taskgroupid=" + taskGroup.getTaskGroupId(), response) + "\">edit</a>)";
					}
					out.println("<th colspan=2>Aufgabengruppe " + Util.escapeHTML(taskGroup.getTitle()) + editLink + "</th>");
					out.println("</tr>");
					while (taskIterator.hasNext()) {
						Task task = taskIterator.next();
						if (task.getStart().before(Util.correctTimezone(new Date())) || participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
							out.println("<tr>");
							out.println("<td><a href=\"" +Util.generateHTMLLink("ShowTask?taskid=" + task.getTaskid(), response) + "\">" + Util.escapeHTML(task.getTitle()) + "</a></td>");
							out.println("<td class=points>" + Util.showPoints(task.getMaxPoints()) + "</td>");
							out.println("</tr>");
						}
					}
				}
			}
			if (isStartedTable) {
				out.println("</table>");
			} else {
				out.println("<div class=mid>keine Aufgaben gefunden.</div>");
			}
		} else {
			out.println("<div class=mid>keine Aufgaben gefunden.</div>");
		}
		if (participation.getRoleType() == ParticipationRole.ADVISOR) {
			out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink("TaskManager?lecture=" + lecture.getId() + "&action=newTaskGroup", response) + "\">Neue Aufgabengruppe</a></div>");
			if (!lecture.getTaskGroups().isEmpty()) {
				out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink("TaskManager?lecture=" + lecture.getId() + "&action=newTask", response) + "\">Neue Aufgabe</a></div>");
			}
		}

		if (requestAdapter.isPrivacyMode()) {
			template.printTemplateFooter();
			return;
		}
		int[] studentsPoints = new int[2];
		Map<Integer, Integer> allPoints = DAOFactory.PointsDAOIf(session).getAllPointsForLecture(lecture);
		out.println("<p>");
		out.println("<h2>Teilnehmende</h2>");
		if (!participationDAO.getParticipationsWithoutGroup(lecture).isEmpty()) {
			out.println("<h3>Ohne Gruppe</h3>");
			listMembers(participationDAO.getParticipationsWithoutGroup(lecture).iterator(), response, isAdvisor, showMatNo, requestAdapter, studentsPoints, allPoints);
			out.println("<p class=mid>");
			if (participation.getRoleType() == ParticipationRole.ADVISOR) {
				out.println("<a href=\"" + Util.generateHTMLLink("AddGroup?lecture=" + lecture.getId(), response) + "\">Neue Gruppe erstellen</a>");
				if (!lecture.getGroups().isEmpty()) {
					out.println("<br><a href=\"" + Util.generateHTMLLink("EditMultipleGroups?lecture=" + lecture.getId(), response) + "\">Mehrere Gruppen auf einmal bearbeiten</a>");
				}
				out.println("<br>");
			}
		} else {
			out.println("<p class=mid>");
		}
		out.println("<a href=\"" + Util.generateHTMLLink("MassMail?lectureid=" + lecture.getId(), response) + "\">Mail an alle</a>");
		out.println("</p>");
		for (Group group : lecture.getGroups()) {
			out.println("<h3><a name=\"group" + group.getGid() + "\">Gruppe: " + Util.escapeHTML(group.getName()) + "</a> <a href=\"#\" onclick=\"$('#contentgroup" + group.getGid() + "').toggle(); return false;\">(+/-)</a></h3>");
			String defaultState = "";
			if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) != 0 && !group.getTutors().isEmpty() && !group.getTutors().contains(participation)) {
				defaultState = "style=\"display: none;\"";
			}
			out.println("<div " + defaultState + " id=\"contentgroup" + group.getGid() + "\">");
			if (!participationDAO.getParticipationsWithoutGroup(lecture).isEmpty()) {
				out.println("<p class=mid>");
				out.println("<a href=\"" + Util.generateHTMLLink("EditGroup?groupid=" + group.getGid(), response) + "\">Teilnehmer zuordnen / Gruppe bearbeiten</a>");
				out.println("<br><a href=\"" + Util.generateHTMLLink("MassMail?groupid=" + group.getGid(), response) + "\">Mail an Gruppe</a>");
				out.println("</p>");
			}
			if (!group.getTutors().isEmpty()) {
				out.println("<table class=border>");
				out.println("<tr>");
				if (group.getTutors().size() > 1) {
					out.println("<th>TutorInnen</th>");
				} else {
					out.println("<th>TutorIn</th>");
				}
				out.println("</tr>");
				for (Participation tutorParticipation : group.getTutors()) {
					out.println("<tr>");
					out.println("<td><a href=\"mailto:" + Util.escapeHTML(tutorParticipation.getUser().getEmail()) + "\">" + Util.escapeHTML(tutorParticipation.getUser().getFullName()) + "</a>");
					if (isAdvisor) {
						out.println(" <a onclick=\"return confirmLink('Tutor-Gruppen-Zugehöhrigkeit entfernen?')\" href=\"" + Util.generateHTMLLink("EditGroup?groupid=" + group.getGid() + "&participationid=" + tutorParticipation.getId() + "&action=removeTutorFromGroup", response) + "\"><img src=\"" + getServletContext().getContextPath() + "/log-out.svg\" width=16 height=16 border=0 alt=\"Tutor-Gruppen-Zugehöhrigkeit entfernen\" title=\"Tutor-Gruppen-Zugehöhrigkeit entfernen\"></a>");
					}
					out.println("</td>");
					out.println("</tr>");
				}
				out.println("</table><p>");
			}
			listMembers(participationDAO.getParticipationsOfGroup(group).iterator(), response, isAdvisor, showMatNo, requestAdapter, studentsPoints, allPoints);
			out.println("</div>");
		}
		out.println("<h3>Gesamtdurchschnitt: " + Util.showPoints(((Double) (studentsPoints[1] / (double) studentsPoints[0])).intValue()) + "</h3>");
		out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink("ShowLecture?lecture=" + lecture.getId() + "&show=list", response) + "\">Gesamtliste</a> - <a href=\"" + Util.generateHTMLLink("ShowLecture?lecture=" + lecture.getId() + "&show=csv", response) + "\">CSV-Download</a></div>");
		template.printTemplateFooter();
	}

	public void listMembers(Iterator<Participation> participationIterator, HttpServletResponse response, boolean isAdvisor, boolean showMatNo, RequestAdapter requestAdapter, int[] studentsPoints, Map<Integer, Integer> allPoints) throws IOException {
		if (participationIterator.hasNext()) {
			PrintWriter out = response.getWriter();
			int sumOfPoints = 0;
			int usersCount = 0;
			out.println("<table class=border>");
			out.println("<tr>");
			if (showMatNo) {
				out.println("<th>MatNo</th>");
			}
			out.println("<th>TeilnehmerIn</th>");
			out.println("<th>Rolle</th>");
			out.println("<th>Punkte</th>");
			out.println("</tr>");
			while (participationIterator.hasNext()) {
				Participation thisParticipation = participationIterator.next();
				out.println("<tr>");
				if (showMatNo) {
					if (thisParticipation.getUser() instanceof Student) {
						out.println("<td>" + ((Student) thisParticipation.getUser()).getMatrikelno() + "</td>");
					} else {
						out.println("<td>n/a</td>");
					}
				}
				out.println("<td><a href=\"" + Util.generateHTMLLink("ShowUser?uid=" + thisParticipation.getUser().getUid(), response) + "\">" + Util.escapeHTML(thisParticipation.getUser().getFullName()) + "</a></td>");
				if (thisParticipation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
					out.println("<td>" + Util.escapeHTML(thisParticipation.getRoleType().toString()));
					if (isAdvisor) {
						out.println(" (<a onclick=\"return confirmLink('Wirklich promoten?')\" href=\"" + Util.generateHTMLLink("EditParticipation?lectureid=" + thisParticipation.getLecture().getId() + "&participationid=" + thisParticipation.getId() + "&type=tutor", response) + "\">+</a>)");
					}
					if (thisParticipation.getGroup() != null) {
						out.println(" <a onclick=\"return confirmLink('Wirklich aus der Gruppe entfernen?')\" href=\"" + Util.generateHTMLLink("EditGroup?groupid=" + thisParticipation.getGroup().getGid() + "&participationid=" + thisParticipation.getId() + "&action=removeFromGroup", response) + "\"><img src=\"" + getServletContext().getContextPath() + "/log-out.svg\" width=16 height=16 border=0 alt=\"aus Gruppe entfernen\" title=\"aus Gruppe entfernen\"></a>");
					}
				} else if (thisParticipation.getRoleType().compareTo(ParticipationRole.TUTOR) == 0) {
					out.println("<td>" + Util.escapeHTML(thisParticipation.getRoleType().toString()));
					if (isAdvisor) {
						out.println(" (<a onclick=\"return confirmLink('Wirklich degradieren?')\" href=\"" + Util.generateHTMLLink("EditParticipation?lectureid=" + thisParticipation.getLecture().getId() + "&participationid=" + thisParticipation.getId() + "&type=normal", response) + "\">-</a>)");
					}
					if (thisParticipation.getGroup() != null && thisParticipation.getUser() != requestAdapter.getUser()) {
						out.println(" <a onclick=\"return confirmLink('Wirklich aus der Gruppe entfernen?')\" href=\"" + Util.generateHTMLLink("EditGroup?groupid=" + thisParticipation.getGroup().getGid() + "&participationid=" + thisParticipation.getId() + "&action=removeFromGroup", response) + "\"><img src=\"" + getServletContext().getContextPath() + "/log-out.svg\" width=16 height=16 border=0 alt=\"aus Gruppe entfernen\" title=\"aus Gruppe entfernen\"></a>");
					}
				} else {
					out.println("<td>" + Util.escapeHTML(thisParticipation.getRoleType().toString()));
					if (isAdvisor && thisParticipation.getGroup() != null) {
						out.println(" <a onclick=\"return confirmLink('Wirklich aus der Gruppe entfernen?')\" href=\"" + Util.generateHTMLLink("EditGroup?groupid=" + thisParticipation.getGroup().getGid() + "&participationid=" + thisParticipation.getId() + "&action=removeFromGroup", response) + "\"><img src=\"" + getServletContext().getContextPath() + "/log-out.svg\" width=16 height=16 border=0 alt=\"aus Gruppe entfernen\" title=\"aus Gruppe entfernen\"></a>");
					}
				}
				out.println("</td>");
				int points = 0;
				if (allPoints.containsKey(thisParticipation.getId())) {
					points = allPoints.get(thisParticipation.getId());
				}
				// only count real users. Tutoren and advisors do not have submissions
				if (thisParticipation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
					usersCount++;
					sumOfPoints += points;
				}
				out.println("<td class=points>" + Util.showPoints(points) + "</td>");

				out.println("</tr>");
			}
			if (sumOfPoints > 0 && usersCount > 0) {
				out.println("<tr>");
				out.println("<td colspan=" + (2 + (showMatNo ? 1 : 0)) + ">Anzahl: " + usersCount + " / Durchschnittspunkte:</td>");
				out.println("<td class=points>" + Util.showPoints(Float.valueOf(sumOfPoints / (float) usersCount).intValue()) + "</td>");
				out.println("</tr>");
			} else {
				out.println("<tr>");
				out.println("<td colspan=" + (2 + (showMatNo ? 1 : 0)) + ">Anzahl:</td>");
				out.println("<td class=points>" + usersCount + "</td>");
				out.println("</tr>");
			}
			studentsPoints[0] += usersCount;
			studentsPoints[1] += sumOfPoints;
			out.println("</table><p>");
		}
	}
}
