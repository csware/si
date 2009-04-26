package de.tuclausthal.abgabesystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.GroupDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.TaskDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.impl.GroupDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.LectureDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.ParticipationDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.TaskDAO;
import de.tuclausthal.abgabesystem.persistence.datamodel.Group;
import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.util.Util;

public class AddGroup extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();

		Lecture lecture = DAOFactory.LectureDAOIf().getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			mainbetternamereq.template().printTemplateHeader("Veranstaltung nicht gefunden");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
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

		if (request.getParameter("action") != null && request.getParameter("action").equals("saveNewGroup") && request.getParameter("name") != null) {
			GroupDAOIf groupDAO = DAOFactory.GroupDAOIf();
			Group group = groupDAO.createGroup(lecture, request.getParameter("name"));
			response.sendRedirect(response.encodeRedirectURL("/ba/servlets/EditGroup?groupid=" + group.getGid()));
			return;
		} else {
			out.println("<form action=\"" + response.encodeURL("?") + "\" method=post>");
			out.println("<input type=hidden name=action value=saveNewGroup>");
			out.println("<input type=hidden name=lecture value=\"" + lecture.getId() + "\">");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Name:</th>");
			out.println("<td><input type=text name=name></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
			out.println(response.encodeURL("/ba/servlets/ShowLecture?lecture=" + lecture.getId()));
			out.println("\">Abbrechen</a></td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");
		}

		mainbetternamereq.template().printTemplateFooter();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
