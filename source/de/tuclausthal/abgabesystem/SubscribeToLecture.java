package de.tuclausthal.abgabesystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.LectureDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.impl.LectureDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.ParticipationDAO;
import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;
import de.tuclausthal.abgabesystem.util.Util;

public class SubscribeToLecture extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();

		LectureDAOIf lectureDAO = DAOFactory.LectureDAOIf();

		if (request.getParameter("lecture") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf().getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			if (lecture == null) {
				mainbetternamereq.template().printTemplateHeader("Veranstaltung nicht gefunden");
				out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
				mainbetternamereq.template().printTemplateFooter();
				return;
			}

			ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
			Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), lecture);
			if (participation != null || lecture.getSemester() < Util.getCurrentSemester()) {
				mainbetternamereq.template().printTemplateHeader("Ungültiger Aufruf");
				mainbetternamereq.template().printTemplateFooter();
				return;
			} else {
				participationDAO.createParticipation(mainbetternamereq.getUser(), lecture, ParticipationRole.NORMAL);
				response.sendRedirect(response.encodeRedirectURL("/ba/servlets/ShowLecture?lecture=" + lecture.getId()));
				return;
			}
		} else {
			mainbetternamereq.template().printTemplateHeader("Veranstaltungen");

			Iterator<Lecture> lectureIterator = lectureDAO.getCurrentLucturesWithoutUser((User) request.getAttribute("user")).iterator();
			if (lectureIterator.hasNext()) {
				out.println("<table class=border>");
				out.println("<tr>");
				out.println("<th>Veranstaltung</th>");
				out.println("<th>Semester</th>");
				out.println("</tr>");
				while (lectureIterator.hasNext()) {
					Lecture lecture = lectureIterator.next();
					out.println("<tr>");
					out.println("<td>" + Util.mknohtml(lecture.getName()) + "</td>");
					out.println("<td><a href=\"" + response.encodeURL("?lecture=" + lecture.getId()) + "\">anmelden</a></td>");
					out.println("</tr>");
				}
				out.println("</table><p>");
			} else {
				out.println("<div class=mid>keine Veranstaltungen gefunden.</div>");
			}

			mainbetternamereq.template().printTemplateFooter();
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
