package de.tuclausthal.abgabesystem;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.TaskDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.TestResultDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.JUnitTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.RegExpTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.persistence.datamodel.TestResult;
import de.tuclausthal.abgabesystem.util.Util;

public class SubmitSolution extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf();
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			mainbetternamereq.template().printTemplateHeader("Aufgabe nicht gefunden");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), task.getLecture());
		if (participation == null) {
			mainbetternamereq.template().printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid>Sie sind kein Teilnehmer dieser Veranstaltung.</div>");
			out.println("<div class=mid><a href=\"" + response.encodeURL("/ba/servlets/Overview") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if (task.getStart().after(new Date()) && participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			mainbetternamereq.template().printTemplateHeader("Aufgabe nicht abrufbar");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if (task.getDeadline().before(new Date()) || participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
			mainbetternamereq.template().printTemplateHeader("Abgabe nicht (mehr) möglich");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		mainbetternamereq.template().printTemplateHeader("Aufgabe \"" + Util.mknohtml(task.getTitle()) + "\"");

		if (task.getDeadline().before(new Date())) {
			out.println("Keine Abgabe mehr möglich");
		} else {
			SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf();
			Submission submission = submissionDAO.getSubmission(task, mainbetternamereq.getUser());
			if (submission != null) {
				out.println("Infos zu meiner bisherigen Abgabe");
			}
			out.println("<FORM ENCTYPE=\"multipart/form-data\" method=POST action=\"?taskid=" + task.getTaskid() + "\">");
			out.println("<INPUT TYPE=file NAME=file>");
			out.println("<INPUT TYPE=submit VALUE=upload>");
			out.println("</FORM>");
		}

		mainbetternamereq.template().printTemplateFooter();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		MainBetterNameHereRequired mainbetternamereq = new MainBetterNameHereRequired(request, response);

		PrintWriter out = response.getWriter();

		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf();
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			mainbetternamereq.template().printTemplateHeader("Aufgabe nicht gefunden");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), task.getLecture());
		if (participation == null) {
			mainbetternamereq.template().printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid>Sie sind kein Teilnehmer dieser Veranstaltung.</div>");
			out.println("<div class=mid><a href=\"" + response.encodeURL("/ba/servlets/Overview") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if (task.getStart().after(new Date()) && participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			mainbetternamereq.template().printTemplateHeader("Aufgabe nicht abrufbar");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if (task.getDeadline().before(new Date()) || participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
			mainbetternamereq.template().printTemplateHeader("Abgabe nicht (mehr) möglich");
			out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		//mainbetternamereq.template().printTemplateHeader("Upload");

		//http://commons.apache.org/fileupload/using.html

		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		if (isMultipart) {

			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();

			// Set factory constraints
			//factory.setSizeThreshold(yourMaxMemorySize);
			//factory.setRepository(yourTempDirectory);

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse the request
			List<FileItem> items = null;
			try {
				items = upload.parseRequest(request);
			} catch (FileUploadException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "filename invalid");
				return;
			}

			SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf();
			Submission submission = submissionDAO.createSubmission(task, participation);

			File path = new File("c:/abgabesystem/" + task.getLecture().getId() + "/" + task.getTaskid() + "/" + submission.getSubmissionid() + "/");
			if (path.exists() == false) {
				path.mkdirs();
			}
			// Process the uploaded items
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = iter.next();

				// Process a file upload
				if (!item.isFormField()) {
					Pattern pattern = Pattern.compile("(\\|/)?([A-Z][A-Za-z0-9_]+\\.java)$");
					Matcher m = pattern.matcher(item.getName());
					if (!m.matches()) {
						out.println("Filename invalid ;)");
						return;
					}
					String fileName = m.group(0);
					File uploadedFile = new File(path, fileName);
					try {
						item.write(uploadedFile);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// compile test
					// http://forums.java.net/jive/message.jspa?messageID=325269
					JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
					/*StandardJavaFileManager sjfm = jc.getStandardFileManager(null, null, null);
					Iterable codeObjecten = sjfm.getJavaFileObjects(uploadedFile);
					jc.getTask(null, sjfm, null, null, null, codeObjecten).call();
					sjfm.close();*/
					try {
						ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();

						List<String> javaFiles = new LinkedList<String>();
						for (File javaFile : path.getAbsoluteFile().listFiles()) {
							if (javaFile.getName().endsWith(".java")) {
								javaFiles.add(javaFile.getAbsolutePath());
							}
						}
						int a = jc.run(null, null, errorOutputStream, javaFiles.toArray(new String[] {}));
						submission.setCompiles(a == 0);
						submission.setStderr(errorOutputStream.toString());
					} catch (Exception e) {
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "System.getProperty(\"java.home\") should point to a jre in a jdk directory");
						return;
					}

					// Test exists
					if (task.getTest() != null) {
						// check what kind of test it is
						List<String> params = new LinkedList<String>();
						params.add("java");
						//params.add("-Djava.security.manager");
						//params.add("-Djava.security.policy=myPol.policy");
						if (task.getTest() instanceof JUnitTest) {
							// TODO: only win atm
							params.add("-cp");
							params.add("..\\junittest.jar;c:\\junit.jar;.");
							params.add("junit.textui.TestRunner");
							params.add("AllTests");
						} else if (task.getTest() instanceof RegExpTest) {
							RegExpTest regExpTest = (RegExpTest) task.getTest();
							params.add(regExpTest.getMainClass());
							params.addAll(Arrays.asList(regExpTest.getCommandLineParameter().split(" ")));
						} else {
							// TODO: throw error!
						}
						ProcessBuilder pb = new ProcessBuilder(params);
						pb.directory(path);
						Process process = pb.start();
						CheckThread checkTread = new CheckThread(process);
						checkTread.run();
						int exitValue = -1;
						try {
							exitValue = process.waitFor();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
						}
						BufferedReader testErrorInputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
						TestResultDAOIf testResultDAO = DAOFactory.TestResultDAOIf();
						TestResult testResult = testResultDAO.createTestResult(submission);
						testResult.setPassedTest((exitValue == 0));
						String testError = "";
						String line;
						while ((line = testErrorInputStream.readLine()) != null) {
							testError = testError.concat(line + "\n");
						}
						if (task.getTest() instanceof RegExpTest) {
							System.out.println(((RegExpTest) task.getTest()).getRegularExpression());
							System.out.println(testError);
							Pattern testPattern = Pattern.compile(((RegExpTest) task.getTest()).getRegularExpression());
							Matcher testMatcher = testPattern.matcher(testError.trim());
							if (!testMatcher.matches()) {
								testError = "regexp doesn't match. Output follows:\n" + testError;
								testResult.setPassedTest(false);
							}
						}
						testResult.setTestOutput(testError);
					}

					submissionDAO.saveSubmission(submission);
					response.sendRedirect(response.encodeRedirectURL("/ba/servlets/ShowTask?taskid=" + task.getTaskid()));
					out.close();
				}
			}
		} else {
			out.println("fuck111" + isMultipart);
		}
	}
}

class HTMLFilter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		return (name.endsWith(".java"));
	}
}
