package de.tuclausthal.abgabesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.PointsDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.util.Util;

public class ShowSubmission extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();

		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf();
		Submission submission = submissionDAO.getSubmission(Util.parseInteger(request.getParameter("sid"), 0));
		if (submission == null) {
			mainbetternamereq.template().printTemplateHeader("Abgabe nicht gefunden");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		Task task = submission.getTask();

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), submission.getTask().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			mainbetternamereq.template().printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid>Sie sind kein Tutor dieser Veranstaltung.</div>");
			out.println("<div class=mid><a href=\"" + response.encodeURL("/ba/servlets/ShowTask?taskid=" + task.getTaskid()) + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		mainbetternamereq.template().printTemplateHeader("Abgabe von \"" + Util.mknohtml(submission.getSubmitter().getUser().getFullName()) + "\"");

		if (request.getParameter("points") != null) {
			PointsDAOIf pointsDAO = DAOFactory.PointsDAOIf();
			pointsDAO.createPoints(Util.parseInteger(request.getParameter("points"), 0), submission, participation);
			response.sendRedirect(response.encodeRedirectURL("/ba/servlets/ShowSubmission?sid=" + submission.getSubmissionid()));
			return;
		}

		out.println("<h2>Bewertung:</h2>");
		out.println("<table class=border>");
		if (submission.getPoints() != null) {
			out.println("<tr>");
			out.println("<td>");
			out.println(submission.getPoints().getPoints() + "/" + task.getMaxPoints() + " Punkte vergeben ");
			out.println(" von " + submission.getPoints().getIssuedBy().getUser().getFullName());
			out.println("</td>");
			out.println("</tr>");
		}
		out.println("<tr>");
		out.println("<td>");
		out.println("<form action=\"?\" method=post>");
		out.println("<input type=hidden name=sid value=\"" + submission.getSubmissionid() + "\">");
		out.println("<input type=text name=points> (max. " + task.getMaxPoints() + ")");
		out.println(" <input type=submit>");
		out.println("</form>");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");

		out.println("<p>");

		if (submission.getStderr() != null) {
			out.println("<h2>STDErr:</h2>");
			out.println("<textarea cols=80 rows=15>" + Util.mknohtml(submission.getStderr()) + "</textarea>");
		}
		out.println("<h2>Dateien:</h2>");
		File path = new File("c:/abgabesystem/" + task.getLecture().getId() + "/" + task.getTaskid() + "/" + submission.getSubmissionid() + "/");
		for (File file : path.listFiles()) {
			if (file.getName().endsWith(".java")) {
				out.println(file.getName() + "<br>");
				String code = "";
				BufferedReader freader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = freader.readLine()) != null) {
					code = code.concat(line + "\n");
				}
				out.println("<textarea cols=80 rows=15>" + Util.mknohtml(code) + "</textarea><p>");
				freader.close();
			}
		}
		mainbetternamereq.template().printTemplateFooter();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
