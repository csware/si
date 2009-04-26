package de.tuclausthal.abgabesystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.GroupDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.impl.GroupDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.LectureDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.ParticipationDAO;
import de.tuclausthal.abgabesystem.persistence.datamodel.Group;
import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.util.Util;

public class ShowLecture extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();

		Lecture lecture = DAOFactory.LectureDAOIf().getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			mainbetternamereq.template().printTemplateHeader("Veranstaltung nicht gefunden");
			out.println("<div class=mid><a href=\"" + response.encodeURL("/ba/servlets/Overview") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), lecture);
		if (participation == null) {
			mainbetternamereq.template().printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid>Sie sind kein Teilnehmer dieser Veranstaltung.</div>");
			out.println("<div class=mid><a href=\"" + response.encodeURL("/ba/servlets/Overview") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		// list all tasks for a lecture
		mainbetternamereq.template().printTemplateHeader("Aufgaben der Veranstaltung \"" + Util.mknohtml(lecture.getName()) + "\"");

		Iterator<Task> taskIterator = lecture.getTasks().iterator();
		if (taskIterator.hasNext()) {
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Aufgabe</th>");
			out.println("<th>Max. Punkte</th>");
			out.println("</tr>");
			while (taskIterator.hasNext()) {
				Task task = taskIterator.next();
				out.println("<tr>");
				out.println("<td><a href=\"" + response.encodeURL("/ba/servlets/ShowTask?taskid=" + task.getTaskid()) + "\">" + Util.mknohtml(task.getTitle()) + "</a></td>");
				out.println("<td>" + task.getMaxPoints() + "</td>");
				out.println("</tr>");
			}
			out.println("</table>");
		} else {
			out.println("<div class=mid>keine Aufgaben gefunden.</div>");
		}
		if (participation.getRoleType() == ParticipationRole.ADVISOR) {
			out.println("<p><div class=mid><a href=\"" + response.encodeURL("/ba/servlets/TaskManager?lecture=" + lecture.getId() + "&action=newTask") + "\">Neue Aufgabe</a></div>");
		}

		if (participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
			out.println("<p>");
			out.println("<h2>Teilnehmer</h2>");
			boolean isAdvisor = (participation.getRoleType().compareTo(ParticipationRole.TUTOR) == 0);
			if (participationDAO.getParticipationsWithoutGroup(lecture).size() > 0) {
				out.println("<h3>Ohne Gruppe</h3>");
				listMembers(participationDAO.getParticipationsWithoutGroup(lecture).iterator(), response, isAdvisor);
				if (participation.getRoleType() == ParticipationRole.ADVISOR) {
					out.println("<p><div class=mid><a href=\"" + response.encodeURL("/ba/servlets/AddGroup?lecture=" + lecture.getId()) + "\">Neue Gruppe erstellen</a></div>");
				}
			}
			for (Group group : lecture.getGroups()) {
				out.println("<h3>Gruppe: " + Util.mknohtml(group.getName()) + "</h3>");
				if (participationDAO.getParticipationsWithoutGroup(lecture).size() > 0) {
					out.println("<div class=mid><a href=\"" + response.encodeURL("/ba/servlets/EditGroup?groupid=" + group.getGid()) + "\">Teilnehmer zuordnen</a></div>");
				}
				listMembers(group.getMembers().iterator(), response, isAdvisor);
			}
		}

		mainbetternamereq.template().printTemplateFooter();
	}

	public void listMembers(Iterator<Participation> participationIterator, HttpServletResponse response, boolean isAdvisor) throws IOException {
		if (participationIterator.hasNext()) {
			PrintWriter out = response.getWriter();
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Teilnehmer</th>");
			out.println("<th>Rolle</th>");
			out.println("</tr>");
			while (participationIterator.hasNext()) {
				Participation thisParticipation = participationIterator.next();
				out.println("<tr>");
				out.println("<td><a href=\"mailto:" + Util.mknohtml(thisParticipation.getUser().getEmail()) + "\">" + Util.mknohtml(thisParticipation.getUser().getFullName()) + "</a></td>");
				if (thisParticipation.getRoleType().compareTo(ParticipationRole.NORMAL) == 0) {
					out.println("<td>" + Util.mknohtml(thisParticipation.getRoleType().toString()));
					if (isAdvisor) {
						out.println(" (<a href=\"" + response.encodeURL("/ba/servlets/EditParticipation?lectureid=" + thisParticipation.getLecture().getId() + "&participationid=" + thisParticipation.getId()) + "&type=tutor\">+</a>)");
					}
					out.println("</td>");
				} else if (thisParticipation.getRoleType().compareTo(ParticipationRole.TUTOR) == 0) {
					out.println("<td>" + Util.mknohtml(thisParticipation.getRoleType().toString()));
					if (isAdvisor) {
						out.println(" (<a href=\"" + response.encodeURL("/ba/servlets/EditParticipation?lectureid=" + thisParticipation.getLecture().getId() + "&participationid=" + thisParticipation.getId()) + "&type=normal\">-</a>)");
					}
					out.println("</td>");
				} else {
					out.println("<td>" + Util.mknohtml(thisParticipation.getRoleType().toString()) + "</td>");
				}
				out.println("</tr>");
			}
			out.println("</table><p>");
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
