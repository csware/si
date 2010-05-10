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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.dupecheck.normalizers.NormalizerIf;
import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.StripCommentsNormalizer;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.impl.LogDAO;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry.LogAction;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for the submission of files
 * @author Sven Strickroth
 */
public class SubmitSolution extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = HibernateSessionHelper.getSession();
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(session), task.getLecture());
		if (participation == null) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.getStart().after(Util.correctTimezone(new Date())) && participation.getRoleType().compareTo(ParticipationRole.ADVISOR) != 0) {
			request.setAttribute("title", "Abgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		if (task.getDeadline().before(Util.correctTimezone(new Date())) && participation.getRoleType().compareTo(ParticipationRole.ADVISOR) != 0) {
			request.setAttribute("title", "Abgabe nicht mehr möglich");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		request.setAttribute("task", task);

		if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) >= 0) {
			request.getRequestDispatcher("SubmitSolutionAdvisorFormView").forward(request, response);
		} else {
			request.setAttribute("participation", participation);

			if (task.isShowTextArea()) {
				String textsolution = "";
				Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(task, new SessionAdapter(request).getUser(session));
				if (submission != null) {
					ContextAdapter contextAdapter = new ContextAdapter(getServletContext());
					File textSolutionFile = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator") + "textloesung.txt");
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
		Session session = HibernateSessionHelper.getSession();
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

		Participation studentParticipation = participationDAO.getParticipation(new SessionAdapter(request).getUser(session), task.getLecture());
		if (studentParticipation == null) {
			template.printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid>Sie sind kein Teilnehmer dieser Veranstaltung.</div>");
			out.println("<div class=mid><a href=\"" + response.encodeURL("Overview") + "\">zur Übersicht</a></div>");
			template.printTemplateFooter();
			return;
		}

		//http://commons.apache.org/fileupload/using.html

		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		int partnerID = 0;
		int uploadFor = 0;
		List<FileItem> items = null;
		if (!isMultipart) {
			partnerID = Util.parseInteger(request.getParameter("partnerid"), 0);
		} else {
			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();

			// Set factory constraints
			//factory.setSizeThreshold(yourMaxMemorySize);
			//factory.setRepository(yourTempDirectory);

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse the request
			try {
				items = upload.parseRequest(request);
			} catch (FileUploadException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "filename invalid");
				return;
			}

			// Process the uploaded items
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = iter.next();
				if (item.isFormField() && "partnerid".equals(item.getFieldName())) {
					partnerID = Util.parseInteger(item.getString(), 0);
				} else if (item.isFormField() && "uploadFor".equals(item.getFieldName())) {
					uploadFor = Util.parseInteger(item.getString(), 0);
				}
			}
		}

		if (uploadFor > 0) {
			// Uploader ist wahrscheinlich Betreuer -> keine zeitlichen Prüfungen
			if (studentParticipation.getRoleType().compareTo(ParticipationRole.ADVISOR) != 0) {
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Sie sind kein Betreuer dieser Veranstaltung.</div>");
				out.println("<div class=mid><a href=\"" + response.encodeURL("Overview") + "\">zur Übersicht</a></div>");
				template.printTemplateFooter();
				return;
			}
			studentParticipation = participationDAO.getParticipation(uploadFor);
			if (studentParticipation == null || studentParticipation.getLecture().getId() != task.getLecture().getId()) {
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Der gewählte Student ist kein Teilnehmer dieser Veranstaltung.</div>");
				out.println("<div class=mid><a href=\"" + response.encodeURL("Overview") + "\">zur Übersicht</a></div>");
				template.printTemplateFooter();
				return;
			}
		} else {
			// Uploader ist Student, -> harte prüfung
			if (task.getStart().after(Util.correctTimezone(new Date()))) {
				template.printTemplateHeader("Aufgabe nicht abrufbar");
				out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
				template.printTemplateFooter();
				return;
			}

			if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
				template.printTemplateHeader("Abgabe nicht (mehr) möglich");
				out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
				template.printTemplateFooter();
				return;
			}

			if (!isMultipart && "-".equals(task.getFilenameRegexp())) {
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Dateiupload ist für diese Aufgabe deaktiviert.</div>");
				template.printTemplateFooter();
				return;
			}
		}

		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);

		Transaction tx = session.beginTransaction();
		Submission submission = submissionDAO.createSubmission(task, studentParticipation);

		if (partnerID > 0) {
			Participation partnerParticipation = participationDAO.getParticipation(partnerID);
			if (submission.getSubmitters().size() < 2 && partnerParticipation != null && partnerParticipation.getLecture().getId() == task.getLecture().getId() && submissionDAO.getSubmissionLocked(task, partnerParticipation.getUser()) == null) {
				submission.getSubmitters().add(partnerParticipation);
				session.update(submission);
			} else {
				tx.rollback();
				template.printTemplateHeader("Ungültige Anfrage");
				out.println("<div class=mid>Der ausgewählte Partner hat bereits eine eigene Abgabe initiiert oder Sie haben bereits einen Partner ausgewählt.</div>");
				template.printTemplateFooter();
				return;
			}
		}

		ContextAdapter contextAdapter = new ContextAdapter(getServletContext());

		File path = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
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
						Pattern pattern = Pattern.compile("([\\/a-zA-Z0-9_.-]+)$");
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
							Util.recursiveDeleteEmptySubDirectories(path);
							tx.rollback();
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
							pattern = Pattern.compile(".*?(?:\\\\|/)?([a-zA-Z0-9_.-]+)$");
						} else {
							pattern = Pattern.compile(".*?(?:\\\\|/)?(" + task.getFilenameRegexp() + ")$");
						}
						StringBuffer submittedFileName = new StringBuffer(item.getName());
						if (submittedFileName.lastIndexOf(".") > 0) {
							int lastDot = submittedFileName.lastIndexOf(".");
							submittedFileName.replace(lastDot, submittedFileName.length(), submittedFileName.subSequence(lastDot, submittedFileName.length()).toString().toLowerCase());
						}
						Matcher m = pattern.matcher(submittedFileName);
						if (!m.matches()) {
							tx.rollback();
							template.printTemplateHeader("Ungültige Anfrage");
							out.println("Dateiname ungültig bzw. entspricht nicht der Vorgabe (ist ein Klassenname vorgegeben, so muss die Datei genauso heißen).<br>Tipp: Nur A-Z, a-z, 0-9, ., - und _ sind erlaubt.");
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
							if (packageMatcher.matches()) {
								String packageName = packageMatcher.group(1).replace(".", System.getProperty("file.separator"));
								File packageDirectory = new File(path, packageName);
								packageDirectory.mkdirs();
								uploadedFile.renameTo(new File(packageDirectory, fileName));
							} else {
								uploadedFile.renameTo(new File(path, fileName));
							}
						}
					}
					session.update(submission);
					Util.recursiveDeleteEmptyDirectories(path);
					tx.commit();
					new LogDAO(session).createLogEntry(studentParticipation.getUser(), null, task, LogAction.UPLOAD, null, null);
					response.sendRedirect(response.encodeRedirectURL("ShowTask?taskid=" + task.getTaskid()));
					return;
				}
			}
			tx.rollback();
			out.println("Problem: Keine Abgabedaten gefunden.");
		} else if (request.getParameter("textsolution") != null && task.isShowTextArea()) {
			File uploadedFile = new File(path, "textloesung.txt");
			FileWriter fileWriter = new FileWriter(uploadedFile);
			fileWriter.write(request.getParameter("textsolution"));
			fileWriter.flush();
			fileWriter.close();

			session.update(submission);
			tx.commit();

			response.sendRedirect(response.encodeRedirectURL("ShowTask?taskid=" + task.getTaskid()));
		} else {
			tx.rollback();
			out.println("Problem: Keine Abgabedaten gefunden.");
		}
	}

	public static final void copyInputStream(ZipInputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.closeEntry();
		out.close();
	}
}
