/*
 * Copyright 2009 - 2012 Sven Strickroth <email@cs-ware.de>
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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

import org.apache.tomcat.util.http.fileupload.DiskFileUpload;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.hibernate.Session;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.NormalizerIf;
import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.StripCommentsNormalizer;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for performing a test (tutor or advisor)
 * @author Sven Strickroth
 */
public class PerformTest extends HttpServlet {
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
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("task", task);

		request.getRequestDispatcher("PerformTestTutorFormView").forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		Template template = TemplateFactory.getTemplate(request, response);

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			template.printTemplateHeader("Aufgabe nicht gefunden");
			PrintWriter out = response.getWriter();
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			template.printTemplateFooter();
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		//http://commons.apache.org/fileupload/using.html

		// Check that we have a file upload request
		boolean isMultipart = FileUpload.isMultipartContent(request);

		if (task.isShowTextArea() == false && "-".equals(task.getFilenameRegexp())) {
			template.printTemplateHeader("Ungültige Anfrage");
			PrintWriter out = response.getWriter();
			out.println("<div class=mid>Das Einsenden von Lösungen ist für diese Aufgabe deaktiviert.</div>");
			template.printTemplateFooter();
			return;
		}
		if ("-".equals(task.getFilenameRegexp())) {
			template.printTemplateHeader("Ungültige Anfrage");
			PrintWriter out = response.getWriter();
			out.println("<div class=mid>Dateiupload ist für diese Aufgabe deaktiviert.</div>");
			template.printTemplateFooter();
			return;
		}
		if (!isMultipart) {
			template.printTemplateHeader("Invalid request");
			template.printTemplateFooter();
			return;
		}

		// Create a new file upload handler
		FileUploadBase upload = new DiskFileUpload();

		List<FileItem> items = null;
		// Parse the request
		try {
			items = upload.parseRequest(request);
		} catch (FileUploadException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}

		int testId = -1;

		// Process the uploaded items
		Iterator<FileItem> iter = items.iterator();
		while (iter.hasNext()) {
			FileItem item = iter.next();
			if (item.isFormField() && "testid".equals(item.getFieldName())) {
				testId = Util.parseInteger(item.getString(), 0);
			}
		}

		File path = Util.createTemporaryDirectory("tutortest", null);
		if (path == null) {
			throw new IOException("Failed to create tempdir!");
		}
		if (path.exists() == false) {
			path.mkdirs();
		}

		// Process the uploaded items
		iter = items.iterator();
		while (iter.hasNext()) {
			FileItem item = iter.next();

			// Process a file upload
			if (!item.isFormField()) {
				Pattern pattern = Pattern.compile("^(?:.*?[\\\\/])?(" + task.getFilenameRegexp() + ")$");
				StringBuffer submittedFileName = new StringBuffer(item.getName());
				if (submittedFileName.lastIndexOf(".") > 0) {
					int lastDot = submittedFileName.lastIndexOf(".");
					submittedFileName.replace(lastDot, submittedFileName.length(), submittedFileName.subSequence(lastDot, submittedFileName.length()).toString().toLowerCase());
				}
				Matcher m = pattern.matcher(submittedFileName);
				if (!m.matches()) {
					template.printTemplateHeader("Ungültige Anfrage");
					PrintWriter out = response.getWriter();
					out.println("Dateiname ungültig bzw. entspricht nicht der Vorgabe (ist ein Klassenname vorgegeben, so muss die Datei genauso heißen).<br>Tipp: Nur A-Z, a-z, 0-9, ., - und _ sind erlaubt. Evtl. muss der Dateiname mit einem Großbuchstaben beginnen und darf keine Leerzeichen enthalten.");
					out.println("<br>Für Experten: Der Dateiname muss dem folgenden regulären Ausdruck genügen: " + Util.escapeHTML(pattern.pattern()));
					template.printTemplateFooter();
					return;
				}
				String fileName = m.group(1);
				if (!"-".equals(task.getArchiveFilenameRegexp()) && (fileName.endsWith(".zip") || fileName.endsWith(".jar"))) {
					ZipInputStream zipFile;
					Pattern archivePattern;
					if (task.getArchiveFilenameRegexp() == null || task.getArchiveFilenameRegexp().isEmpty()) {
						archivePattern = Pattern.compile("^(([/a-zA-Z0-9_ .-]*?/)?([a-zA-Z0-9_ .-]+))$");
					} else if (task.getArchiveFilenameRegexp().startsWith("^")) {
						archivePattern = Pattern.compile("^(" + task.getArchiveFilenameRegexp().substring(1) + ")$");
					} else {
						archivePattern = Pattern.compile("^(([/a-zA-Z0-9_ .-]*?/)?(" + task.getArchiveFilenameRegexp() + "))$");
					}
					try {
						zipFile = new ZipInputStream(item.getInputStream());
						ZipEntry entry = null;
						while ((entry = zipFile.getNextEntry()) != null) {
							if (entry.getName().contains("..") || entry.isDirectory()) {
								System.err.println("Ignored entry: " + entry.getName() + "; contains \"..\" or is directory");
								continue;
							}
							StringBuffer archivedFileName = new StringBuffer(entry.getName().replace("\\", "/"));
							if (!archivePattern.matcher(archivedFileName).matches()) {
								System.err.println("Ignored entry: " + archivedFileName + ";" + archivePattern.pattern());
								continue;
							}
							if (entry.isDirectory() == false && !entry.getName().toLowerCase().endsWith(".class")) {
								if (archivedFileName.lastIndexOf(".") > 0) {
									int lastDot = archivedFileName.lastIndexOf(".");
									archivedFileName.replace(lastDot, archivedFileName.length(), archivedFileName.subSequence(lastDot, archivedFileName.length()).toString().toLowerCase());
								}
								// TODO: relocate java-files from jar/zip archives?
								File fileToCreate = new File(path, archivedFileName.toString());
								if (!fileToCreate.getParentFile().exists()) {
									fileToCreate.getParentFile().mkdirs();
								}
								SubmitSolution.copyInputStream(zipFile, new BufferedOutputStream(new FileOutputStream(fileToCreate)));
							}
						}
						zipFile.close();
					} catch (IOException e) {
						System.err.println("SubmitSolutionProblem1");
						System.err.println(e.getMessage());
						e.printStackTrace();
						template.printTemplateHeader("Ungültige Anfrage");
						PrintWriter out = response.getWriter();
						out.println("Problem beim Entpacken des Archives.");
						template.printTemplateFooter();
						return;
					}
				} else {
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

				Test test = DAOFactory.TestDAOIf(session).getTest(testId);
				if (test == null) {
					template.printTemplateHeader("Ungültige Anfrage");
					PrintWriter out = response.getWriter();
					out.println("Test nicht gefunden.");
					template.printTemplateFooter();
					return;
				}

				request.setAttribute("task", test.getTask());
				request.setAttribute("test", test);

				ContextAdapter contextAdapter = new ContextAdapter(getServletContext());

				TestTask testTask = new TestTask(test);
				TestExecutorTestResult testResult = new TestExecutorTestResult();
				testTask.performTaskInFolder(test, contextAdapter.getDataPath(), path, testResult);

				Util.recursiveDelete(path);
				
				request.setAttribute("testresult", testResult);
				request.getRequestDispatcher("PerformTestResultView").forward(request, response);
				return;
			}
		}
		System.err.println("SubmitSolutionProblem3");
		System.err.println("Problem: Keine Abgabedaten gefunden.");
		PrintWriter out = response.getWriter();
		out.println("Problem: Keine Abgabedaten gefunden.");
	}
}
