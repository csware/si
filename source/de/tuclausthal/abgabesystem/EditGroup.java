package de.tuclausthal.abgabesystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.GroupDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Group;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.util.Util;

public class EditGroup extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();

		GroupDAOIf groupDAO = DAOFactory.GroupDAOIf();
		Group group = groupDAO.getGroup(Util.parseInteger(request.getParameter("groupid"), 0));
		if (group == null) {
			mainbetternamereq.template().printTemplateHeader("Gruppe nicht gefunden");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), group.getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			mainbetternamereq.template().printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid>Sie sind kein Teilnehmer dieser Veranstaltung.</div>");
			out.println("<div class=mid><a href=\"" + response.encodeURL("/ba/servlets/Overview") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if (request.getParameterValues("members") != null && request.getParameterValues("members").length > 0) {
			for (String newMember : request.getParameterValues("members")) {
				Participation memberParticipation = participationDAO.getParticipation(Util.parseInteger(newMember, 0));
				if (memberParticipation != null) {
					memberParticipation.setGroup(group);
					participationDAO.saveParticipation(memberParticipation);
				}
			}
			response.sendRedirect(response.encodeRedirectURL("/ba/servlets/ShowLecture?lecture=" + group.getLecture().getId()));
			return;
		}

		mainbetternamereq.template().printTemplateHeader("Gruppe \"" + Util.mknohtml(group.getName()) + "\"");

		out.println("<form action=\"" + response.encodeURL("?") + "\" method=post>");
		out.println("<input type=hidden name=action value=assignGroup>");
		out.println("<input type=hidden name=groupid value=" + group.getGid() + ">");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Gruppe:</th>");
		out.println("<td><input type=text name=title value=\"" + Util.mknohtml(group.getName()) + "\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Punkte:</th>");
		out.println("<td><select multiple name=members>");
		Iterator<Participation> participationIterator = participationDAO.getParticipationsWithoutGroup(group.getLecture()).iterator();
		while (participationIterator.hasNext()) {
			Participation thisParticipation = participationIterator.next();
			out.println("<option value=" + thisParticipation.getId() + ">" + Util.mknohtml(thisParticipation.getUser().getFullName()) + "</option>");
		}
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=zuordnen> <a href=\"" + response.encodeURL("/ba/servlets/ShowLecture?lecture=" + group.getLecture().getId()));
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		mainbetternamereq.template().printTemplateFooter();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
