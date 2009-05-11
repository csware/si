package de.tuclausthal.abgabesystem;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import de.tuclausthal.abgabesystem.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.abgabesystem.persistence.dao.TaskDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.util.CheckSubmission;
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
			out.println("<div class=mid><a href=\"" + response.encodeURL("Overview") + "\">zur Übersicht</a></div>");
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
			out.println("<FORM class=mid ENCTYPE=\"multipart/form-data\" method=POST action=\"?taskid=" + task.getTaskid() + "\">");
			out.println("<p>Bitte wählen Sie eine .java-Quellcode-Datei aus, die Sie einsenden möchten.</p>");
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
			out.println("<div class=mid><a href=\"" + response.encodeURL("Overview") + "\">zur Übersicht</a></div>");
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

			ContextAdapter contextAdapter = new ContextAdapter(getServletContext());

			File path = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
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

					CheckSubmission checkSubmission = new CheckSubmission(submission, contextAdapter.getDataPath());
					checkSubmission.checkTest(response);

					response.sendRedirect(response.encodeRedirectURL("ShowTask?taskid=" + task.getTaskid()));
				}
			}
		} else {
			out.println("fuck111" + isMultipart);
		}
	}
}
