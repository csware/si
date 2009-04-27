package de.tuclausthal.abgabesystem;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.TaskDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.TestDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.RegExpTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.persistence.datamodel.Test;
import de.tuclausthal.abgabesystem.util.Util;

public class TestManager extends HttpServlet {
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

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), task.getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			mainbetternamereq.template().printTemplateHeader("insufficient rights");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if ("newTest".equals(request.getParameter("action"))) {
			mainbetternamereq.template().printTemplateHeader("Test erstellen");
			out.println("<form action=\"" + response.encodeURL("?") + "\" method=post>");
			out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
			out.println("<input type=hidden name=action value=saveNewTest>");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Main-Klasse:</th>");
			out.println("<td><input type=text name=mainclass></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>CommandLine Parameter:</th>");
			out.println("<td><input type=text name=parameter></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Reg.Exp.:</th>");
			out.println("<td><input type=text name=regexp></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
			out.println(response.encodeURL("/ba/servlets/ShowTask?taskid=" + task.getTaskid()));
			out.println("\">Abbrechen</a></td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");
		} else if ("saveNewTest".equals(request.getParameter("action"))) {
			TestDAOIf testDAO = DAOFactory.TestDAOIf();
			RegExpTest test = testDAO.createRegExpTest(task);
			test.setMainClass(request.getParameter("mainclass"));
			test.setCommandLineParameter(request.getParameter("parameter"));
			test.setRegularExpression(request.getParameter("regexp"));
			testDAO.saveTest(test);
			// Race cond?
			task.setTest(test);
			taskDAO.saveTask(task);
			response.sendRedirect(response.encodeRedirectURL("/ba/servlets/TaskManager?action=editTask&lecture=" + task.getLecture().getId() + "&taskid=" + task.getTaskid()));
		} else if ("deleteTest".equals(request.getParameter("action"))) {
			TestDAOIf testDAO = DAOFactory.TestDAOIf();
			Test test = task.getTest();
			task.setTest(null);
			taskDAO.saveTask(task);
			testDAO.deleteTest(test);
			response.sendRedirect(response.encodeRedirectURL("/ba/servlets/TaskManager?action=editTask&lecture=" + task.getLecture().getId() + "&taskid=" + task.getTaskid()));
			return;
		} else {
			mainbetternamereq.template().printTemplateHeader("Ungültiger Aufruf");
		}

		mainbetternamereq.template().printTemplateFooter();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
