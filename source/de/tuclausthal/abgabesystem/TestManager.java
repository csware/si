package de.tuclausthal.abgabesystem;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.TaskDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.TestDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.JUnitTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.RegExpTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.persistence.datamodel.Test;
import de.tuclausthal.abgabesystem.util.Util;

public class TestManager extends HttpServlet {
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

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(mainbetternamereq.getUser(), task.getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.ADVISOR) {
			mainbetternamereq.template().printTemplateHeader("insufficient rights");
			mainbetternamereq.template().printTemplateFooter();
			return;
		}

		if ("newTest".equals(request.getParameter("action"))) {
			mainbetternamereq.template().printTemplateHeader("Test erstellen");
			out.println("<h2>RegExp. Test</h2>");
			out.println("<form action=\"" + response.encodeURL("?") + "\" method=post>");
			out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
			out.println("<input type=hidden name=action value=saveNewTest>");
			out.println("<input type=hidden name=type value=regexp>");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Main-Klasse:</th>");
			out.println("<td><input type=text name=mainclass></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>CommandLine Parameter:</th>");
			out.println("<td><input type=text name=parameter></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<th>Reg.Exp.:</th>");
			out.println("<td><input type=text name=regexp></td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
			out.println(response.encodeURL("/ba/servlets/ShowTask?taskid=" + task.getTaskid()));
			out.println("\">Abbrechen</a></td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");
			out.println("<p><h2>JUnit. Test</h2>");
			out.println("<form ENCTYPE=\"multipart/form-data\" action=\"" + response.encodeURL("?taskid=" + task.getTaskid() + "&amp;action=saveNewTest&type=junit") + "\" method=post>");
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>JUnit-Testcase:</th>");
			out.println("<td><INPUT TYPE=file NAME=testcase> (Doc. required here)</td>");
			out.println("</tr>");
			out.println("<tr>");
			out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
			out.println(response.encodeURL("/ba/servlets/ShowTask?taskid=" + task.getTaskid()));
			out.println("\">Abbrechen</a></td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</form>");
		} else if ("saveNewTest".equals(request.getParameter("action")) && "junit".equals(request.getParameter("type"))) {
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

				File path = new File("c:/abgabesystem/" + task.getLecture().getId() + "/" + task.getTaskid() + "/");
				if (path.exists() == false) {
					path.mkdirs();
				}
				// Process the uploaded items
				Iterator<FileItem> iter = items.iterator();
				while (iter.hasNext()) {
					FileItem item = iter.next();

					// Process a file upload
					if (!item.isFormField()) {
						if (!item.getName().endsWith(".jar")) {
							out.println("Filename invalid ;)");
							return;
						}
						File uploadedFile = new File(path, "junittest.jar");
						try {
							item.write(uploadedFile);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				TestDAOIf testDAO = DAOFactory.TestDAOIf();
				JUnitTest test = testDAO.createJUnitTest(task);
				testDAO.saveTest(test);
				// Race cond?
				task.setTest(test);
				taskDAO.saveTask(task);
				response.sendRedirect(response.encodeRedirectURL("/ba/servlets/TaskManager?action=editTask&lecture=" + task.getLecture().getId() + "&taskid=" + task.getTaskid()));
			} else {
				out.println("fuck111" + isMultipart);
			}
		} else if ("saveNewTest".equals(request.getParameter("action")) && "regexp".equals(request.getParameter("type"))) {
			//check regexp
			try {
				Pattern.compile(request.getParameter("regexp"));
			} catch (PatternSyntaxException e) {
				mainbetternamereq.template().printTemplateHeader("invalid pattern");
				mainbetternamereq.template().printTemplateFooter();
				return;
			}
			// store it
			TestDAOIf testDAO = DAOFactory.TestDAOIf();
			RegExpTest test = testDAO.createRegExpTest(task);
			test.setMainClass(request.getParameter("mainclass"));
			test.setCommandLineParameter(request.getParameter("parameter"));
			test.setRegularExpression(request.getParameter("regexp"));
			testDAO.saveTest(test);
			// Race cond?
			task.setTest(test);
			taskDAO.saveTask(task);
			response.sendRedirect(response.encodeRedirectURL("/ba/servlets/TaskManager?action=editTask&lecture=" + task.getLecture().getId() + "&taskid=" + task.getTaskid()));
		} else if ("deleteTest".equals(request.getParameter("action"))) {
			TestDAOIf testDAO = DAOFactory.TestDAOIf();
			Test test = task.getTest();
			task.setTest(null);
			taskDAO.saveTask(task);
			testDAO.deleteTest(test);
			response.sendRedirect(response.encodeRedirectURL("/ba/servlets/TaskManager?action=editTask&lecture=" + task.getLecture().getId() + "&taskid=" + task.getTaskid()));
			return;
		} else {
			mainbetternamereq.template().printTemplateHeader("Ungültiger Aufruf");
		}

		mainbetternamereq.template().printTemplateFooter();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
