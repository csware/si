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
import de.tuclausthal.abgabesystem.persistence.dao.PointsDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.Similarity;
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
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur �bersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		Task task = submission.getTask();

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), submission.getTask().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			mainbetternamereq.template().printTemplateHeader("Ung�ltige Anfrage");
			out.println("<div class=mid>Sie sind kein Tutor dieser Veranstaltung.</div>");
			out.println("<div class=mid><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">zur �bersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		mainbetternamereq.template().printTemplateHeader("Abgabe von \"" + Util.mknohtml(submission.getSubmitter().getUser().getFullName()) + "\"");

		if (submission.getSubmitter().getGroup() != null) {
			out.println("<h2>Gruppe: " + submission.getSubmitter().getGroup().getName() + "</h2>");
		}

		if (task.getDeadline().before(new Date())) {
			if (request.getParameter("points") != null) {
				PointsDAOIf pointsDAO = DAOFactory.PointsDAOIf();
				pointsDAO.createPoints(Util.parseInteger(request.getParameter("points"), 0), submission, participation);
				response.sendRedirect(response.encodeRedirectURL("ShowSubmission?sid=" + submission.getSubmissionid()));
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
		}

		if (submission.getSimilarSubmissions().size() > 0) {
			out.println("<h2>�hnliche Abgaben:</h2>");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Student</th>");
			out.println("<th>�hnlichkeit</th>");
			out.println("</tr>");
			Iterator<Similarity> similarityIterator = submission.getSimilarSubmissions().iterator();
			while (similarityIterator.hasNext()) {
				Similarity similarity = similarityIterator.next();
				out.println("<tr>");
				out.println("<td><a href=\"" + response.encodeURL("ShowSubmission?sid=" + similarity.getSubmissionTwo().getSubmissionid()) + "\">" + Util.mknohtml(similarity.getSubmissionTwo().getSubmitter().getUser().getFullName()) + "</a></td>");
				out.println("<td class=points>" + similarity.getPercentage() + "</td>");
				out.println("</tr>");
			}
			out.println("</table><p>");
		}

		if (task.getTest() != null && submission.getTestResult() != null) {
			out.println("<h2>Test:</h2>");
			out.println("<div class=mid>Erfolgreich: " + Util.boolToHTML(submission.getTestResult().getPassedTest()));
			out.println("<p><textarea cols=80 rows=15>" + Util.mknohtml(submission.getTestResult().getTestOutput()) + "</textarea></div>");
		}

		if (submission.getStderr() != null && !submission.getStderr().isEmpty()) {
			out.println("<h2>Standard-Error Ausgabe:</h2>");
			out.println("<p class=mid><textarea cols=80 rows=15>" + Util.mknohtml(submission.getStderr()) + "</textarea></p>");
		}
		out.println("<h2>Dateien:</h2>");
		out.println("<div class=mid>");
		File path = new File("c:/abgabesystem/" + task.getLecture().getId() + "/" + task.getTaskid() + "/" + submission.getSubmissionid() + "/");
		for (File file : path.listFiles()) {
			if (file.getName().endsWith(".java")) {
				out.println("<iframe width=800 height=250 src=\"" + response.encodeURL("ShowFile/" + file.getName() + "?sid=" + submission.getSubmissionid()) + "\"></iframe><p>");
			}
		}
		out.println("</div>");
		mainbetternamereq.template().printTemplateFooter();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
