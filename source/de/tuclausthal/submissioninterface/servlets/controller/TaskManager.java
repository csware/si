/*
 * Copyright 2009 - 2011 Sven Strickroth <email@cs-ware.de>
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.DiskFileUpload;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.PointCategoryDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskGroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for managing (add, edit, remove) tasks by advisors
 * @author Sven Strickroth
 */
public class TaskManager extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			request.setAttribute("title", "Veranstaltung nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), lecture);
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
				if (lecture.getTaskGroups().size() == 0) {
					request.setAttribute("title", "Es wurde noch keine Aufgabengruppe angelegt");
					request.getRequestDispatcher("MessageView").forward(request, response);
					return;
				}
				// temp. Task for code-reuse
				task = new Task();
				task.setStart(new Date());
				task.setDeadline(new Date(new Date().getTime() + 3600 * 24 * 7 * 1000));
				task.setShowPoints(task.getDeadline());
				task.setTaskGroup(lecture.getTaskGroups().get(lecture.getTaskGroups().size() - 1));
			}

			request.setAttribute("task", task);
			request.setAttribute("advisorFiles", Util.listFilesAsRelativeStringList(new File(new ContextAdapter(getServletContext()).getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + "advisorfiles" + System.getProperty("file.separator"))));
			request.getRequestDispatcher("TaskManagerView").forward(request, response);
		} else if (request.getParameter("action") != null && (request.getParameter("action").equals("saveNewTask") || request.getParameter("action").equals("saveTask"))) {
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
			TaskGroup taskGroup = DAOFactory.TaskGroupDAOIf(session).getTaskGroup(Util.parseInteger(request.getParameter("taskGroup"), 0));
			if (taskGroup == null) {
				request.setAttribute("title", "Aufgabengruppe nicht gefunden");
				request.getRequestDispatcher("MessageView").forward(request, response);
			}
			Task task;
			if (request.getParameter("action").equals("saveTask")) {
				task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
				if (task == null) {
					request.setAttribute("title", "Aufgabe nicht gefunden");
					request.getRequestDispatcher("MessageView").forward(request, response);
					return;
				}
				task.setMinPointStep(Util.convertToPoints(request.getParameter("minpointstep")));
				if (task.getPointCategories().size() == 0) {
					task.setMaxPoints(Util.convertToPoints(request.getParameter("maxpoints"), task.getMinPointStep()));
				}
				task.setTitle(request.getParameter("title").trim());
				task.setDescription(request.getParameter("description"));
				task.setMaxSubmitters(Util.parseInteger(request.getParameter("maxSubmitters"), 1));
				task.setFilenameRegexp(request.getParameter("filenameregexp"));
				task.setArchiveFilenameRegexp(request.getParameter("archivefilenameregexp"));
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
				task.setTaskGroup(taskGroup);
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
				int maxSubmitters = 1;
				if (request.getParameter("twosubmitters") != null) {
					maxSubmitters = 2;
				}
				task = taskDAO.newTask(request.getParameter("title"), Util.convertToPoints(request.getParameter("maxpoints"), Util.convertToPoints(request.getParameter("minpointstep"))), Util.convertToPoints(request.getParameter("minpointstep")), startdate, deadline, request.getParameter("description"), taskGroup, showPoints, request.getParameter("filenameregexp"), request.getParameter("archivefilenameregexp"), request.getParameter("showtextarea") != null, request.getParameter("featuredfiles"), request.getParameter("tutorsCanUploadFiles") != null, maxSubmitters);
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
		} else if ("uploadTaskFile".equals(request.getParameter("action"))) {
			Template template = TemplateFactory.getTemplate(request, response);
			PrintWriter out = response.getWriter();

			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
			Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
			if (task == null) {
				request.setAttribute("title", "Aufgabe nicht gefunden");
				request.getRequestDispatcher("MessageView").forward(request, response);
			}
			if (FileUpload.isMultipartContent(request)) {
				ContextAdapter contextAdapter = new ContextAdapter(getServletContext());

				File path = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + "advisorfiles" + System.getProperty("file.separator"));
				if (path.exists() == false) {
					path.mkdirs();
				}
				List<FileItem> items = null;
				// Create a new file upload handler
				FileUploadBase upload = new DiskFileUpload();

				// Parse the request
				try {
					items = upload.parseRequest(request);
				} catch (FileUploadException e) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
					return;
				}

				// Process the uploaded items
				Iterator<FileItem> iter = items.iterator();
				while (iter.hasNext()) {
					FileItem item = iter.next();

					// Process a file upload
					if (!item.isFormField()) {
						Pattern pattern = Pattern.compile(".*?(?:\\\\|/)?([a-zA-Z0-9_.-]+)$");
						StringBuffer submittedFileName = new StringBuffer(item.getName());
						if (submittedFileName.lastIndexOf(".") > 0) {
							int lastDot = submittedFileName.lastIndexOf(".");
							submittedFileName.replace(lastDot, submittedFileName.length(), submittedFileName.subSequence(lastDot, submittedFileName.length()).toString().toLowerCase());
						}
						Matcher m = pattern.matcher(submittedFileName);
						if (!m.matches()) {
							System.out.println("SubmitSolutionProblem2: " + item.getName() + ";" + submittedFileName + ";" + pattern.pattern());
							template.printTemplateHeader("Ungültige Anfrage");
							out.println("Dateiname ungültig bzw. entspricht nicht der Vorgabe (ist ein Klassenname vorgegeben, so muss die Datei genauso heißen).<br>Tipp: Nur A-Z, a-z, 0-9, ., - und _ sind erlaubt.");
							template.printTemplateFooter();
							return;
						}
						String fileName = m.group(1);

						File uploadedFile = new File(path, fileName);
						try {
							item.write(uploadedFile);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				response.sendRedirect(response.encodeRedirectURL("TaskManager?lecture=" + task.getTaskGroup().getLecture().getId() + "&action=editTask&taskid=" + task.getTaskid()));
				return;
			} else {
				// error
			}
		} else if ((("editTaskGroup".equals(request.getParameter("action")) && request.getParameter("taskgroupid") != null) || (request.getParameter("action").equals("newTaskGroup") && request.getParameter("lecture") != null))) {
			boolean editTaskGroup = request.getParameter("action").equals("editTaskGroup");
			TaskGroup taskGroup;
			if (editTaskGroup == true) {
				TaskGroupDAOIf taskDAO = DAOFactory.TaskGroupDAOIf(session);
				taskGroup = taskDAO.getTaskGroup(Util.parseInteger(request.getParameter("taskgroupid"), 0));
				if (taskGroup == null) {
					request.setAttribute("title", "Aufgabengruppe nicht gefunden");
					request.getRequestDispatcher("MessageView").forward(request, response);
					return;
				}
			} else {
				// temp. Task for code-reuse
				taskGroup = new TaskGroup();
				taskGroup.setLecture(lecture);
			}
			request.setAttribute("taskGroup", taskGroup);
			request.getRequestDispatcher("TaskGroupManagerView").forward(request, response);
		} else if (("saveNewTaskGroup".equals(request.getParameter("action")) || "saveTaskGroup".equals(request.getParameter("action")))) {
			TaskGroupDAOIf taskGroupDAO = DAOFactory.TaskGroupDAOIf(session);
			TaskGroup taskGroup;
			if (request.getParameter("action").equals("saveTaskGroup")) {
				taskGroup = taskGroupDAO.getTaskGroup(Util.parseInteger(request.getParameter("taskgroupid"), 0));
				if (taskGroup == null) {
					request.setAttribute("title", "Aufgabengruppe nicht gefunden");
					request.getRequestDispatcher("MessageView").forward(request, response);
					return;
				}
				taskGroup.setTitle(request.getParameter("title"));
				Transaction tx = session.beginTransaction();
				taskGroupDAO.saveTaskGroup(taskGroup);
				tx.commit();
			} else {
				Transaction tx = session.beginTransaction();
				taskGroupDAO.newTaskGroup(request.getParameter("title"), lecture);
				tx.commit();
			}
			response.sendRedirect(response.encodeRedirectURL("ShowLecture?lecture=" + lecture.getId()));
			return;
		} else if ("deleteTaskGroup".equals(request.getParameter("action"))) {
			TaskGroupDAOIf taskGroupDAO = DAOFactory.TaskGroupDAOIf(session);
			TaskGroup taskGroup = taskGroupDAO.getTaskGroup(Util.parseInteger(request.getParameter("taskgroupid"), 0));
			if (taskGroup == null) {
				request.setAttribute("title", "Aufgabengruppe nicht gefunden");
				request.getRequestDispatcher("MessageView").forward(request, response);
			} else {
				Transaction tx = session.beginTransaction();
				taskGroupDAO.deleteTaskGroup(taskGroup);
				tx.commit();
			}
			response.sendRedirect(response.encodeRedirectURL("ShowLecture?lecture=" + lecture.getId()));
			return;
		} else if ("deletePointCategory".equals(request.getParameter("action"))) {
			PointCategoryDAOIf pointCategoryDAO = DAOFactory.PointCategoryDAOIf(session);
			PointCategory pointCategory = pointCategoryDAO.getPointCategory(Util.parseInteger(request.getParameter("pointCategoryId"), 0));
			if (pointCategory == null) {
				request.setAttribute("title", "Punktkategorie nicht gefunden");
				request.getRequestDispatcher("MessageView").forward(request, response);
			} else {
				Transaction tx = session.beginTransaction();
				// TODO make nice! and use DAOs
				Task task = pointCategory.getTask();
				session.lock(task, LockMode.UPGRADE);
				pointCategoryDAO.deletePointCategory(pointCategory);
				task.setMaxPoints(pointCategoryDAO.countPoints(task));
				session.update(task);
				tx.commit();
			}
			response.sendRedirect(response.encodeRedirectURL("TaskManager?lecture=" + pointCategory.getTask().getTaskGroup().getLecture().getId() + "&action=editTask&taskid=" + pointCategory.getTask().getTaskid()));
			return;
		} else if ("newPointCategory".equals(request.getParameter("action"))) {
			PointCategoryDAOIf pointCategoryDAO = DAOFactory.PointCategoryDAOIf(session);
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
			Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
			if (task == null) {
				request.setAttribute("title", "Aufgabe nicht gefunden");
				request.getRequestDispatcher("MessageView").forward(request, response);
			}
			if (Util.convertToPoints(request.getParameter("points"), task.getMinPointStep()) > 0) {
				// TODO make nice! and use DAOs
				Transaction tx = session.beginTransaction();
				session.lock(task, LockMode.UPGRADE);
				pointCategoryDAO.newPointCategory(task, Util.convertToPoints(request.getParameter("points")), request.getParameter("description"), request.getParameter("optional") != null);
				task.setMaxPoints(pointCategoryDAO.countPoints(task));
				session.update(task);
				tx.commit();
				response.sendRedirect(response.encodeRedirectURL("TaskManager?lecture=" + lecture.getId() + "&action=editTask&taskid=" + task.getTaskid()));
			} else {
				request.setAttribute("title", "Punkte ungültig.");
				request.getRequestDispatcher("MessageView").forward(request, response);
			}
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
