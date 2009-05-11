package de.tuclausthal.abgabesystem;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.util.Util;

public class EditParticipation extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(Util.parseInteger(request.getParameter("participationid"), 0));
		if (participation == null) {
			mainbetternamereq.template().printTemplateHeader("Teilnahme nicht gefunden");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}
		Participation callerParticipation = participationDAO.getParticipation(mainbetternamereq.getUser(), participation.getLecture());
		if (callerParticipation == null || callerParticipation.getRoleType() != ParticipationRole.ADVISOR) {
			mainbetternamereq.template().printTemplateHeader("insufficient rights");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if (request.getParameter("type") != null && request.getParameter("type").equals("advisor") && callerParticipation.getUser().isSuperUser()) {
			participationDAO.createParticipation(participation.getUser(), participation.getLecture(), ParticipationRole.ADVISOR);
		} else if (request.getParameter("type") != null && request.getParameter("type").equals("tutor")) {
			participationDAO.createParticipation(participation.getUser(), participation.getLecture(), ParticipationRole.TUTOR);
		} else {
			participationDAO.createParticipation(participation.getUser(), participation.getLecture(), ParticipationRole.NORMAL);
		}
		if ("admin".equals(request.getParameter("goback"))) {
			response.sendRedirect(response.encodeURL("AdminMenue?action=showLecture&lecture=" + callerParticipation.getLecture().getId()));
		} else {
			response.sendRedirect(response.encodeURL("ShowLecture?action=showLecture&lecture=" + callerParticipation.getLecture().getId()));
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
