package de.tuclausthal.abgabesystem;

import java.io.File;
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
import de.tuclausthal.abgabesystem.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.TaskDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.util.Util;

public class ShowTask extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf();
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			mainbetternamereq.template().printTemplateHeader("Aufgabe nicht gefunden");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), task.getLecture());
		if (participation == null) {
			mainbetternamereq.template().printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid>Sie sind kein Teilnehmer dieser Veranstaltung.</div>");
			out.println("<div class=mid><a href=\"" + response.encodeURL("/ba/servlets/Overview") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if (task.getStart().after(new Date()) && participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			mainbetternamereq.template().printTemplateHeader("Aufgabe nicht abrufbar");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		mainbetternamereq.template().printTemplateHeader("Aufgabe \"" + Util.mknohtml(task.getTitle()) + "\"");

		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		// HTML must be possible here
		out.println("<td>" + task.getDescription() + "&nbsp;</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Startdatum:</th>");
		out.println("<td>" + Util.mknohtml(task.getStart().toLocaleString()) + "</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Enddatum:</th>");
		out.println("<td>" + Util.mknohtml(task.getDeadline().toLocaleString()));
		if (task.getDeadline().before(new Date())) {
			out.println(" Keine Abgabe mehr möglich");
		}
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Punkte:</th>");
		out.println("<td>" + task.getMaxPoints() + "</td>");
		out.println("</tr>");
		out.println("</table>");

		if (participation.getRoleType() == ParticipationRole.ADVISOR) {
			out.println("<p><div class=mid><a href=\"" + response.encodeURL("/ba/servlets/TaskManager?lecture=" + task.getLecture().getId() + "&amp;taskid=" + task.getTaskid() + "&amp;action=editTask") + "\">Aufgabe bearbeiten</a></div>");
		}
		if (participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
			if (task.getSubmissions() != null && task.getSubmissions().size() > 0) {
				out.println("<p><h2>Abgaben</h2><p>");
				// TODO: nach Gruppen sortiert
				out.println("<table class=border>");
				out.println("<tr>");
				out.println("<th>Benutzer</th>");
				out.println("<th>Kompiliert</th>");
				if (task.getTest() != null) {
					out.println("<th>Test</th>");
				}
				out.println("<th>Punkte</th>");
				out.println("</tr>");
				Iterator<Submission> submissionIterator = task.getSubmissions().iterator();
				while (submissionIterator.hasNext()) {
					Submission submission = submissionIterator.next();
					out.println("<tr>");
					out.println("<td><a href=\"" + response.encodeURL("/ba/servlets/ShowSubmission?sid=" + submission.getSubmissionid()) + "\">" + Util.mknohtml(submission.getSubmitter().getUser().getFullName()) + "</a></td>");
					String compiles = "<span class=green>ja</span>";
					if (submission.getCompiles() == false) {
						compiles = "<span class=red>nein</span>";
					}
					out.println("<td>" + compiles + "</td>");
					if (task.getTest() != null) {
						String testOk = "<span class=green>ja</span>";
						if (submission.getTestResult() == null || submission.getTestResult().getPassedTest() == false) {
							testOk = "<span class=red>nein</span>";
						}
						out.println("<td>" + testOk + "</td>");
					}
					if (submission.getPoints() != null) {
						out.println("<td align=right>" + submission.getPoints().getPoints() + "</td>");
					} else {
						out.println("<td>n/a</td>");
					}
					out.println("</tr>");
				}

				out.println("</table>");
			}
		} else {
			SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf();
			Submission submission = submissionDAO.getSubmission(task, mainbetternamereq.getUser());
			if (submission != null) {
				out.println("<p><h2>Informationen zu meiner Abgabe:</h2>");
				out.println("<table class=border>");
				out.println("<tr>");
				out.println("<th>Kompiliert:</th>");
				out.println("<td>" + submission.getCompiles() + "</td>");
				out.println("</tr>");
				// TODO: öffentlicher Test
				out.println("<tr>");
				out.println("<th>Besteht aus:</th>");
				out.println("<td>");
				File path = new File("c:/abgabesystem/" + task.getLecture().getId() + "/" + task.getTaskid() + "/" + submission.getSubmissionid() + "/");
				for (File file : path.listFiles()) {
					if (file.getName().endsWith(".java")) {
						out.println(file.getName() + "<br>");
					}
				}
				out.println("</td>");
				out.println("</tr>");
				if (task.getDeadline().after(new Date()) && submission.getPoints() != null) {
					out.println("<tr>");
					out.println("<th>Bewertung:</td>");
					out.println("<td>");
					out.println(submission.getPoints().getPoints() + " von " + task.getMaxPoints());
					out.println("</td>");
					out.println("</tr>");
				}
				out.println("</table>");
			}
			out.println("<p>");
			if (task.getDeadline().before(new Date())) {
				out.println("Keine Abgabe mehr möglich");
			} else {
				out.println("<div class=mid><a href=\"" + response.encodeURL("/ba/servlets/SubmitSolution?taskid=" + task.getTaskid()) + "\">Abgabe starten</a></div");
			}
		}

		mainbetternamereq.template().printTemplateFooter();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
