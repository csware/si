package de.tuclausthal.abgabesystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Group;
import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.util.Util;

public class ShowLecture extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();

		Lecture lecture = DAOFactory.LectureDAOIf().getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			mainbetternamereq.template().printTemplateHeader("Veranstaltung nicht gefunden");
			out.println("<div class=mid><a href=\"" + response.encodeURL("Overview") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), lecture);
		if (participation == null) {
			mainbetternamereq.template().printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid>Sie sind kein Teilnehmer dieser Veranstaltung.</div>");
			out.println("<div class=mid><a href=\"" + response.encodeURL("Overview") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		// list all tasks for a lecture
		mainbetternamereq.template().printTemplateHeader("Aufgaben der Veranstaltung \"" + Util.mknohtml(lecture.getName()) + "\"");

		// todo: wenn keine abrufbaren tasks da sind, nichts anzeigen
		Iterator<Task> taskIterator = lecture.getTasks().iterator();
		if (taskIterator.hasNext()) {
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Aufgabe</th>");
			out.println("<th>Max. Punkte</th>");
			if (participation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
				out.println("<th>Meine Punkte</th>");
			}
			out.println("</tr>");
			while (taskIterator.hasNext()) {
				Task task = taskIterator.next();
				if (task.getStart().before(new Date()) || participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
					out.println("<tr>");
					out.println("<td><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">" + Util.mknohtml(task.getTitle()) + "</a></td>");
					out.println("<td class=points>" + task.getMaxPoints() + "</td>");
					if (participation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
						Submission submission = DAOFactory.SubmissionDAOIf().getSubmission(task, mainbetternamereq.getUser());
						if (submission != null && submission.getPoints() != null && submission.getTask().getShowPoints().after(new Date())) {
							out.println("<td class=points>" + submission.getPoints().getPoints() + "</td>");
						} else {
							out.println("<td class=points>n/a</td>");
						}
					}
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

		if (participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
			out.println("<p>");
			out.println("<h2>Teilnehmer</h2>");
			boolean isAdvisor = (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) == 0);
			if (participationDAO.getParticipationsWithoutGroup(lecture).size() > 0) {
				out.println("<h3>Ohne Gruppe</h3>");
				listMembers(participationDAO.getParticipationsWithoutGroup(lecture).iterator(), response, isAdvisor);
				if (participation.getRoleType() == ParticipationRole.ADVISOR) {
					out.println("<p class=mid><a href=\"" + response.encodeURL("AddGroup?lecture=" + lecture.getId()) + "\">Neue Gruppe erstellen</a></p>");
				}
			}
			for (Group group : lecture.getGroups()) {
				out.println("<h3>Gruppe: " + Util.mknohtml(group.getName()) + "</h3>");
				if (participationDAO.getParticipationsWithoutGroup(lecture).size() > 0) {
					out.println("<p class=mid><a href=\"" + response.encodeURL("EditGroup?groupid=" + group.getGid()) + "\">Teilnehmer zuordnen</a></p>");
				}
				listMembers(group.getMembers().iterator(), response, isAdvisor);
			}
		}

		mainbetternamereq.template().printTemplateFooter();
	}

	public void listMembers(Iterator<Participation> participationIterator, HttpServletResponse response, boolean isAdvisor) throws IOException {
		if (participationIterator.hasNext()) {
			PrintWriter out = response.getWriter();
			int sumOfPoints = 0;
			int usersCount = 0;
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Teilnehmer</th>");
			out.println("<th>Rolle</th>");
			out.println("<th>Punkte</th>");
			out.println("</tr>");
			while (participationIterator.hasNext()) {
				Participation thisParticipation = participationIterator.next();
				out.println("<tr>");
				out.println("<td><a href=\"mailto:" + Util.mknohtml(thisParticipation.getUser().getEmail()) + "\">" + Util.mknohtml(thisParticipation.getUser().getFullName()) + "</a></td>");
				if (thisParticipation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
					out.println("<td>" + Util.mknohtml(thisParticipation.getRoleType().toString()));
					if (isAdvisor) {
						out.println(" (<a href=\"" + response.encodeURL("EditParticipation?lectureid=" + thisParticipation.getLecture().getId() + "&amp;participationid=" + thisParticipation.getId()) + "&amp;type=tutor\">+</a>)");
					}
					if (thisParticipation.getGroup() != null) {
						out.println(" (<a href=\"" + response.encodeURL("EditGroup?groupid=" + thisParticipation.getGroup().getGid() + "&amp;participationid=" + thisParticipation.getId()) + "&amp;action=removeFromGroup\">--</a>)");
					}
				} else if (thisParticipation.getRoleType().compareTo(ParticipationRole.TUTOR) == 0) {
					out.println("<td>" + Util.mknohtml(thisParticipation.getRoleType().toString()));
					if (isAdvisor) {
						out.println(" (<a href=\"" + response.encodeURL("EditParticipation?lectureid=" + thisParticipation.getLecture().getId() + "&amp;participationid=" + thisParticipation.getId()) + "&amp;type=normal\">-</a>)");
					}
					if (thisParticipation.getGroup() != null && thisParticipation.getUser() != MainBetterNameHereRequired.getUser()) {
						out.println(" (<a href=\"" + response.encodeURL("EditGroup?groupid=" + thisParticipation.getGroup().getGid() + "&amp;participationid=" + thisParticipation.getId()) + "&amp;action=removeFromGroup\">--</a>)");
					}
				} else {
					out.println("<td>" + Util.mknohtml(thisParticipation.getRoleType().toString()));
					if (isAdvisor && thisParticipation.getGroup() != null) {
						out.println(" (<a href=\"" + response.encodeURL("EditGroup?groupid=" + thisParticipation.getGroup().getGid() + "&amp;participationid=" + thisParticipation.getId()) + "&amp;action=removeFromGroup\">--</a>)");
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
			if (sumOfPoints > 0) {
				out.println("<tr>");
				out.println("<td colspan=2>Durchschnittspunkte:</td>");
				out.println("<td class=points>" + Float.valueOf(sumOfPoints / (float) usersCount).intValue() + "</td>");
				out.println("</tr>");
			}
			out.println("</table><p>");
		}
	}

	public int getAllPoints(Participation participation) {
		int points = 0;
		for (Submission submission : participation.getSubmissions()) {
			if (submission.getPoints() != null) {
				points += submission.getPoints().getPoints();
			}
		}
		return points;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
