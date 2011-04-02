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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.DiskFileUpload;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.NormalizerIf;
import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.StripCommentsNormalizer;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.impl.LogDAO;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry.LogAction;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for the submission of files
 * @author Sven Strickroth
 */
public class SubmitSolution extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		boolean canUploadForStudents = participation.getRoleType() == ParticipationRole.ADVISOR || (task.isTutorsCanUploadFiles() && participation.getRoleType() == ParticipationRole.TUTOR);

		// if session-user is not a tutor (with rights to upload for students) or advisor: check dates
		if (!canUploadForStudents) {
			if (participation.getRoleType() == ParticipationRole.TUTOR) {
				request.setAttribute("title", "Tutoren können keine eigenen Lösungen einsenden.");
				request.getRequestDispatcher("MessageView").forward(request, response);
				return;
			}
			if (task.getStart().after(Util.correctTimezone(new Date()))) {
				request.setAttribute("title", "Abgabe nicht gefunden");
				request.getRequestDispatcher("MessageView").forward(request, response);
				return;
			}
			if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
				request.setAttribute("title", "Abgabe nicht mehr möglich");
				request.getRequestDispatcher("MessageView").forward(request, response);
				return;
			}
		}

		if (task.isShowTextArea() == false && "-".equals(task.getFilenameRegexp())) {
			request.setAttribute("title", "Das Einsenden von Lösungen ist für diese Aufgabe deaktiviert.");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		request.setAttribute("task", task);

		if (canUploadForStudents) {
			request.getRequestDispatcher("SubmitSolutionAdvisorFormView").forward(request, response);
		} else {
			request.setAttribute("participation", participation);

			if (task.isShowTextArea()) {
				String textsolution = "";
				Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(task, RequestAdapter.getUser(request));
				if (submission != null) {
					ContextAdapter contextAdapter = new ContextAdapter(getServletContext());
					File textSolutionFile = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator") + "textloesung.txt");
					if (textSolutionFile.exists()) {
						BufferedReader bufferedReader = new BufferedReader(new FileReader(textSolutionFile));
						StringBuffer sb = new StringBuffer();
						String line;
						while ((line = bufferedReader.readLine()) != null) {
							sb.append(line);
							sb.append(System.getProperty("line.separator"));
						}
						textsolution = sb.toString();
						bufferedReader.close();
					}
				}
				request.setAttribute("textsolution", textsolution);
			}
			request.getRequestDispatcher("SubmitSolutionFormView").forward(request, response);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			template.printTemplateHeader("Aufgabe nicht gefunden");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			template.printTemplateFooter();
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);

		Participation studentParticipation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (studentParticipation == null) {
			template.printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid>Sie sind kein Teilnehmer dieser Veranstaltung.</div>");
			out.println("<div class=mid><a href=\"" + response.encodeURL("Overview") + "\">zur Übersicht</a></div>");
			template.printTemplateFooter();
			return;
		}

		//http://commons.apache.org/fileupload/using.html

		// Check that we have a file upload request
		boolean isMultipart = FileUpload.isMultipartContent(request);

		List<Integer> partnerIDs = new LinkedList<Integer>();
		int uploadFor = 0;
		List<FileItem> items = null;
		if (!isMultipart) {
			for (String partnerIdParameter : request.getParameterValues("partnerid")) {
				int partnerID = Util.parseInteger(partnerIdParameter, 0);
				if (partnerID > 0) {
					partnerIDs.add(partnerID);
				}
			}
		} else {
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
				if (item.isFormField() && "partnerid".equals(item.getFieldName())) {
					int partnerID = Util.parseInteger(item.getString(), 0);
					if (partnerID > 0) {
						partnerIDs.add(partnerID);
					}
				} else if (item.isFormField() && "uploadFor".equals(item.getFieldName())) {
					uploadFor = Util.parseInteger(item.getString(), 0);
				}
			}
		}

		if (uploadFor > 0) {
			// Uploader ist wahrscheinlich Betreuer -> keine zeitlichen Prüfungen
			// check if uploader is allowed to upload for students
			if (!(studentParticipation.getRoleType() == ParticipationRole.ADVISOR || (task.isTutorsCanUploadFiles() && studentParticipation.getRoleType() == ParticipationRole.TUTOR))) {
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Sie sind nicht berechtigt bei dieser Veranstaltung Dateien für Studenten hochzuladen.</div>");
				out.println("<div class=mid><a href=\"" + response.encodeURL("Overview") + "\">zur Übersicht</a></div>");
				template.printTemplateFooter();
				return;
			}
			studentParticipation = participationDAO.getParticipation(uploadFor);
			if (studentParticipation == null || studentParticipation.getLecture().getId() != task.getTaskGroup().getLecture().getId()) {
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Der gewählte Student ist kein Teilnehmer dieser Veranstaltung.</div>");
				out.println("<div class=mid><a href=\"" + response.encodeURL("Overview") + "\">zur Übersicht</a></div>");
				template.printTemplateFooter();
				return;
			}
			if (task.isShowTextArea() == false && "-".equals(task.getFilenameRegexp())) {
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Das Einsenden von Lösungen ist für diese Aufgabe deaktiviert.</div>");
				template.printTemplateFooter();
				return;
			}
		} else {
			if (studentParticipation.getRoleType() == ParticipationRole.ADVISOR || studentParticipation.getRoleType() == ParticipationRole.TUTOR) {
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Betreuer und Tutoren können keine eigenen Lösungen einsenden.</div>");
				template.printTemplateFooter();
				return;
			}
			// Uploader is Student, -> hard date checks
			if (task.getStart().after(Util.correctTimezone(new Date()))) {
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Abgabe nicht gefunden.</div>");
				template.printTemplateFooter();
				return;
			}
			if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Abgabe nicht mehr möglich.</div>");
				template.printTemplateFooter();
				return;
			}
			if (isMultipart && "-".equals(task.getFilenameRegexp())) {
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Dateiupload ist für diese Aufgabe deaktiviert.</div>");
				template.printTemplateFooter();
				return;
			} else if (!isMultipart && !task.isShowTextArea()) {
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Textlösungen sind für diese Aufgabe deaktiviert.</div>");
				template.printTemplateFooter();
				return;
			}
		}

		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);

		Transaction tx = session.beginTransaction();
		Submission submission = submissionDAO.createSubmission(task, studentParticipation);

		for (int partnerID : partnerIDs) {
			Participation partnerParticipation = participationDAO.getParticipation(partnerID);
			if (submission.getSubmitters().size() < task.getMaxSubmitters() && partnerParticipation != null && partnerParticipation.getLecture().getId() == task.getTaskGroup().getLecture().getId() && submissionDAO.getSubmissionLocked(task, partnerParticipation.getUser()) == null) {
				submission.getSubmitters().add(partnerParticipation);
				session.update(submission);
			} else {
				tx.rollback();
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Ein ausgewählter Partner hat bereits eine eigene Abgabe initiiert oder Sie haben bereits die maximale Anzahl von Partnern ausgewählt.</div>");
				template.printTemplateFooter();
				return;
			}
		}

		ContextAdapter contextAdapter = new ContextAdapter(getServletContext());

		File path = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
		if (path.exists() == false) {
			path.mkdirs();
		}

		if (isMultipart) {
			// Process the uploaded items
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = iter.next();

				// Process a file upload
				if (!item.isFormField()) {
					if (item.getName().toLowerCase().endsWith(".zip") || item.getName().toLowerCase().endsWith(".jar")) {
						ZipInputStream zipFile;
						// TODO: relocate java-files from jar/zip archives?
						Pattern pattern = Pattern.compile("^([\\/a-zA-Z0-9_ .-]+)$");
						try {
							zipFile = new ZipInputStream(item.getInputStream());
							ZipEntry entry = null;
							while ((entry = zipFile.getNextEntry()) != null) {
								if (entry.getName().contains("..")) {
									continue;
								}
								StringBuffer submittedFileName = new StringBuffer(entry.getName());
								if (!pattern.matcher(submittedFileName).matches()) {
									System.out.println("Ignored entry: " + submittedFileName);
									continue;
								}
								if (entry.isDirectory() == false && !entry.getName().toLowerCase().endsWith(".class")) {
									if (submittedFileName.lastIndexOf(".") > 0) {
										int lastDot = submittedFileName.lastIndexOf(".");
										submittedFileName.replace(lastDot, submittedFileName.length(), submittedFileName.subSequence(lastDot, submittedFileName.length()).toString().toLowerCase());
									}
									File fileToCreate = new File(path, submittedFileName.toString());
									if (!fileToCreate.getParentFile().exists()) {
										fileToCreate.getParentFile().mkdirs();
									}
									copyInputStream(zipFile, new BufferedOutputStream(new FileOutputStream(fileToCreate)));
								}
							}
							zipFile.close();
						} catch (IOException e) {
							if (!submissionDAO.deleteIfNoFiles(submission, path)) {
								submission.setLastModified(new Date());
								submissionDAO.saveSubmission(submission);
							}
							System.out.println("SubmitSolutionProblem1");
							tx.commit();
							System.out.println(e.getMessage());
							e.printStackTrace();
							template.printTemplateHeader("Ungültige Anfrage");
							out.println("Problem beim entpacken der .zip-Datei.");
							template.printTemplateFooter();
							return;
						}
					} else {
						Pattern pattern;
						if (task.getFilenameRegexp() == null || task.getFilenameRegexp().isEmpty() || uploadFor > 0) {
							pattern = Pattern.compile("^(?:.*?\\\\|/)?([a-zA-Z0-9_.-]+)$");
						} else {
							pattern = Pattern.compile("^(?:.*?\\\\|/)?(" + task.getFilenameRegexp() + ")$");
						}
						StringBuffer submittedFileName = new StringBuffer(item.getName());
						if (submittedFileName.lastIndexOf(".") > 0) {
							int lastDot = submittedFileName.lastIndexOf(".");
							submittedFileName.replace(lastDot, submittedFileName.length(), submittedFileName.subSequence(lastDot, submittedFileName.length()).toString().toLowerCase());
						}
						Matcher m = pattern.matcher(submittedFileName);
						if (!m.matches()) {
							if (!submissionDAO.deleteIfNoFiles(submission, path)) {
								submission.setLastModified(new Date());
								submissionDAO.saveSubmission(submission);
							}
							System.out.println("SubmitSolutionProblem2: " + item.getName() + ";" + submittedFileName + ";" + pattern.pattern());
							tx.commit();
							template.printTemplateHeader("Ungültige Anfrage");
							out.println("Dateiname ungültig bzw. entspricht nicht der Vorgabe (ist ein Klassenname vorgegeben, so muss die Datei genauso heißen).<br>Tipp: Nur A-Z, a-z, 0-9, ., - und _ sind erlaubt. Evtl. muss der Dateiname mit einem Großbuchstaben beginnen und darf keine Leerzeichen enthalten.");
							if (uploadFor > 0) {
								out.println("<br>Für Experten: Der Dateiname muss dem folgenden regulären Ausdruck genügen: " + Util.mknohtml(pattern.pattern()));
							}
							template.printTemplateFooter();
							return;
						}
						String fileName = m.group(1);

						File uploadedFile = new File(path, fileName);
						// handle .java-files differently in order to extract package and move it to the correct folder
						if (fileName.toLowerCase().endsWith(".java")) {
							uploadedFile = File.createTempFile("upload", null, path);
						}
						try {
							item.write(uploadedFile);
						} catch (Exception e) {
							e.printStackTrace();
						}
						// extract defined package in java-files
						if (fileName.toLowerCase().endsWith(".java")) {
							NormalizerIf stripComments = new StripCommentsNormalizer();
							StringBuffer javaFileContents = stripComments.normalize(Util.loadFile(uploadedFile));
							Pattern packagePattern = Pattern.compile(".*package\\s+([a-zA-Z$]([a-zA-Z0-9_$]|\\.[a-zA-Z0-9_$])*)\\s*;.*", Pattern.DOTALL);
							Matcher packageMatcher = packagePattern.matcher(javaFileContents);
							File destFile = new File(path, fileName);
							if (packageMatcher.matches()) {
								String packageName = packageMatcher.group(1).replace(".", System.getProperty("file.separator"));
								File packageDirectory = new File(path, packageName);
								packageDirectory.mkdirs();
								destFile = new File(packageDirectory, fileName);
							}
							if (destFile.exists() && destFile.isFile()) {
								destFile.delete();
							}
							uploadedFile.renameTo(destFile);
						}
					}
					if (!submissionDAO.deleteIfNoFiles(submission, path)) {
						submission.setLastModified(new Date());
						submissionDAO.saveSubmission(submission);
					}
					tx.commit();
					new LogDAO(session).createLogEntry(studentParticipation.getUser(), null, task, LogAction.UPLOAD, null, null);
					response.sendRedirect(response.encodeRedirectURL("ShowTask?taskid=" + task.getTaskid()));
					return;
				}
			}
			if (!submissionDAO.deleteIfNoFiles(submission, path)) {
				submission.setLastModified(new Date());
				submissionDAO.saveSubmission(submission);
			}
			System.out.println("SubmitSolutionProblem3");
			System.out.println("Problem: Keine Abgabedaten gefunden.");
			tx.commit();
			out.println("Problem: Keine Abgabedaten gefunden.");
		} else if (request.getParameter("textsolution") != null) {
			File uploadedFile = new File(path, "textloesung.txt");
			FileWriter fileWriter = new FileWriter(uploadedFile);
			fileWriter.write(request.getParameter("textsolution"));
			fileWriter.flush();
			fileWriter.close();

			submission.setLastModified(new Date());
			submissionDAO.saveSubmission(submission);
			tx.commit();

			response.sendRedirect(response.encodeRedirectURL("ShowTask?taskid=" + task.getTaskid()));
		} else {
			if (!submissionDAO.deleteIfNoFiles(submission, path)) {
				submission.setLastModified(new Date());
				submissionDAO.saveSubmission(submission);
			}
			System.out.println("SubmitSolutionProblem4");
			tx.commit();
			out.println("Problem: Keine Abgabedaten gefunden.");
		}
	}

	public static final void copyInputStream(ZipInputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}

		in.closeEntry();
		out.close();
	}
}
