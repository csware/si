/*
 * Copyright 2009-2012, 2014, 2020-2023 Sven Strickroth <email@cs-ware.de>
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
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieFactory;
import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieIf;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.PointCategoryDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskGroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.ModelSolutionProvisionType;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.servlets.view.TaskGroupManagerView;
import de.tuclausthal.submissioninterface.servlets.view.TaskManagerDynamicTaskPreView;
import de.tuclausthal.submissioninterface.servlets.view.TaskManagerView;
import de.tuclausthal.submissioninterface.tasktypes.ClozeTaskType;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for managing (add, edit, remove) tasks by advisors
 * @author Sven Strickroth
 */
@MultipartConfig(maxFileSize = Configuration.MAX_UPLOAD_SIZE)
@GATEController
public class TaskManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Veranstaltung nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), lecture);
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (("editTask".equals(request.getParameter("action")) && request.getParameter("taskid") != null) || ("newTask".equals(request.getParameter("action")) && request.getParameter("lecture") != null)) {
			boolean editTask = request.getParameter("action").equals("editTask");
			Task task;
			if (editTask == true) {
				TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
				task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
				if (task == null || task.getTaskGroup().getLecture().getId() != participation.getLecture().getId()) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					request.setAttribute("title", "Aufgabe nicht gefunden");
					getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
					return;
				}
				if (task.isSCMCTask()) {
					request.setAttribute("mcOptions", DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(task));
				}
			} else {
				if (lecture.getTaskGroups().isEmpty()) {
					request.setAttribute("title", "Es wurde noch keine Aufgabengruppe angelegt");
					getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
					return;
				}
				// temp. Task for code-reuse
				task = new Task();
				// guess start and end times
				TaskGroup potentialTaskGroup = lecture.getTaskGroups().get(lecture.getTaskGroups().size() - 1);
				if (!potentialTaskGroup.getTasks().isEmpty()) {
					task.setStart(potentialTaskGroup.getTasks().get(0).getStart());
					task.setDeadline(potentialTaskGroup.getTasks().get(0).getDeadline());
					task.setShowPoints(potentialTaskGroup.getTasks().get(0).getShowPoints());
				} else if (lecture.getTaskGroups().size() > 1 && (potentialTaskGroup = lecture.getTaskGroups().get(lecture.getTaskGroups().size() - 2)) != null && !potentialTaskGroup.getTasks().isEmpty()) {
					task.setStart(potentialTaskGroup.getTasks().get(0).getStart().plusWeeks(1));
					task.setDeadline(potentialTaskGroup.getTasks().get(0).getDeadline().plusWeeks(1));
					if (potentialTaskGroup.getTasks().get(0).getShowPoints() != null) {
						task.setShowPoints(potentialTaskGroup.getTasks().get(0).getShowPoints().plusWeeks(1));
					}
				} else {
					task.setStart(ZonedDateTime.now());
					task.setDeadline(ZonedDateTime.now().plusWeeks(1));
					task.setShowPoints(task.getDeadline());
				}
				task.setTaskGroup(lecture.getTaskGroups().get(lecture.getTaskGroups().size() - 1));
			}

			request.setAttribute("task", task);
			final File taskPath = Util.constructPath(Configuration.getInstance().getDataPath(), task);
			request.setAttribute("advisorFiles", Util.listFilesAsRelativeStringListSorted(new File(taskPath, "advisorfiles" + System.getProperty("file.separator"))));
			request.setAttribute("modelSolutionFiles", Util.listFilesAsRelativeStringListSorted(new File(taskPath, "modelsolutionfiles" + System.getProperty("file.separator"))));

			getServletContext().getNamedDispatcher(TaskManagerView.class.getSimpleName()).forward(request, response);
		} else if ((("editTaskGroup".equals(request.getParameter("action")) && request.getParameter("taskgroupid") != null) || ("newTaskGroup".equals(request.getParameter("action")) && request.getParameter("lecture") != null))) {
			boolean editTaskGroup = request.getParameter("action").equals("editTaskGroup");
			TaskGroup taskGroup;
			if (editTaskGroup == true) {
				TaskGroupDAOIf taskDAO = DAOFactory.TaskGroupDAOIf(session);
				taskGroup = taskDAO.getTaskGroup(Util.parseInteger(request.getParameter("taskgroupid"), 0));
				if (taskGroup == null || taskGroup.getLecture().getId() != participation.getLecture().getId()) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					request.setAttribute("title", "Aufgabengruppe nicht gefunden");
					getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
					return;
				}
			} else {
				// temp. Task for code-reuse
				taskGroup = new TaskGroup();
				taskGroup.setLecture(lecture);
			}
			request.setAttribute("taskGroup", taskGroup);
			getServletContext().getNamedDispatcher(TaskGroupManagerView.class.getSimpleName()).forward(request, response);
		} else if ("dynamictaskpreview".equals(request.getParameter("action"))) {
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
			Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
			if (task == null || task.getTaskGroup().getLecture().getId() != participation.getLecture().getId()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Aufgabe nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			if (!task.isADynamicTask()) {
				request.setAttribute("title", "Aufgabe ist keine dynamische Aufgabe");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}

			DynamicTaskStrategieIf dts = task.getDynamicTaskStrategie(session);
			List<TaskNumber> taskNumbers = dts.getVariables((Participation) null);
			List<String> correctResults = dts.getCorrectResults(taskNumbers, true);

			task.setDescription(dts.getTranslatedDescription(taskNumbers));
			request.setAttribute("task", task);
			request.setAttribute("variableNames", dts.getVariableNames());
			request.setAttribute("taskNumbers", taskNumbers);
			request.setAttribute("correctResults", correctResults);
			request.setAttribute("resultFields", dts.getResultFields(true));

			getServletContext().getNamedDispatcher(TaskManagerDynamicTaskPreView.class.getSimpleName()).forward(request, response);
		} else {
			request.setAttribute("title", "Ungültiger Aufruf");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
		}
	}

	private void handleUploadedFiles(Lecture lecture, String foldername, HttpServletRequest request, HttpServletResponse response, Session session) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);
		PrintWriter out = response.getWriter();

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null || task.getTaskGroup().getLecture().getId() != lecture.getId()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		final File path = new File(Util.constructPath(Configuration.getInstance().getDataPath(), task), foldername);
		Util.ensurePathExists(path);

		long fileParts = request.getParts().stream().filter(part -> "file".equals(part.getName())).count();
		if (fileParts == 0) {
			request.setAttribute("title", "Keine Datei gefunden.");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}
		if (fileParts > 1 && fileParts != request.getParts().stream().filter(part -> "file".equals(part.getName())).map(part -> Util.getUploadFileName(part)).collect(Collectors.toSet()).size()) {
			request.setAttribute("title", "Mehrere Dateien gleichen Namens gefunden.");
			request.setAttribute("message", "<div class=mid><a href=\"javascript:window.history.back();\">zurück zur vorherigen Seite</a></div>");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}
		// Process a file upload
		Pattern pattern = Pattern.compile(Configuration.GLOBAL_FILENAME_REGEXP);
		for (Part file : request.getParts()) {
			if (!file.getName().equalsIgnoreCase("file")) {
				continue;
			}
			StringBuffer submittedFileName = new StringBuffer(Util.getUploadFileName(file));
			Util.lowerCaseExtension(submittedFileName);
			Matcher m = pattern.matcher(submittedFileName);
			if (!m.matches()) {
				LOG.debug("Filename did not match pattern: file;" + submittedFileName + ";" + pattern.pattern());
				template.printTemplateHeader("Dateiname ungültig");
				out.println("Dateiname ungültig, Datei muss folgendem Regexp entsprechen: &quot;" + Util.escapeHTML(Configuration.GLOBAL_FILENAME_REGEXP) + "&quot;");
				template.printTemplateFooter();
				return;
			}
			String fileName = m.group(1);

			File uploadedFile = Util.buildPath(path, fileName);
			try (InputStream is = file.getInputStream()) {
				Util.copyInputStreamAndClose(is, uploadedFile);
			}
		}

		response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?lecture=" + task.getTaskGroup().getLecture().getId() + "&action=editTask&taskid=" + task.getTaskid() + "#" + foldername, response));
	}

	/**
	 * Parses a string to a date
	 * @param dateString the string to parse as a date
	 * @param def the default date to return if parsing fails
	 * @return the parsed or default date
	 */
	static public ZonedDateTime parseDate(String dateString, ZonedDateTime def) {
		ZonedDateTime date = null;
		try {
			date = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")).atZone(ZoneId.systemDefault());
		} catch (DateTimeParseException e) {
		}
		if (date == null) {
			try {
				date = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("dd.MM.yyyy")).atZone(ZoneId.systemDefault());
			} catch (DateTimeParseException e) {
			}
		}
		if (date == null) {
			date = def;
		}
		return date;
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if ("regexptest".equals(request.getParameter("action"))) {
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			String regexp = request.getParameter("regexp");
			if ("-".equals(regexp)) {
				out.println("Dateiupload deaktiviert.");
				return;
			}
			if (!Pattern.compile(Configuration.GLOBAL_FILENAME_REGEXP).matcher(request.getParameter("test")).matches()) {
				out.println("nicht OK (blockiert vom globalen System-Regexp)");
				return;
			}
			try {
				if (regexp != null && !regexp.isEmpty() && !Pattern.compile("^(" + regexp + ")$").matcher(request.getParameter("test")).matches()) {
					out.println("nicht OK");
					return;
				}
			} catch (PatternSyntaxException e) {
				out.println("Regulärer Ausdruck ungültig:");
				out.println(e.getLocalizedMessage());
				return;
			}
			out.println("OK");
			return;
		} else if ("dynamictaskhints".equals(request.getParameter("action"))) {
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			DynamicTaskStrategieIf dts = DynamicTaskStrategieFactory.createDynamicTaskStrategie(null, request.getParameter("dynamicTask"), null);
			if (dts == null) {
				out.println("unbekannte dynamische Aufgabe");
			} else {
				out.println("<b>" + DynamicTaskStrategieFactory.GetStrategieName(request.getParameter("dynamicTask")) + "</b><br>");
				out.println("<dl>");
				out.println("<dt><b>Beispiel-Aufgabenstellung:</b></dt>");
				out.println("<dd>" + Util.makeCleanHTML(dts.getExampleTaskDescription()) + "</dd>");

				out.println("<dt><b>Variablen:</b></dt>");
				out.println("<dd>");
				int variableCounter = 0;
				for (String variableName : dts.getVariableNames()) {
					out.print(Util.escapeHTML(variableName) + ": $Var" + variableCounter + "$<br>");
					variableCounter++;
				}
				out.println("</dd>");

				out.println("<dt><b>Lösungsfelder:</b></dt>");
				out.println("<dd>");
				for (String resultField : dts.getResultFields(false)) {
					out.println(Util.escapeHTML(resultField) + "<br>");
				}
				out.println("</dd>");
				out.println("</dl>");
			}
			return;
		} else if ("checkregexps".equals(request.getParameter("action"))) {
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			try {
				for (String regexp : request.getParameter("regexps").split("\n")) {
					Pattern.compile(regexp);
				}
			} catch (PatternSyntaxException e) {
				out.print(e.getLocalizedMessage());
			}
			out.print("ok");
			return;
		} else if ("clozecheck".equals(request.getParameter("action"))) {
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			ClozeTaskType clozeHelper = new ClozeTaskType(request.getParameter("description"), null, false, false);
			PrintWriter out = response.getWriter();
			if (clozeHelper.hasError()) {
				out.print(clozeHelper.getError());
			} else {
				out.print("ok");
			}
			return;
		} else if ("clozepreview".equals(request.getParameter("action"))) {
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			ClozeTaskType clozeHelper = new ClozeTaskType(request.getParameter("description"), null, false, false);
			Template template = TemplateFactory.getTemplate(request, response);
			template.printTemplateHeader("Close testen...");
			PrintWriter out = response.getWriter();
			if (clozeHelper.hasError()) {
				out.println("Cloze konnte nicht geparsed werden:<br>");
				out.print(Util.escapeHTML(clozeHelper.getError()));
				template.printTemplateFooter();
				return;
			}
			out.println("<form action=\"" + Util.generateHTMLLink("?action=clozepreviewtest", response) + "\" method=post><div id=taskdescription>");
			out.println(clozeHelper.toHTML());
			out.println("</div><input type=hidden name=description value=\"" + Util.escapeHTML(request.getParameter("description")) + "\">");
			out.println("<input type=submit value=testen...>");
			out.println("</form>");
			template.printTemplateFooter();
			return;
		} else if ("clozepreviewtest".equals(request.getParameter("action"))) {
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			ClozeTaskType clozeHelper = new ClozeTaskType(request.getParameter("description"), null, false, false);
			Template template = TemplateFactory.getTemplate(request, response);
			template.printTemplateHeader("Close testen: Ergebnis");
			List<String> results = clozeHelper.parseResults(request);
			clozeHelper = new ClozeTaskType(request.getParameter("description"), results, true, true);
			PrintWriter out = response.getWriter();
			out.println(clozeHelper.toHTML());
			out.println("<hr>");
			out.println("Automatisch bewertbar: " + Util.boolToHTML(clozeHelper.isAutoGradeAble()) + "<br>");
			if (clozeHelper.isAutoGradeAble()) {
				out.println("Calculated points: " + Util.showPoints(clozeHelper.calculatePoints(results)));
			}
			template.printTemplateFooter();
			return;
		}

		Session session = RequestAdapter.getSession(request);
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(Util.parseInteger(request.getParameter("lecture"), 0));
		if (lecture == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Veranstaltung nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), lecture);
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ("saveNewTask".equals(request.getParameter("action")) || "saveTask".equals(request.getParameter("action"))) {
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
			TaskGroup taskGroup = DAOFactory.TaskGroupDAOIf(session).getTaskGroup(Util.parseInteger(request.getParameter("taskGroup"), 0));
			if (taskGroup == null || taskGroup.getLecture().getId() != participation.getLecture().getId()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Aufgabengruppe nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			Task task;
			if (request.getParameter("action").equals("saveTask")) {
				task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
				if (task == null || task.getTaskGroup().getLecture().getId() != participation.getLecture().getId()) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					request.setAttribute("title", "Aufgabe nicht gefunden");
					getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
					return;
				}
				Transaction tx = session.beginTransaction();
				if (!task.isSCMCTask()) {
					task.setMinPointStep(Util.convertToPoints(request.getParameter("minpointstep")));
				}
				if (task.getPointCategories().isEmpty()) {
					task.setMaxPoints(Util.convertToPoints(request.getParameter("maxpoints")));
				}
				if (task.isClozeTask()) {
					ClozeTaskType clozeHelper = new ClozeTaskType(request.getParameter("description"), null, false, false);
					int points = clozeHelper.maxPoints();
					if (points > task.getMaxPoints()) {
						task.setMaxPoints(points);
					}
				}
				task.setTitle(request.getParameter("title").trim());
				task.setDescription(request.getParameter("description"));
				task.setMaxSubmitters(Util.parseInteger(request.getParameter("maxSubmitters"), 1));
				task.setAllowSubmittersAcrossGroups(request.getParameter("allowSubmittersAcrossGroups") != null);
				task.setMaxsize(1024 * Util.parseInteger(request.getParameter("maxfilesize"), 0));
				if (!task.isSCMCTask() && !task.isADynamicTask() && !task.isClozeTask()) {
					task.setFilenameRegexp(request.getParameter("filenameregexp"));
					task.setArchiveFilenameRegexp(request.getParameter("archivefilenameregexp"));
					task.setFeaturedFiles(request.getParameter("featuredfiles"));
					task.setShowTextArea(request.getParameter("showtextarea"));
					task.setTutorsCanUploadFiles(request.getParameter("tutorsCanUploadFiles") != null);
				}
				task.setStart(parseDate(request.getParameter("startdate"), ZonedDateTime.now()));
				task.setDeadline(parseDate(request.getParameter("deadline"), ZonedDateTime.now()));
				if (task.getDeadline().isBefore(task.getStart())) {
					task.setDeadline(task.getStart());
				}
				task.setAllowPrematureSubmissionClosing(request.getParameter("prematureClosing") != null);
				if (request.getParameter("pointsmanual") != null) {
					task.setShowPoints(null);
				} else {
					task.setShowPoints(parseDate(request.getParameter("pointsdate"), ZonedDateTime.now()));
					if (task.getShowPoints().isBefore(task.getDeadline())) {
						task.setShowPoints(task.getDeadline());
					}
				}
				task.setTaskGroup(taskGroup);
				tx.commit();
				if (request.getParameter("dynamictaskpreview") != null) {
					response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid() + "&action=dynamictaskpreview", response));
				} else {
					response.sendRedirect(Util.generateRedirectURL(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response));
				}
			} else {
				ZonedDateTime startdate = parseDate(request.getParameter("startdate"), ZonedDateTime.now());
				ZonedDateTime deadline = parseDate(request.getParameter("deadline"), ZonedDateTime.now());
				ZonedDateTime pointsdate = parseDate(request.getParameter("pointsdate"), ZonedDateTime.now());
				if (deadline.isBefore(startdate)) {
					deadline = startdate;
				}
				if (request.getParameter("pointsmanual") != null) {
					pointsdate = null;
				} else if (pointsdate.isBefore(deadline)) {
					pointsdate = deadline;
				}
				String taskType = "";
				String dynamicTask = null;
				if (request.getParameter("mctask") != null && !request.getParameter("mctask").isEmpty()) {
					if ("singlechoice".equals(request.getParameter("mctask"))) {
						taskType = "sc";
					} else {
						taskType = "mc";
					}
				} else if (request.getParameter("cloze") != null) {
					taskType = "cloze";
				} else {
					if (DynamicTaskStrategieFactory.IsValidStrategieName(request.getParameter("dynamicTask"))) {
						taskType = "dynamicTask";
						dynamicTask = request.getParameter("dynamicTask");
					}
				}
				Transaction tx = session.beginTransaction();
				task = taskDAO.newTask(request.getParameter("title"), Util.convertToPoints(request.getParameter("maxpoints"), 50), startdate, deadline, request.getParameter("description"), taskGroup, pointsdate, Util.parseInteger(request.getParameter("maxSubmitters"), 1), request.getParameter("allowSubmittersAcrossGroups") != null, taskType, dynamicTask, request.getParameter("prematureClosing") != null);
				if (task.isSCMCTask() || task.isClozeTask()) {
					task.setFilenameRegexp("-");
					task.setShowTextArea("-"); // be explicit here, it's false by default
				} else if (task.isADynamicTask()) {
					task.setFilenameRegexp("-");
					task.setShowTextArea("textloesung.txt");
				}
				if (task.isClozeTask()) {
					ClozeTaskType clozeHelper = new ClozeTaskType(request.getParameter("description"), null, false, false);
					int points = clozeHelper.maxPoints();
					if (points > task.getMaxPoints()) {
						task.setMaxPoints(points);
					}
				}
				tx.commit();
				response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid() + "&action=editTask", response));
			}
			return;
		} else if ("deleteTask".equals(request.getParameter("action"))) {
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
			Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
			if (task == null || task.getTaskGroup().getLecture().getId() != participation.getLecture().getId()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Aufgabe nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			Transaction tx = session.beginTransaction();
			taskDAO.deleteTask(task);
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(ShowLecture.class.getSimpleName() + "?lecture=" + lecture.getId(), response));
			return;
		} else if ("uploadTaskFile".equals(request.getParameter("action"))) {
			handleUploadedFiles(participation.getLecture(), "advisorfiles", request, response, session);
			return;
		} else if ("uploadModelSolutionFile".equals(request.getParameter("action"))) {
			handleUploadedFiles(participation.getLecture(), "modelsolutionfiles", request, response, session);
			return;
		} else if (("saveNewTaskGroup".equals(request.getParameter("action")) || "saveTaskGroup".equals(request.getParameter("action")))) {
			TaskGroupDAOIf taskGroupDAO = DAOFactory.TaskGroupDAOIf(session);
			TaskGroup taskGroup;
			if (request.getParameter("action").equals("saveTaskGroup")) {
				taskGroup = taskGroupDAO.getTaskGroup(Util.parseInteger(request.getParameter("taskgroupid"), 0));
				if (taskGroup == null || taskGroup.getLecture().getId() != participation.getLecture().getId()) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					request.setAttribute("title", "Aufgabengruppe nicht gefunden");
					getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
					return;
				}
				Transaction tx = session.beginTransaction();
				taskGroup.setTitle(request.getParameter("title"));
				tx.commit();
			} else {
				Transaction tx = session.beginTransaction();
				taskGroupDAO.newTaskGroup(request.getParameter("title"), lecture);
				tx.commit();
			}
			response.sendRedirect(Util.generateRedirectURL(ShowLecture.class.getSimpleName() + "?lecture=" + lecture.getId(), response));
			return;
		} else if ("deleteTaskGroup".equals(request.getParameter("action"))) {
			TaskGroupDAOIf taskGroupDAO = DAOFactory.TaskGroupDAOIf(session);
			TaskGroup taskGroup = taskGroupDAO.getTaskGroup(Util.parseInteger(request.getParameter("taskgroupid"), 0));
			if (taskGroup == null || taskGroup.getLecture().getId() != participation.getLecture().getId()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Aufgabengruppe nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			Transaction tx = session.beginTransaction();
			taskGroupDAO.deleteTaskGroup(taskGroup);
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(ShowLecture.class.getSimpleName() + "?lecture=" + lecture.getId(), response));
			return;
		} else if ("deletePointCategory".equals(request.getParameter("action"))) {
			PointCategoryDAOIf pointCategoryDAO = DAOFactory.PointCategoryDAOIf(session);
			PointCategory pointCategory = pointCategoryDAO.getPointCategory(Util.parseInteger(request.getParameter("pointCategoryId"), 0));
			if (pointCategory == null || pointCategory.getTask().getTaskGroup().getLecture().getId() != participation.getLecture().getId()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Punktkategorie nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			Transaction tx = session.beginTransaction();
			// TODO make nice! and use DAOs
			Task task = pointCategory.getTask();
			session.lock(task, LockMode.PESSIMISTIC_WRITE);
			pointCategoryDAO.deletePointCategory(pointCategory);
			task.setMaxPoints(pointCategoryDAO.countPoints(task));
			tx.commit();

			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?lecture=" + pointCategory.getTask().getTaskGroup().getLecture().getId() + "&action=editTask&taskid=" + pointCategory.getTask().getTaskid() + "#pointcriteria", response));
			return;
		} else if ("newPointCategory".equals(request.getParameter("action"))) {
			PointCategoryDAOIf pointCategoryDAO = DAOFactory.PointCategoryDAOIf(session);
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
			Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
			if (task == null || task.getTaskGroup().getLecture().getId() != participation.getLecture().getId()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Aufgabe nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			if (Util.convertToPoints(request.getParameter("points"), task.getMinPointStep()) > 0) {
				// TODO make nice! and use DAOs
				Transaction tx = session.beginTransaction();
				session.lock(task, LockMode.PESSIMISTIC_WRITE);
				pointCategoryDAO.newPointCategory(task, Util.convertToPoints(request.getParameter("points")), request.getParameter("description"), request.getParameter("optional") != null);
				task.setMaxPoints(pointCategoryDAO.countPoints(task));
				tx.commit();
				response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?lecture=" + lecture.getId() + "&action=editTask&taskid=" + task.getTaskid() + "#pointcriteria", response));
			} else {
				request.setAttribute("title", "Punkte ungültig.");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			}
			return;
		} else if ("provideModelSolutionToStudents".equals(request.getParameter("action"))) {
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
			Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
			if (task == null || task.getTaskGroup().getLecture().getId() != participation.getLecture().getId()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Aufgabe nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}

			Transaction tx = session.beginTransaction();
			session.lock(task, LockMode.PESSIMISTIC_WRITE);
			task.setModelSolutionProvisionType(ModelSolutionProvisionType.valueOf(request.getParameter("modelsolutiontype")));
			tx.commit();

			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?lecture=" + lecture.getId() + "&action=editTask&taskid=" + task.getTaskid() + "#modelsolutions", response));
		} else if ("newMCOption".equals(request.getParameter("action"))) {
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
			Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
			if (task == null || task.getTaskGroup().getLecture().getId() != participation.getLecture().getId()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Aufgabe nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			Transaction tx = session.beginTransaction();
			DAOFactory.MCOptionDAOIf(session).createMCOption(task, request.getParameter("option"), request.getParameter("correkt") != null);
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?lecture=" + lecture.getId() + "&action=editTask&taskid=" + task.getTaskid() + "#mcoptions", response));
		} else if ("deleteMCOption".equals(request.getParameter("action"))) {
			TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
			Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
			if (task == null || task.getTaskGroup().getLecture().getId() != participation.getLecture().getId()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Aufgabe nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			for (MCOption option : DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(task)) {
				if (option.getId() == Util.parseInteger(request.getParameter("optionId"), -1)) {
					Transaction tx = session.beginTransaction();
					DAOFactory.MCOptionDAOIf(session).deleteMCOption(option);
					tx.commit();
					break;
				}
			}
			response.sendRedirect(Util.generateRedirectURL(TaskManager.class.getSimpleName() + "?lecture=" + lecture.getId() + "&action=editTask&taskid=" + task.getTaskid() + "#mcoptions", response));
		} else {
			request.setAttribute("title", "Ungültiger Aufruf");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
		}
	}
}
