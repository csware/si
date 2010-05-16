/*
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of the SubmissionInterface.
 * 
 * SubmissionInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * 
 * SubmissionInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SubmissionInterface. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.servlets.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for managing (add, edit, remove) tasks by advisors
 * @author Sven Strickroth
 */
public class TaskManager extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = HibernateSessionHelper.getSession();
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			request.setAttribute("title", "Veranstaltung nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(session), lecture);
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (request.getParameter("action") != null && ((request.getParameter("action").equals("editTask") && request.getParameter("taskid") != null) || (request.getParameter("action").equals("newTask") && request.getParameter("lecture") != null))) {
			boolean editTask = request.getParameter("action").equals("editTask");
			Task task;
			if (editTask == true) {
				TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
				task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
				if (task == null) {
					request.setAttribute("title", "Aufgabe nicht gefunden");
					request.getRequestDispatcher("MessageView").forward(request, response);
					return;
				}
			} else {
				// temp. Task for code-reuse
				task = new Task();
				task.setStart(new Date());
				task.setDeadline(new Date(new Date().getTime() + 3600 * 24 * 7 * 1000));
				task.setShowPoints(task.getDeadline());
				task.setFilenameRegexp("[A-Z][A-Za-z0-9_]+\\.java");
				task.setLecture(lecture);
			}

			request.setAttribute("task", task);
			request.getRequestDispatcher("TaskManagerView").forward(request, response);
		} else if (request.getParameter("action") != null && (request.getParameter("action").equals("saveNewTask") || request.getParameter("action").equals("saveTask"))) {
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
			Task task;
			if (request.getParameter("action").equals("saveTask")) {
				task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
				if (task == null) {
					request.setAttribute("title", "Aufgabe nicht gefunden");
					request.getRequestDispatcher("MessageView").forward(request, response);
					return;
				}
				task.setMaxPoints(Util.convertToPoints(request.getParameter("maxpoints")));
				task.setTitle(request.getParameter("title"));
				task.setDescription(request.getParameter("description"));
				task.setFilenameRegexp(request.getParameter("filenameregexp"));
				task.setFeaturedFiles(request.getParameter("featuredfiles"));
				task.setShowTextArea(request.getParameter("showtextarea") != null);
				task.setTutorsCanUploadFiles(request.getParameter("tutorsCanUploadFiles") != null);
				task.setStart(parseDate(request.getParameter("startdate"), new Date()));
				task.setDeadline(parseDate(request.getParameter("deadline"), new Date()));
				if (task.getDeadline().before(task.getStart())) {
					task.setDeadline(task.getStart());
				}
				task.setShowPoints(parseDate(request.getParameter("pointsdate"), new Date()));
				if (task.getShowPoints().before(task.getDeadline())) {
					task.setShowPoints(task.getDeadline());
				}
				taskDAO.saveTask(task);
			} else {
				Date startdate = parseDate(request.getParameter("startdate"), new Date());
				Date deadline = parseDate(request.getParameter("deadline"), new Date());
				if (deadline.before(startdate)) {
					deadline = startdate;
				}
				Date showPoints = parseDate(request.getParameter("pointsdate"), new Date());
				if (showPoints.before(deadline)) {
					showPoints = deadline;
				}
				task = taskDAO.newTask(request.getParameter("title"), Util.convertToPoints(request.getParameter("maxpoints")), startdate, deadline, request.getParameter("description"), lecture, showPoints, request.getParameter("filenameregexp"), request.getParameter("showtextarea") != null, request.getParameter("featuredfiles"), request.getParameter("tutorsCanUploadFiles") != null);
			}
			// do a redirect, so that refreshing the page in a browser doesn't create duplicates
			response.sendRedirect(response.encodeRedirectURL("ShowTask?taskid=" + task.getTaskid()));
			return;
		} else if ("deleteTask".equals(request.getParameter("action"))) {
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
			Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
			if (task == null) {
				request.setAttribute("title", "Aufgabe nicht gefunden");
				request.getRequestDispatcher("MessageView").forward(request, response);
			} else {
				taskDAO.deleteTask(task);
			}
			response.sendRedirect(response.encodeRedirectURL("ShowLecture?lecture=" + lecture.getId()));
			return;
		} else {
			request.setAttribute("title", "Ungültiger Aufruf");
			request.getRequestDispatcher("MessageView").forward(request, response);

		}
	}

	/**
	 * Parses a string to a date
	 * @param dateString the string to parse as a date
	 * @param def the default date to return if parsing fails
	 * @return the parsed or default date
	 */
	public Date parseDate(String dateString, Date def) {
		SimpleDateFormat formatA = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		SimpleDateFormat formatB = new SimpleDateFormat("dd.MM.yyyy");
		Date date = null;
		try {
			date = formatA.parse(dateString);
		} catch (ParseException e) {
		}
		if (date == null) {
			try {
				date = formatB.parse(dateString);
			} catch (ParseException e) {
			}
		}
		if (date == null) {
			date = def;
		}
		return date;
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
