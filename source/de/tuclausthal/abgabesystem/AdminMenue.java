package de.tuclausthal.abgabesystem;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.UserDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;
import de.tuclausthal.abgabesystem.util.Util;

public class AdminMenue extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();
		if (mainbetternamereq.getUser().isSuperUser() != true) {
			mainbetternamereq.template().printTemplateHeader("insufficient rights");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}
		if (request.getParameter("action") != null && request.getParameter("action").equals("newLecture")) {
			mainbetternamereq.template().printTemplateHeader("neue Veranstaltung");

			out.println("<form action=\"" + response.encodeURL("?action=saveLecture") + "\" method=post>");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Name der Veranstaltung:</th>");
			out.println("<td><input type=text name=name></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Semester:</th>");
			out.println("<td>aktuelles Semester</td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid><input type=submit value=anlegen> <a href=\"" + response.encodeURL("?") + "\">Abbrechen</a></td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");
		} else if (request.getParameter("action") != null && request.getParameter("action").equals("cleanup")) {
			File path = new File("c:/abgabesystem/");
			// list lectures
			for (File lectures : path.listFiles()) {
				if (DAOFactory.LectureDAOIf().getLecture(Util.parseInteger(lectures.getName(), 0)) == null) {
					Util.recursiveDelete(lectures);
				} else {
					// list all tasks
					for (File tasks : lectures.listFiles()) {
						if (!tasks.getName().equals("junittest.jar") && DAOFactory.TaskDAOIf().getTask(Util.parseInteger(tasks.getName(), 0)) == null) {
							Util.recursiveDelete(tasks);
						} else {
							// list all submissions
							for (File submissions : tasks.listFiles()) {
								if (DAOFactory.SubmissionDAOIf().getSubmission(Util.parseInteger(submissions.getName(), 0)) == null) {
									Util.recursiveDelete(submissions);
								}
							}
						}
					}
				}
			}
			response.sendRedirect(response.encodeRedirectURL(request.getRequestURL() + "?"));
			return;
		} else if (request.getParameter("action") != null && request.getParameter("action").equals("saveLecture") && request.getParameter("name") != null && !request.getParameter("name").trim().isEmpty()) {
			Lecture newLecture = DAOFactory.LectureDAOIf().newLecture(request.getParameter("name").trim());
			// do a redirect, so that refreshing the page in a browser doesn't create duplicates
			response.sendRedirect(response.encodeRedirectURL(request.getRequestURL() + "?action=showLecture&lecture=" + newLecture.getId()));
			return;
		} else if (request.getParameter("action") != null && request.getParameter("action").equals("showLecture") && request.getParameter("lecture") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf().getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			if (lecture == null) {
				mainbetternamereq.template().printTemplateHeader("Veranstaltung nicht gefunden");
				out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			} else {
				mainbetternamereq.template().printTemplateHeader("Veranstaltung \"" + Util.mknohtml(lecture.getName()) + "\" bearbeiten");

				out.println("<h2>Betreuer</h2>");
				Iterator<Participation> advisorIterator = lecture.getParticipants().iterator();
				out.println("<table class=border>");
				out.println("<tr>");
				out.println("<th>Benutzer</th>");
				out.println("<th>Entfernen</th>");
				out.println("</tr>");
				while (advisorIterator.hasNext()) {
					Participation participation = advisorIterator.next();
					if (participation.getRoleType() == ParticipationRole.ADVISOR) {
						User user = participation.getUser();
						out.println("<tr>");
						out.println("<td>" + user.getFullName() + "</td>");
						out.println("<td><a href=\"" + response.encodeURL("?action=removeUser&amp;lecture=" + lecture.getId() + "&amp;userid=" + user.getUid()) + "\">degradieren</a></td>");
						out.println("</tr>");
					}
				}
				out.println("<tr>");
				out.println("<td colspan=2>");
				printAddUserForm(out, lecture, "advisor");
				out.println("</td>");
				out.println("</tr>");
				out.println("</table><p>");

				out.println("<h2>Tutoren</h2>");
				Iterator<Participation> tutorIterator = lecture.getParticipants().iterator();
				out.println("<table class=border>");
				out.println("<tr>");
				out.println("<th>Benutzer</th>");
				out.println("<th>Entfernen</th>");
				out.println("</tr>");
				while (tutorIterator.hasNext()) {
					Participation participation = tutorIterator.next();
					if (participation.getRoleType() == ParticipationRole.TUTOR) {
						User user = participation.getUser();
						out.println("<tr>");
						out.println("<td>" + user.getFullName() + "</td>");
						out.println("<td><a href=\"" + response.encodeURL("?action=removeUser&amp;lecture=" + lecture.getId() + "&amp;userid=" + user.getUid()) + "\">degradieren</a></td>");
						out.println("</tr>");
					}
				}
				out.println("<tr>");
				out.println("<td colspan=2>");
				printAddUserForm(out, lecture, "tutor");
				out.println("</td>");
				out.println("</tr>");
				out.println("</table><p>");
				out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			}

		} else if (request.getParameter("action") != null && request.getParameter("action").equals("showAdminUsers")) {
			mainbetternamereq.template().printTemplateHeader("Super User");

			UserDAOIf userDAO = DAOFactory.UserDAOIf();

			Iterator<User> userIterator = userDAO.getSuperUsers().iterator();
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Benutzer</th>");
			out.println("<th>Entfernen</th>");
			out.println("</tr>");
			while (userIterator.hasNext()) {
				User user = userIterator.next();
				out.println("<tr>");
				out.println("<td>" + user.getFullName() + "</td>");
				out.println("<td><a href=\"" + response.encodeURL("?action=removeSuperUser&amp;userid=" + user.getUid()) + "\">degradieren</a></td>");
				out.println("</tr>");
			}
			out.println("<tr>");
			out.println("<td colspan=2>");
			userIterator = userDAO.getUsers().iterator();
			out.println("<form action=\"?\">");
			out.println("<input type=hidden name=action value=addSuperUser>");
			out.println("<select name=userid>");
			while (userIterator.hasNext()) {
				User user = userIterator.next();
				if (!user.isSuperUser()) {
					out.println("<option value=" + user.getUid() + ">" + user.getFullName());
				}
			}
			out.println("</select>");
			out.println("<input type=submit value=\"hinzufügen\">");
			out.println("</form>");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table><p>");

			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
		} else if (("addSuperUser".equals(request.getParameter("action")) || "removeSuperUser".equals(request.getParameter("action"))) && request.getParameter("userid") != null) {
			UserDAOIf userDAO = DAOFactory.UserDAOIf();
			User user = userDAO.getUser(Util.parseInteger(request.getParameter("userid"), 0));
			if (user != null) {
				user.setSuperUser("addSuperUser".equals(request.getParameter("action")));
				userDAO.saveUser(user);
			}
			response.sendRedirect(response.encodeURL(request.getRequestURL() + "?action=showAdminUsers"));
			return;
		} else if (request.getParameter("action") != null && (request.getParameter("action").equals("addUser") || request.getParameter("action").equals("removeUser")) && request.getParameter("lecture") != null && request.getParameter("userid") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf().getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			UserDAOIf userDAO = DAOFactory.UserDAOIf();
			User user = userDAO.getUser(Util.parseInteger(request.getParameter("userid"), 0));
			if (lecture == null || user == null) {
				mainbetternamereq.template().printTemplateHeader("Veranstaltung oder Benutzer nicht gefunden");
				out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			} else {
				// request.getParameter("type") != null
				ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
				if (request.getParameter("action").equals("addUser")) {
					if (request.getParameter("type").equals("advisor")) {
						participationDAO.createParticipation(user, lecture, ParticipationRole.ADVISOR);
					} else {
						participationDAO.createParticipation(user, lecture, ParticipationRole.TUTOR);
					}
				} else { // dregregate user
					participationDAO.createParticipation(user, lecture, ParticipationRole.NORMAL);
				}
				response.sendRedirect(response.encodeURL(request.getRequestURL() + "?action=showLecture&amp;lecture=" + lecture.getId()));
				return;
			}
		} else { // list all lectures
			mainbetternamereq.template().printTemplateHeader("Admin-Menü");

			Iterator<Lecture> lectureIterator = DAOFactory.LectureDAOIf().getLectures().iterator();
			if (lectureIterator.hasNext()) {
				out.println("<table class=border>");
				out.println("<tr>");
				out.println("<th>Veranstaltung</th>");
				out.println("<th>Semester</th>");
				out.println("</tr>");
				while (lectureIterator.hasNext()) {
					Lecture lecture = lectureIterator.next();
					out.println("<tr>");
					out.println("<td><a href=\"" + response.encodeURL(request.getRequestURL() + "?action=showLecture&amp;lecture=" + lecture.getId()) + "\">" + Util.mknohtml(lecture.getName()) + "</a></td>");
					out.println("<td>" + lecture.getReadableSemester() + "</td>");
					out.println("</tr>");
				}
				out.println("</table>");
			}
			out.println("<p class=mid><a href=\"" + response.encodeURL("?action=newLecture") + "\">Neue Veranstaltung</a></p>");
			out.println("<p class=mid><a href=\"" + response.encodeURL("?action=showAdminUsers") + "\">Super User anzeigen</a></p>");
			out.println("<p class=mid><a href=\"" + response.encodeURL("?action=cleanup") + "\">Verzeichnis Cleanup</a></p>");
		}

		mainbetternamereq.template().printTemplateFooter();
	}

	public void printAddUserForm(PrintWriter out, Lecture lecture, String type) {
		Iterator<Participation> iterator = lecture.getParticipants().iterator();
		out.println("<form action=\"?\">");
		out.println("<input type=hidden name=action value=addUser>");
		out.println("<input type=hidden name=type value=" + type + ">");
		out.println("<input type=hidden name=lecture value=" + lecture.getId() + ">");
		out.println("<select name=userid>");
		while (iterator.hasNext()) {
			Participation participation = iterator.next();
			if (participation.getRoleType() == ParticipationRole.NORMAL) {
				User user = participation.getUser();
				out.println("<option value=" + user.getUid() + ">" + user.getFullName());
			}
		}
		out.println("</select>");
		out.println("<input type=submit value=\"hinzufügen\">");
		out.println("</form>");
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
