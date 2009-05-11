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

import com.uwyn.jhighlight.renderer.Renderer;
import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.util.Util;

public class ShowFile extends HttpServlet {
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
			out.println("<div class=mid><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if (request.getPathInfo() == null) {
			mainbetternamereq.template().printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		File path = new File("c:/abgabesystem/" + task.getLecture().getId() + "/" + task.getTaskid() + "/" + submission.getSubmissionid() + "/");
		for (File file : path.listFiles()) {
			if (file.getName().equals(request.getPathInfo().substring(1))) {
				//response.setContentType("text/plain");
				response.setContentType("text/html");
				BufferedReader freader = new BufferedReader(new FileReader(file));
				String line;
				String code = "";
				while ((line = freader.readLine()) != null) {
					code = code + line + "\n";
				}
				freader.close();

				Renderer renderer = XhtmlRendererFactory.getRenderer(XhtmlRendererFactory.JAVA);
				out.write(renderer.highlight(file.getName(), code, "UTF-8", false));
				return;
			}
		}

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
