package de.tuclausthal.abgabesystem;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;
import de.tuclausthal.abgabesystem.util.Util;

public class Overview extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();
		MainBetterNameHereRequired.template().printTemplateHeader("Meine Veranstaltungen");

		User user = (User) request.getAttribute("user");

		if (user.getLectureParticipant().size() > 0) {
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Veranstaltung</th>");
			out.println("<th>Semester</th>");
			out.println("</tr>");
			for (Participation participation : user.getLectureParticipant()) {
				out.println("<tr>");
				out.println("<td><a href=\"" + response.encodeURL("ShowLecture?lecture=" + participation.getLecture().getId()) + "\">" + Util.mknohtml(participation.getLecture().getName()) + "</a></td>");
				out.println("<td>" + participation.getLecture().getReadableSemester() + "</td>");
				out.println("</tr>");
			}
			out.println("</table><p>");
		}
		out.println("<div class=mid><a href=\"" + response.encodeURL("SubscribeToLecture") + "\">In eine Veranstaltung eintragen...</a></div>");

		MainBetterNameHereRequired.template().printTemplateFooter();
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
