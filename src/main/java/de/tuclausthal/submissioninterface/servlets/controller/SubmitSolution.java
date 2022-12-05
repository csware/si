/*
 * Copyright 2009-2014, 2017, 2020-2022 Sven Strickroth <email@cs-ware.de>
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.MCOptionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.impl.LogDAO;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry.LogAction;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.UMLConstraintTest;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.servlets.view.SubmitSolutionAdvisorFormView;
import de.tuclausthal.submissioninterface.servlets.view.SubmitSolutionFormView;
import de.tuclausthal.submissioninterface.servlets.view.SubmitSolutionPossiblePartnersView;
import de.tuclausthal.submissioninterface.tasktypes.ClozeTaskType;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for the submission of files/solutions
 * @author Sven Strickroth
 */
@MultipartConfig(maxFileSize = Configuration.MAX_UPLOAD_SIZE)
@GATEController
public class SubmitSolution extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		boolean canUploadForStudents = participation.getRoleType() == ParticipationRole.ADVISOR || (task.isTutorsCanUploadFiles() && participation.getRoleType() == ParticipationRole.TUTOR);

		// if session-user is not a tutor (with rights to upload for students) or advisor: check dates
		if (!canUploadForStudents) {
			if (participation.getRoleType() == ParticipationRole.TUTOR) {
				request.setAttribute("title", "TutorInnen können keine eigenen Lösungen einsenden.");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			if (task.getStart().isAfter(ZonedDateTime.now())) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Aufgabe nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			if (task.getDeadline().isBefore(ZonedDateTime.now())) {
				request.setAttribute("title", "Abgabe nicht mehr möglich");
				request.setAttribute("message", "<div class=mid><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
		}

		if (task.showTextArea() == false && "-".equals(task.getFilenameRegexp()) && !task.isSCMCTask() && !task.isClozeTask()) {
			request.setAttribute("title", "Das Einsenden von Lösungen ist für diese Aufgabe deaktiviert.");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		request.setAttribute("task", task);

		if (canUploadForStudents) {
			request.setAttribute("participants", DAOFactory.ParticipationDAOIf(session).getLectureParticipationsOrderedByName(task.getTaskGroup().getLecture()));
			getServletContext().getNamedDispatcher(SubmitSolutionAdvisorFormView.class.getSimpleName()).forward(request, response);
		} else {
			SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
			Submission submission = submissionDAO.getSubmission(task, participation.getUser());
			request.setAttribute("submission", submission);
			request.setAttribute("participation", participation);

			if (request.getParameter("onlypartners") == null) {
				if (task.showTextArea()) {
					String textsolution = "";
					if (submission != null) {
						File textSolutionFile = new File(Configuration.getInstance().getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator") + task.getShowTextArea());
						if (textSolutionFile.exists()) {
							textsolution = Util.loadFile(textSolutionFile).toString();
						}
						if (task.isADynamicTask()) {
							task.setDescription(task.getDynamicTaskStrategie(session).getTranslatedDescription(submission));
						}
					} else {
						if (task.isADynamicTask()) {
							task.setDescription(task.getDynamicTaskStrategie(session).getTranslatedDescription(participation));
						}
					}
					request.setAttribute("textsolution", textsolution);
				}
				getServletContext().getNamedDispatcher(SubmitSolutionFormView.class.getSimpleName()).forward(request, response);
			} else {
				getServletContext().getNamedDispatcher(SubmitSolutionPossiblePartnersView.class.getSimpleName()).forward(request, response);
			}
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Template template = TemplateFactory.getTemplate(request, response);

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			template.printTemplateHeader("Aufgabe nicht gefunden");
			PrintWriter out = response.getWriter();
			out.println("<div class=mid><a href=\"" + Util.generateHTMLLink("?", response) + "\">zur Übersicht</a></div>");
			template.printTemplateFooter();
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);

		Participation studentParticipation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (studentParticipation == null) {
			template.printTemplateHeader("Ungültige Anfrage");
			PrintWriter out = response.getWriter();
			out.println("<div class=mid>Sie nehmen an dieser Veranstaltung nicht teil.</div>");
			out.println("<div class=mid><a href=\"" + Util.generateHTMLLink(Overview.class.getSimpleName(), response) + "\">zur Übersicht</a></div>");
			template.printTemplateFooter();
			return;
		}

		List<Integer> partnerIDs = new ArrayList<>();
		int uploadFor = Util.parseInteger(request.getParameter("uploadFor"), 0);

		if (request.getParameterValues("partnerid") != null) {
			for (String partnerIdParameter : request.getParameterValues("partnerid")) {
				int partnerID = Util.parseInteger(partnerIdParameter, 0);
				if (partnerID > 0) {
					partnerIDs.add(partnerID);
				}
			}
			Collections.sort(partnerIDs);
		}

		Part file = null;
		String contentType = request.getContentType();
		if (contentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/")) {
			file = request.getPart("file");
		}
		if (file != null) {
			if (!request.getParts().stream().allMatch(part -> part.getSize() <= task.getMaxsize())) {
				request.setAttribute("title", "Datei ist zu groß (maximum sind " + task.getMaxsize() + " Bytes)");
				request.setAttribute("message", "<div class=mid><a href=\"javascript:window.history.back();\">zurück zur vorherigen Seite</a></div>");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			long fileParts = request.getParts().stream().filter(part -> "file".equals(part.getName())).count();
			if (fileParts > 1 && fileParts != request.getParts().stream().filter(part -> "file".equals(part.getName())).map(part -> Util.getUploadFileName(part)).collect(Collectors.toSet()).size()) {
				request.setAttribute("title", "Mehrere Dateien mit identischem Namen im Upload gefunden.");
				request.setAttribute("message", "<div class=mid><a href=\"javascript:window.history.back();\">zurück zur vorherigen Seite</a></div>");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
		}

		if (uploadFor > 0) {
			// Uploader ist wahrscheinlich Betreuer -> keine zeitlichen Prüfungen
			// check if uploader is allowed to upload for students
			if (!(studentParticipation.getRoleType() == ParticipationRole.ADVISOR || (task.isTutorsCanUploadFiles() && studentParticipation.getRoleType() == ParticipationRole.TUTOR))) {
				template.printTemplateHeader("Ungültige Anfrage", task);
				PrintWriter out = response.getWriter();
				out.println("<div class=mid>Sie sind nicht berechtigt bei dieser Veranstaltung Dateien für Studierende hochzuladen.</div>");
				out.println("<p><div class=mid><a href=\"javascript:window.history.back();\">zurück zur vorherigen Seite</a></div>");
				template.printTemplateFooter();
				return;
			}
			studentParticipation = participationDAO.getParticipation(uploadFor);
			if (studentParticipation == null || studentParticipation.getLecture().getId() != task.getTaskGroup().getLecture().getId()) {
				template.printTemplateHeader("Ungültige Anfrage", task);
				PrintWriter out = response.getWriter();
				out.println("<div class=mid>Der gewählte Studierende ist keine Teilnehemerin bzw. kein Teilnehmer dieser Veranstaltung.</div>");
				out.println("<p><div class=mid><a href=\"javascript:window.history.back();\">zurück zur vorherigen Seite</a></div>");
				template.printTemplateFooter();
				return;
			}
			if (task.showTextArea() == false && "-".equals(task.getFilenameRegexp()) && !task.isSCMCTask() && !task.isClozeTask()) {
				template.printTemplateHeader("Ungültige Anfrage", task);
				PrintWriter out = response.getWriter();
				out.println("<div class=mid>Das Einsenden von Lösungen ist für diese Aufgabe deaktiviert.</div>");
				template.printTemplateFooter();
				return;
			}
		} else {
			if (studentParticipation.getRoleType() == ParticipationRole.ADVISOR || studentParticipation.getRoleType() == ParticipationRole.TUTOR) {
				template.printTemplateHeader("Ungültige Anfrage", task);
				PrintWriter out = response.getWriter();
				out.println("<div class=mid>BetreuerInnen und TutorInnen können keine eigenen Lösungen einsenden.</div>");
				template.printTemplateFooter();
				return;
			}
			// Uploader is Student, -> hard date checks
			if (task.getStart().isAfter(ZonedDateTime.now())) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				template.printTemplateHeader("Ungültige Anfrage");
				PrintWriter out = response.getWriter();
				out.println("<div class=mid>Aufgabe nicht gefunden.</div>");
				template.printTemplateFooter();
				return;
			}
			if (task.getDeadline().isBefore(ZonedDateTime.now())) {
				template.printTemplateHeader("Ungültige Anfrage", task);
				PrintWriter out = response.getWriter();
				out.println("<div class=mid>Abgabe nicht mehr möglich.</div>");
				out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
				template.printTemplateFooter();
				return;
			}
			if (file != null && "-".equals(task.getFilenameRegexp())) {
				template.printTemplateHeader("Ungültige Anfrage", task);
				PrintWriter out = response.getWriter();
				out.println("<div class=mid>Dateiupload ist für diese Aufgabe deaktiviert.</div>");
				out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
				template.printTemplateFooter();
				return;
			} else if (file == null && !task.showTextArea() && !task.isSCMCTask() && !task.isClozeTask()) {
				template.printTemplateHeader("Ungültige Anfrage", task);
				PrintWriter out = response.getWriter();
				out.println("<div class=mid>Textlösungen sind für diese Aufgabe deaktiviert.</div>");
				out.println("<p><div class=mid><a href=\"javascript:window.history.back();\">zurück zur vorherigen Seite</a></div>");
				template.printTemplateFooter();
				return;
			}
		}

		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);

		Transaction tx = session.beginTransaction();
		// lock participation (in createSubmission), because locking of not-existing entries in InnoDB might lock the whole table (submissions AND tasks) causing a strict serialization of ALL requests
		Submission submission = submissionDAO.createSubmission(task, studentParticipation);

		if (uploadFor <= 0 && task.isAllowPrematureSubmissionClosing() && submission.isClosed()) {
			tx.rollback();
			request.setAttribute("title", "Die Abgabe wurde bereits als endgültig abgeschlossen markiert. Eine Veränderung ist daher nicht mehr möglich.");
			request.setAttribute("message", "<p><div class=mid><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		if (task.getMaxSubmitters() > 1 && (task.isAllowSubmittersAcrossGroups() || studentParticipation.getGroup() != null)) {
			if (studentParticipation.getGroup() != null && studentParticipation.getGroup().isSubmissionGroup()) {
				for (Participation partnerParticipation : studentParticipation.getGroup().getMembers()) {
					session.buildLockRequest(LockOptions.UPGRADE).lock(partnerParticipation); // creating submissions is serialized by locking the participation, see above
					if (partnerParticipation.getRoleType().ordinal() >= ParticipationRole.TUTOR.ordinal()) {
						continue;
					}
					Submission partnerSubmission = submissionDAO.getSubmission(task, partnerParticipation.getUser());
					if (partnerSubmission == null) {
						submission.getSubmitters().add(partnerParticipation);
					} else if (partnerSubmission.getSubmissionid() != submission.getSubmissionid()) {
						tx.rollback();
						template.printTemplateHeader("Ungültige Anfrage", task);
						PrintWriter out = response.getWriter();
						out.println("<div class=mid>Es wurde bereits eine Gruppen-Abgabe initiiert.</div>");
						template.printTemplateFooter();
						return;
					}
				}
			} else {
				for (int partnerID : partnerIDs) {
					Participation partnerParticipation = participationDAO.getParticipation(partnerID);
					session.buildLockRequest(LockOptions.UPGRADE).lock(partnerParticipation); // creating submissions is serialized by locking the participation, see above
					if (submission.getSubmitters().size() < task.getMaxSubmitters() && partnerParticipation != null && partnerParticipation.getRoleType().equals(ParticipationRole.NORMAL) && partnerParticipation.getLecture().getId() == task.getTaskGroup().getLecture().getId() && ((task.isAllowSubmittersAcrossGroups() && (partnerParticipation.getGroup() == null || !partnerParticipation.getGroup().isSubmissionGroup())) || (!task.isAllowSubmittersAcrossGroups() && partnerParticipation.getGroup() != null && studentParticipation.getGroup() != null && partnerParticipation.getGroup().getGid() == studentParticipation.getGroup().getGid())) && submissionDAO.getSubmission(task, partnerParticipation.getUser()) == null) {
						submission.getSubmitters().add(partnerParticipation);
					} else {
						tx.rollback();
						template.printTemplateHeader("Ungültige Anfrage", task);
						PrintWriter out = response.getWriter();
						out.println("<div class=mid>Ein ausgewählter Studierender hat bereits eine eigene Abgabe initiiert, Sie haben bereits die maximale Anzahl von Studierenden überschritten oder einen nicht verfügbaren Studierenden ausgewählt.</div>");
						out.println("<p><div class=mid><a href=\"javascript:window.history.back();\">zurück zur vorherigen Seite</a></div>");
						template.printTemplateFooter();
						return;
					}
				}
			}
		}

		File taskPath = new File(Configuration.getInstance().getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid());
		File path = new File(taskPath, String.valueOf(submission.getSubmissionid()));
		if (path.exists() == false) {
			path.mkdirs();
		}

		if (file != null) {
			LogEntry logEntry = new LogDAO(session).createLogUploadEntry(studentParticipation.getUser(), task, uploadFor > 0 ? LogAction.UPLOAD_ADMIN : LogAction.UPLOAD, null);
			File logPath = new File(taskPath, "logs" + System.getProperty("file.separator") + String.valueOf(logEntry.getId()));
			logPath.mkdirs();
			boolean skippedFiles = false;
			Vector<String> uploadedFilenames = new Vector<>();
			for (Part aFile : request.getParts()) {
				if (!aFile.getName().equalsIgnoreCase("file")) {
					continue;
				}
				StringBuffer submittedFileName = new StringBuffer(Util.getUploadFileName(aFile));
				Util.lowerCaseExtension(submittedFileName);
				String fileName = null;
				for (Pattern pattern : getTaskFileNamePatterns(task, uploadFor > 0)) {
					Matcher m = pattern.matcher(submittedFileName);
					if (!m.matches()) {
						LOG.debug("File does not match pattern: file;" + submittedFileName + ";" + pattern.pattern());
						skippedFiles = true;
						fileName = null;
						break;
					}
					fileName = m.group(1);
				}
				if (fileName == null) {
					continue;
				}
				try {
					if (!handleUploadedFile(LOG, path, task, fileName, aFile, Configuration.getInstance().getDefaultZipFileCharset())) {
						skippedFiles = true;
					}
					Util.copyInputStreamAndClose(aFile.getInputStream(), new File(logPath, fileName));
					uploadedFilenames.add(fileName);
				} catch (IOException | IllegalArgumentException e) {
					if (!submissionDAO.deleteIfNoFiles(submission, path)) {
						submission.setLastModified(ZonedDateTime.now());
					}
					LOG.error("Problem on processing uploaded file", e);
					Util.recursiveDeleteEmptyDirectories(logPath);
					if (!logPath.exists()) {
						session.remove(logEntry);
					} else {
						logEntry.setAdditionalData(Json.createObjectBuilder().add("filenames", Json.createArrayBuilder(uploadedFilenames)).build().toString());
					}
					tx.commit();
					template.printTemplateHeader("Fehler beim Upload", task);
					PrintWriter out = response.getWriter();
					out.println("<div class=mid>Problem beim Speichern der Daten.</div>");
					out.println("<p><div class=mid><a href=\"javascript:window.history.back();\">zurück zur vorherigen Seite</a></div>");
					template.printTemplateFooter();
					return;
				}
			}
			if (!submissionDAO.deleteIfNoFiles(submission, path)) {
				submission.setLastModified(ZonedDateTime.now());
			}
			if (!uploadedFilenames.isEmpty()) {
				logEntry.setAdditionalData(Json.createObjectBuilder().add("filenames", Json.createArrayBuilder(uploadedFilenames)).build().toString());
			} else {
				session.remove(logEntry);
				Util.recursiveDeleteEmptyDirectories(logPath);
			}
			tx.commit();

			if (skippedFiles) {
				template.printTemplateHeader("Nicht alle Dateien wurden verarbeitet.", task);
				PrintWriter out = response.getWriter();
				if (uploadedFilenames.isEmpty()) {
					out.println("Es konnte keine Datei verarbeitet werden, da der");
				} else {
					out.println("Nicht alle Dateien konnten verarbeitet werden, da mindestens ein");
				}
				out.println("Dateiname ungültig war bzw. nicht der Vorgabe entsprach (ist z.B. ein Klassenname vorgegeben, so muss die Datei genauso heißen, Tipp: Nur A-Z, a-z, 0-9, ., - und _ sind erlaubt. Evtl. muss der Dateiname mit einem Großbuchstaben beginnen und darf keine Leerzeichen enthalten).");
				out.println("<p><div class=mid><a href=\"javascript:window.history.back();\">zurück zur Abgabeseite</a></div>");
				out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
				template.printTemplateFooter();
				return;
			}

			response.addIntHeader("SID", submission.getSubmissionid());
			for (Test test : task.getTests()) {
				if (test instanceof UMLConstraintTest && test.getTimesRunnableByStudents() > 0) {
					response.addIntHeader("TID", test.getId());
					break;
				}
			}

			response.sendRedirect(Util.generateRedirectURL(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response));
			return;
		} else if (task.isSCMCTask()) {
			MCOptionDAOIf mcOptionDAO = DAOFactory.MCOptionDAOIf(session);
			List<MCOption> options = mcOptionDAO.getMCOptionsForTask(task);
			Collections.shuffle(options, new Random(studentParticipation.getId()));
			boolean allCorrect = true;
			List<Integer> resultIDs = new ArrayList<>();
			int i = 0;
			for (MCOption option : options) {
				if ((task.isMCTask() && request.getParameter("check" + i) != null) || (task.isSCMCTask() && String.valueOf(i).equals(request.getParameter("check")))) {
					resultIDs.add(option.getId());
					allCorrect &= option.isCorrect();
				} else {
					allCorrect &= !option.isCorrect();
				}
				++i;
			}
			Collections.sort(resultIDs);
			List<String> results = resultIDs.stream().map(String::valueOf).collect(Collectors.toList());
			DAOFactory.ResultDAOIf(session).createResults(submission, results);

			DAOFactory.PointsDAOIf(session).createMCPoints(allCorrect ? task.getMaxPoints() : 0, submission, "", task.getTaskGroup().getLecture().isRequiresAbhnahme() ? PointStatus.NICHT_ABGENOMMEN : PointStatus.ABGENOMMEN);

			submission.setLastModified(ZonedDateTime.now());
			new LogDAO(session).createLogUploadEntry(studentParticipation.getUser(), task, uploadFor > 0 ? LogAction.UPLOAD_ADMIN : LogAction.UPLOAD, Json.createObjectBuilder().add("mc", Json.createArrayBuilder(results)).build().toString());
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response));
		} else if (task.isClozeTask()) {
			ClozeTaskType clozeHelper = new ClozeTaskType(task.getDescription(), null, false, false);
			List<String> results = clozeHelper.parseResults(request);
			DAOFactory.ResultDAOIf(session).createResults(submission, results);
			if (clozeHelper.isAutoGradeAble()) {
				DAOFactory.PointsDAOIf(session).createMCPoints(clozeHelper.calculatePoints(results), submission, "", task.getTaskGroup().getLecture().isRequiresAbhnahme() ? PointStatus.NICHT_ABGENOMMEN : PointStatus.ABGENOMMEN);
			}
			submission.setLastModified(ZonedDateTime.now());
			new LogDAO(session).createLogUploadEntry(studentParticipation.getUser(), task, uploadFor > 0 ? LogAction.UPLOAD_ADMIN : LogAction.UPLOAD, Json.createObjectBuilder().add("cloze", Json.createArrayBuilder(results)).build().toString());
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response));
		} else if (request.getParameter("textsolution") != null) {
			LogEntry logEntry = new LogDAO(session).createLogUploadEntry(studentParticipation.getUser(), task, uploadFor > 0 ? LogAction.UPLOAD_ADMIN : LogAction.UPLOAD, null);
			File logPath = new File(taskPath, "logs" + System.getProperty("file.separator") + String.valueOf(logEntry.getId()));
			logPath.mkdirs();

			JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
			if (task.isADynamicTask()) {
				int numberOfFields = task.getDynamicTaskStrategie(session).getNumberOfResultFields();
				List<String> results = new ArrayList<>();
				for (int i = 0; i < numberOfFields; i++) {
					String result = request.getParameter("dynamicresult" + i);
					if (result == null) {
						result = "";
					}
					results.add(result);
				}
				DAOFactory.ResultDAOIf(session).createResults(submission, results);
				List<TaskNumber> taskNumbers = DAOFactory.TaskNumberDAOIf(session).assignTaskNumbersToSubmission(submission, studentParticipation);
				jsonBuilder.add("userResponses", Json.createArrayBuilder(results));
				JsonArrayBuilder taskNumbersArrayBuilder = Json.createArrayBuilder();
				for (TaskNumber tasknumber : taskNumbers) {
					JsonObjectBuilder taskNumberBuilder = Json.createObjectBuilder();
					taskNumberBuilder.add("number", tasknumber.getNumber());
					taskNumberBuilder.add("origNumber", tasknumber.getOrigNumber());
					taskNumbersArrayBuilder.add(taskNumberBuilder);
				}
				jsonBuilder.add("taskNumbers", taskNumbersArrayBuilder);
			}

			File uploadedFile = new File(path, task.getShowTextArea());
			try (FileWriter fileWriter = new FileWriter(uploadedFile)) {
				if (request.getParameter("textsolution") != null && request.getParameter("textsolution").length() <= task.getMaxsize()) {
					fileWriter.write(request.getParameter("textsolution"));
				}
			}

			Util.recursiveCopy(uploadedFile, new File(logPath, task.getShowTextArea()));
			logEntry.setAdditionalData(jsonBuilder.add("filename", uploadedFile.getName()).build().toString());

			submission.setLastModified(ZonedDateTime.now());
			tx.commit();
			response.sendRedirect(Util.generateRedirectURL(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response));
		} else {
			if (!submissionDAO.deleteIfNoFiles(submission, path)) {
				submission.setLastModified(ZonedDateTime.now());
			}
			LOG.error("Found no data on upload.");
			tx.commit();
			template.printTemplateHeader("Ungültige Anfrage", task);
			PrintWriter out = response.getWriter();
			out.println("<div class=mid>Problem: Keine Abgabedaten gefunden.</div>");
			out.println("<p><div class=mid><a href=\"javascript:window.history.back();\">zurück zur vorherigen Seite</a></div>");
			template.printTemplateFooter();
		}
	}

	public static Vector<Pattern> getTaskFileNamePatterns(Task task, boolean ignoreTaskPattern) {
		Vector<Pattern> patterns = new Vector<>(2);
		patterns.add(Pattern.compile(Configuration.GLOBAL_FILENAME_REGEXP));
		if (!(task.getFilenameRegexp() == null || task.getFilenameRegexp().isEmpty() || ignoreTaskPattern)) {
			patterns.add(Pattern.compile("^(" + task.getFilenameRegexp() + ")$"));
		}
		return patterns;
	}

	private static Vector<Pattern> getArchiveFileNamePatterns(Task task) {
		Vector<Pattern> patterns = new Vector<>(2);
		patterns.add(Pattern.compile(Configuration.GLOBAL_ARCHIVEFILENAME_REGEXP));
		if (task.getArchiveFilenameRegexp() != null && !task.getArchiveFilenameRegexp().isEmpty()) {
			if (task.getArchiveFilenameRegexp().startsWith("^")) {
				patterns.add(Pattern.compile("^(" + task.getArchiveFilenameRegexp().substring(1) + ")$"));
			} else {
				patterns.add(Pattern.compile("(?:^|.*/)(" + task.getArchiveFilenameRegexp() + ")$"));
			}
		}
		return patterns;
	}

	public static boolean handleUploadedFile(Logger log, File submissionPath, Task task, String fileName, Part item, Charset zipFileCharset) throws IOException {
		if (!"-".equals(task.getArchiveFilenameRegexp()) && (fileName.endsWith(".zip") || fileName.endsWith(".jar"))) {
			boolean skippedFiles = false;
			Vector<Pattern> patterns = getArchiveFileNamePatterns(task);
			try (ZipInputStream zipFile = new ZipInputStream(item.getInputStream(), zipFileCharset)) {
				ZipEntry entry = null;
				while ((entry = zipFile.getNextEntry()) != null) {
					if (entry.isDirectory()) {
						continue;
					}
					StringBuffer archivedFileName = new StringBuffer(entry.getName().replace("\\", "/"));
					boolean fileNameOk = true;
					for (Pattern pattern : patterns) {
						if (!pattern.matcher(archivedFileName).matches()) {
							log.debug("Ignored entry: " + archivedFileName + ";" + pattern.pattern());
							fileNameOk = false;
							break;
						}
					}
					if (!fileNameOk || archivedFileName.length() == 0 || archivedFileName.charAt(0) == '/' || archivedFileName.charAt(archivedFileName.length() - 1) == '/') {
						log.debug("Ignored entry: " + archivedFileName);
						skippedFiles = true;
						continue;
					}
					try {
						if (!new File(submissionPath, archivedFileName.toString()).getCanonicalPath().startsWith(submissionPath.getCanonicalPath())) {
							log.debug("Ignored entry: " + archivedFileName + "; tries to escape submissiondir");
							skippedFiles = true;
							continue;
						}
					} catch (IOException e) {
						// i.e. filename not valid on system
						continue;
					}
					if (!entry.getName().toLowerCase().endsWith(".class") && !entry.getName().startsWith("__MACOSX/")) {
						Util.lowerCaseExtension(archivedFileName);
						// TODO: relocate java-files from jar/zip archives?
						File fileToCreate = new File(submissionPath, archivedFileName.toString());
						if (!fileToCreate.getParentFile().exists()) {
							fileToCreate.getParentFile().mkdirs();
						}
						Util.copyInputStreamAndClose(zipFile, fileToCreate);
					}
				}
			}
			return !skippedFiles;
		}

		Util.saveAndRelocateJavaFile(item, submissionPath, fileName);
		return true;
	}
}
