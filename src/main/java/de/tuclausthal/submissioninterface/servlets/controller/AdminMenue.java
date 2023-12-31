/*
 * Copyright 2009-2012, 2020-2023 Sven Strickroth <email@cs-ware.de>
 *
 * This file is part of the GATE.
 *
 * GATE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * GATE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GATE. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.servlets.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.LectureDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.AdminMenueAddLectureView;
import de.tuclausthal.submissioninterface.servlets.view.AdminMenueEditLectureView;
import de.tuclausthal.submissioninterface.servlets.view.AdminMenueOverView;
import de.tuclausthal.submissioninterface.servlets.view.AdminMenueShowAdminUsersView;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for the Admin-Menue
 * @author Sven Strickroth
 *
 */
@GATEController
public class AdminMenue extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		if (!RequestAdapter.getUser(request).isSuperUser()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ("newLecture".equals(request.getParameter("action"))) {
			getServletContext().getNamedDispatcher(AdminMenueAddLectureView.class.getSimpleName()).forward(request, response);
		} else if ("showLecture".equals(request.getParameter("action")) && request.getParameter("lecture") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			request.setAttribute("lecture", lecture);
			request.setAttribute("participants", DAOFactory.ParticipationDAOIf(session).getLectureParticipationsOrderedByName(lecture));
			getServletContext().getNamedDispatcher(AdminMenueEditLectureView.class.getSimpleName()).forward(request, response);
		} else if ("showAdminUsers".equals(request.getParameter("action"))) {
			request.setAttribute("superusers", DAOFactory.UserDAOIf(session).getSuperUsers());
			getServletContext().getNamedDispatcher(AdminMenueShowAdminUsersView.class.getSimpleName()).forward(request, response);
		} else if ("cleanup".equals(request.getParameter("action"))) {
			Template template = TemplateFactory.getTemplate(request, response);
			template.printAdminMenueTemplateHeader("Verzeichnis Cleanup");
			PrintWriter out = response.getWriter();
			cleanupDataDirectory(session, Configuration.getInstance().getDataPath(), true, out);
			out.println("<hr>");
			out.println("<p>Während des Aufräumens sollten keine Änderungen an Veranstaltungen und Tasks vorgenommen werden.</p>");
			out.println("<form action=\"" + Util.generateHTMLLink("?action=cleanup", response) + "\" method=post>");
			out.println("<input type=submit value=\"Cleanup jetzt wirklich durchführen\"> <a href=\"" + Util.generateHTMLLink("?", response) + "\">Abbrechen</a>");
			out.println("</form>");
			template.printTemplateFooter();
		} else { // list all lectures
			request.setAttribute("lectures", DAOFactory.LectureDAOIf(session).getLectures());
			getServletContext().getNamedDispatcher(AdminMenueOverView.class.getSimpleName()).forward(request, response);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		if (!RequestAdapter.getUser(request).isSuperUser()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ("cleanup".equals(request.getParameter("action"))) {
			Template template = TemplateFactory.getTemplate(request, response);
			template.printAdminMenueTemplateHeader("Verzeichnis Cleanup");
			PrintWriter out = response.getWriter();
			cleanupDataDirectory(session, Configuration.getInstance().getDataPath(), false, out);
			template.printTemplateFooter();
		} else if ("saveLecture".equals(request.getParameter("action")) && request.getParameter("name") != null && !request.getParameter("name").trim().isEmpty()) {
			Transaction tx = session.beginTransaction();
			Lecture newLecture = DAOFactory.LectureDAOIf(session).newLecture(request.getParameter("name").trim(), request.getParameter("requiresAbhnahme") != null, request.getParameter("groupWise") != null);
			int wantedSemester = Util.parseInteger(request.getParameter("semester"), 0);
			if (wantedSemester != newLecture.getSemester() && wantedSemester >= Util.decreaseSemester(Util.getCurrentSemester()) && wantedSemester <= Util.increaseSemester(Util.getCurrentSemester())) {
				newLecture.setSemester(wantedSemester);
			}
			newLecture.setDescription(request.getParameter("description"));
			newLecture.setAllowSelfSubscribe(request.getParameter("allowselfsubscribe") != null);
			DAOFactory.ParticipationDAOIf(session).createParticipation(RequestAdapter.getUser(request), newLecture, ParticipationRole.ADVISOR);
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(AdminMenue.class.getSimpleName() + "?action=showLecture&lecture=" + newLecture.getId(), response));
		} else if ("editLecture".equals(request.getParameter("action"))) {
			Transaction tx = session.beginTransaction();
			Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			lecture.setName(request.getParameter("name"));
			lecture.setDescription(request.getParameter("description"));
			lecture.setAllowSelfSubscribe(request.getParameter("allowselfsubscribe") != null);
			if (request.getParameter("groupWise") != null) {
				lecture.setGradingMethod("groupWise");
			} else {
				lecture.setGradingMethod("taskWise");
			}
			lecture.setRequiresAbhnahme(request.getParameter("requiresAbhnahme") != null);
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(AdminMenue.class.getSimpleName(), response));
		} else if ("deleteLecture".equals(request.getParameter("action")) && request.getParameter("lecture") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			if (lecture != null) {
				Transaction tx = session.beginTransaction();
				DAOFactory.LectureDAOIf(session).deleteLecture(lecture);
				tx.commit();
			}
			// do a redirect, so that refreshing the page in a browser doesn't create duplicates
			response.sendRedirect(Util.generateRedirectURL(AdminMenue.class.getSimpleName(), response));
		} else if (("addSuperUser".equals(request.getParameter("action")) || "removeSuperUser".equals(request.getParameter("action"))) && request.getParameter("userid") != null) {
			UserDAOIf userDAO = DAOFactory.UserDAOIf(session);
			session.beginTransaction();
			User user = userDAO.getUser(Util.parseInteger(request.getParameter("userid"), 0));
			if (user != null) {
				user.setSuperUser("addSuperUser".equals(request.getParameter("action")));
			}
			session.getTransaction().commit();
			response.sendRedirect(Util.generateRedirectURL(AdminMenue.class.getSimpleName() + "?action=showAdminUsers", response));
		} else if ("addUserMulti".equals(request.getParameter("action")) && request.getParameter("mailadresses") != null && request.getParameter("lecture") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
			UserDAOIf userDAO = DAOFactory.UserDAOIf(session);
			Transaction tx = session.beginTransaction();
			int count = 0;
			List<String> errors = new ArrayList<>();
			if (lecture == null) {
				response.sendRedirect(Util.generateRedirectURL(AdminMenue.class.getSimpleName(), response));
				return;
			}
			String mailadresses[] = request.getParameter("mailadresses").replaceAll("\r\n", "\n").split("\n");
			for (String mailaddress : mailadresses) {
				if (mailaddress.isEmpty()) {
					continue;
				}
				User user = userDAO.getUserByEmail(mailaddress);
				if (user == null) {
					errors.add("\"" + mailaddress + "\" nicht gefunden.");
					continue;
				}
				Participation participation = participationDAO.getParticipationLocked(user, lecture);
				if (participation == null) {
					errors.add("\"" + mailaddress + "\" ist kein Teilnehmer der Veranstaltung.");
					continue;
				}
				if (!participation.getRoleType().equals(ParticipationRole.NORMAL)) {
					errors.add("\"" + mailaddress + "\" ist kein normaler Teilnehmer der Veranstaltung.");
					continue;
				}
				participation.setRoleType(ParticipationRole.TUTOR);
				++count;
			}
			tx.commit();
			StringBuilder output = new StringBuilder();
			if (!errors.isEmpty()) {
				output.append("<h2>Fehler</h2><ul>");
				for (String string : errors) {
					output.append("<li>" + Util.escapeHTML(string) + "</li>");
				}
				output.append("</ul>");
			}
			output.append("<h2>Ergebnis</h2>");
			output.append("<p>Zu TutorInnen befördert: " + count + "</p>");
			output.append("<p class=mid><a href=\"" + Util.generateHTMLLink(AdminMenue.class.getSimpleName() + "?action=showLecture&lecture=" + lecture.getId(), response) + "\">zurück zur Übersicht</a></p>");
			request.setAttribute("title", "Batch-Ergebnisse");
			request.setAttribute("message", output.toString());
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
		} else if ("addParticipants".equals(request.getParameter("action")) && request.getParameter("mailadresses") != null && request.getParameter("lecture") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
			UserDAOIf userDAO = DAOFactory.UserDAOIf(session);
			Transaction tx = session.beginTransaction();
			int count = 0;
			List<String> errors = new ArrayList<>();
			if (lecture == null) {
				response.sendRedirect(Util.generateRedirectURL(AdminMenue.class.getSimpleName(), response));
				return;
			}
			String mailadresses[] = request.getParameter("mailadresses").replaceAll("\r\n", "\n").split("\n");
			for (String mailaddress : mailadresses) {
				if (mailaddress.isEmpty()) {
					continue;
				}
				User user = userDAO.getUserByEmail(mailaddress);
				if (user == null) {
					errors.add("\"" + mailaddress + "\" nicht gefunden.");
					continue;
				}
				if (!participationDAO.createParticipation(user, lecture, ParticipationRole.NORMAL)) {
					errors.add("\"" + mailaddress + "\" ist bereits Teilnehmender der Veranstaltung.");
					continue;
				}
				++count;
			}
			StringBuilder output = new StringBuilder();
			if (!errors.isEmpty()) {
				output.append("<h2>Fehler</h2><ul>");
				for (String string : errors) {
					output.append("<li>" + Util.escapeHTML(string) + "</li>");
				}
				output.append("</ul>");
				if (request.getParameter("failonerror") != null) {
					tx.rollback();
					count = 0;
				} else {
					tx.commit();
				}
			} else {
				tx.commit();
			}
			output.append("<h2>Ergebnis</h2>");
			output.append("<p>Teilnehmende hinzugefügt: " + count + "</p>");
			output.append("<p class=mid><a href=\"" + Util.generateHTMLLink(AdminMenue.class.getSimpleName() + "?action=showLecture&lecture=" + lecture.getId(), response) + "\">zurück zur Übersicht</a></p>");
			request.setAttribute("title", "Batch-Ergebnisse");
			request.setAttribute("message", output.toString());
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
		} else if (("addUser".equals(request.getParameter("action")) || "removeUser".equals(request.getParameter("action"))) && request.getParameter("lecture") != null && request.getParameter("participationid") != null) {
			Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
			ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
			Transaction tx = session.beginTransaction();
			Participation participation = participationDAO.getParticipationLocked(Util.parseInteger(request.getParameter("participationid"), 0));
			if (lecture == null || participation == null) {
				response.sendRedirect(Util.generateRedirectURL(AdminMenue.class.getSimpleName(), response));
			} else {
				if (request.getParameter("action").equals("addUser")) {
					if ("advisor".equals(request.getParameter("type"))) {
						participation.setRoleType(ParticipationRole.ADVISOR);
					} else if ("tutor".equals(request.getParameter("type"))) {
						participation.setRoleType(ParticipationRole.TUTOR);
					}
				} else { // dregregate user
					participation.setRoleType(ParticipationRole.NORMAL);
				}
				response.sendRedirect(Util.generateRedirectURL(AdminMenue.class.getSimpleName() + "?action=showLecture&lecture=" + lecture.getId(), response));
			}
			tx.commit();
		} else {
			request.setAttribute("title", "Ungültiger Aufruf");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
		}
	}

	private static void cleanupDataDirectory(final Session session, final File path, boolean dryRun, final PrintWriter out) {
		final ZonedDateTime now = ZonedDateTime.now();
		final LectureDAOIf lectureDAO = DAOFactory.LectureDAOIf(session);
		final TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		final SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);

		for (File lectures : path.listFiles()) {
			if (!lectures.isDirectory() || !Util.isInteger(lectures.getName())) {
				continue;
			}
			if (lectureDAO.getLecture(Util.parseInteger(lectures.getName(), 0)) == null) {
				if (dryRun) {
					out.println("Would delete: " + Util.escapeHTML(lectures.toString()) + "<br>");
					continue;
				}
				out.println("Deleting: " + Util.escapeHTML(lectures.toString()) + "<br>");
				Util.recursiveDelete(lectures);
				continue;
			}

			for (File tasks : lectures.listFiles()) {
				Task task = null;
				if (!tasks.isDirectory() || (task = taskDAO.getTask(Util.parseInteger(tasks.getName(), 0))) == null) {
					if (dryRun) {
						out.println("Would delete: " + Util.escapeHTML(tasks.toString()) + "<br>");
						continue;
					}
					out.println("Deleting: " + Util.escapeHTML(tasks.toString()) + "<br>");
					Util.recursiveDelete(tasks);
					continue;
				}

				// list all submissions
				Set<Integer> submissionsInDB = submissionDAO.getSubmissionsForTaskOrdered(task, false).stream().map(s -> s.getSubmissionid()).collect(Collectors.toSet());
				for (File submissions : tasks.listFiles()) {
					// check for junittests
					if (submissions.isFile() && submissions.getName().startsWith("junittest")) {
						boolean kill = true;
						for (Test test : task.getTests()) {
							if (submissions.getName().equals("junittest" + test.getId() + ".jar") && test instanceof JUnitTest) {
								kill = false;
								break;
							}
						}
						if (kill) {
							if (dryRun) {
								out.println("Would delete: " + Util.escapeHTML(submissions.toString()) + "<br>");
								continue;
							}
							out.println("Deleting: " + Util.escapeHTML(submissions.toString()) + "<br>");
							submissions.delete();
						}
						continue;
					}
					if (task.getDeadline().isAfter(now)) { // do not clean up submissions for tasks that are still open for submission
						continue;
					}
					if (!submissions.isDirectory() || !Util.isInteger(submissions.getName())) { // TODO: implement concrete whitelist here: logs, modelsolutionfiles, advisorfiles, musterloesung-ID.xmi
						if (dryRun) {
							continue;
						}
						Util.recursiveDeleteEmptyDirectories(submissions);
						continue;
					}
					if (!submissionsInDB.contains(Util.parseInteger(submissions.getName(), -1))) {
						if (dryRun) {
							out.println("Would delete: " + Util.escapeHTML(submissions.toString()) + "<br>");
							continue;
						}
						out.println("Deleting: " + Util.escapeHTML(submissions.toString()) + "<br>");
						Util.recursiveDelete(submissions);
						continue;
					}
					if (dryRun) {
						continue;
					}
					Util.recursiveDeleteEmptySubDirectories(submissions);
				}
				out.flush();
			}
		}
	}
}
