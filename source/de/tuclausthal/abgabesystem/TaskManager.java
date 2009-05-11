package de.tuclausthal.abgabesystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.TaskDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.util.Util;

public class TaskManager extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();

		Lecture lecture = DAOFactory.LectureDAOIf().getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			mainbetternamereq.template().printTemplateHeader("Veranstaltung nicht gefunden");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur �bersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), lecture);
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			mainbetternamereq.template().printTemplateHeader("insufficient rights");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if (request.getParameter("action") != null && ((request.getParameter("action").equals("editTask") && request.getParameter("taskid") != null) || (request.getParameter("action").equals("newTask") && request.getParameter("lecture") != null))) {
			boolean editTask = request.getParameter("action").equals("editTask");
			Task task;
			if (editTask == true) {
				TaskDAOIf taskDAO = DAOFactory.TaskDAOIf();
				task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
				if (task == null) {
					mainbetternamereq.template().printTemplateHeader("Aufgabe nicht gefunden");
					out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur �bersicht</a></div>");
					mainbetternamereq.template().printTemplateFooter();
					return;
				}
				mainbetternamereq.template().printTemplateHeader("Aufgabe bearbeiten");
			} else {
				// temp. Task for code-reuse
				task = new Task();
				task.setStart(new Date());
				task.setDeadline(new Date(new Date().getTime() + 3600 * 24 * 7 * 1000));
				task.setShowPoints(task.getDeadline());
				mainbetternamereq.template().printTemplateHeader("neue Aufgabe");
			}

			out.println("<form action=\"" + response.encodeURL("?") + "\" method=post>");
			if (editTask) {
				out.println("<input type=hidden name=action value=saveTask>");
				out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
			} else {
				out.println("<input type=hidden name=action value=saveNewTask>");
			}
			out.println("<input type=hidden name=lecture value=\"" + lecture.getId() + "\">");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Titel:</th>");
			out.println("<td><input type=text name=title value=\"" + Util.mknohtml(task.getTitle()) + "\"></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Beschreibung:</th>");
			out.println("<td><textarea cols=60 rows=10 name=description>" + Util.mknohtml(task.getDescription()) + "</textarea></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Startdatum:</th>");
			out.println("<td><input type=text name=startdate value=\"" + Util.mknohtml(task.getStart().toLocaleString()) + "\"> (dd.MM.yyyy oder dd.MM.yyyy HH:mm:ss)</td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Enddatum:</th>");
			out.println("<td><input type=text name=deadline value=\"" + Util.mknohtml(task.getDeadline().toLocaleString()) + "\"> (dd.MM.yyyy oder dd.MM.yyyy HH:mm:ss)</td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Punktedatum:</th>");
			out.println("<td><input type=text name=pointsdate value=\"" + Util.mknohtml(task.getShowPoints().toLocaleString()) + "\"> (dd.MM.yyyy oder dd.MM.yyyy HH:mm:ss)</td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Max. Punkte:</th>");
			out.println("<td><input type=text name=maxpoints value=\"" + task.getMaxPoints() + "\"> <b>TODO: bereits vergebene Pkts. pr�fen!</b></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
			if (editTask) {
				out.println(response.encodeURL("ShowTask?taskid=" + task.getTaskid()));
			} else {
				out.println(response.encodeURL("ShowLecture?lecture=" + lecture.getId()));
			}
			out.println("\">Abbrechen</a></td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");
			if (editTask) {
				// TODO: bissl mehr info ;)
				if (task.getTest() == null) {
					out.println("<p class=mid><a href=\"" + response.encodeURL("TestManager?action=newTest&amp;taskid=" + task.getTaskid()) + "\">Test hinzuf�gen</a></p>");
				} else {
					//out.println("<p class=mid><a href=\"" + response.encodeURL("?") + "\">Test bearbeiten</a></p>");
					out.println("<p class=mid><a href=\"" + response.encodeURL("TestManager?action=deleteTest&amp;taskid=" + task.getTaskid()) + "\">Test l�schen</a></p>");
				}
			}
		} else if (request.getParameter("action") != null && (request.getParameter("action").equals("saveNewTask") || request.getParameter("action").equals("saveTask"))) {
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf();
			Task task;
			if (request.getParameter("action").equals("saveTask")) {
				task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
				if (task == null) {
					mainbetternamereq.template().printTemplateHeader("Aufgabe nicht gefunden");
					out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur �bersicht</a></div>");
					mainbetternamereq.template().printTemplateFooter();
					return;
				}
				task.setMaxPoints(Util.parseInteger(request.getParameter("maxpoints"), 0));
				task.setTitle(request.getParameter("title"));
				task.setDescription(request.getParameter("description"));
				try {
					// TODO: Datum richtig parsen, am besten mit Fkt. da �fters ben�tigt
					task.setStart(format.parse(request.getParameter("startdate")));
					task.setDeadline(format.parse(request.getParameter("deadline")));
					task.setShowPoints(format.parse(request.getParameter("pointsdate")));
				} catch (Exception e) {
					// ignore
				}
				taskDAO.saveTask(task);
			} else {
				// TODO: Datum richtig parsen, am besten mit Fkt. da �fters ben�tigt
				Date startdate = new Date();
				Date deadline = new Date();
				Date showPoints = new Date();
				try {
					startdate = format.parse(request.getParameter("startdate"));
					deadline = format.parse(request.getParameter("deadline"));
					showPoints = format.parse(request.getParameter("pointsdate"));
				} catch (Exception e) {
					// ignore
				}
				// more checks
				task = taskDAO.newTask(request.getParameter("title"), Util.parseInteger(request.getParameter("maxpoints"), 0), startdate, deadline, request.getParameter("description"), lecture, showPoints);
			}
			// do a redirect, so that refreshing the page in a browser doesn't create duplicates
			response.sendRedirect(response.encodeRedirectURL("ShowTask?taskid=" + task.getTaskid()));
			return;
		} else if (request.getParameter("action") != null && request.getParameter("action").equals("deleteTask")) {
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf();
			Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
			if (task == null) {
				mainbetternamereq.template().printTemplateHeader("Aufgabe nicht gefunden");
				out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur �bersicht</a></div>");
			} else {
				taskDAO.deleteTask(task);
			}
			response.sendRedirect(response.encodeRedirectURL("ShowLecture?lecture=" + lecture.getId()));
			return;
		} else {
			mainbetternamereq.template().printTemplateHeader("Ung�ltiger Aufruf");
		}

		mainbetternamereq.template().printTemplateFooter();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
