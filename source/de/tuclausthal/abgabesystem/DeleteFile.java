package de.tuclausthal.abgabesystem;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.util.CheckSubmission;
import de.tuclausthal.abgabesystem.util.Util;

public class DeleteFile extends HttpServlet {
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
		Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), task.getLecture());
		if (participation == null) {
			mainbetternamereq.template().printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid>Sie sind kein Teilnehmer dieser Veranstaltung.</div>");
			out.println("<div class=mid><a href=\"" + response.encodeURL("Overview") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if (task.getDeadline().before(new Date())) {
			mainbetternamereq.template().printTemplateHeader("Abgabe nicht (mehr) möglich");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if (request.getPathInfo() == null) {
			mainbetternamereq.template().printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		ContextAdapter contextAdapter = new ContextAdapter(getServletContext());
		File path = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
		for (File file : path.listFiles()) {
			if (file.getName().equals(request.getPathInfo().substring(1))) {
				file.delete();

				CheckSubmission checkSubmission = new CheckSubmission(submission, contextAdapter.getDataPath());
				checkSubmission.compileTest(response);
				checkSubmission.checkTest(response);

				response.sendRedirect(response.encodeRedirectURL("ShowTask?taskid=" + task.getTaskid()));
				return;
			}
		}

		// delete submission w/o file

		mainbetternamereq.template().printTemplateHeader("File not found");
		out.println("<div class=mid><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">zur Übersicht</a></div>");
		mainbetternamereq.template().printTemplateFooter();
		return;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
