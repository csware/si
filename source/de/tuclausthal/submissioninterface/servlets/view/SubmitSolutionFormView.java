/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for the submission of files
 * @author Sven Strickroth
 */
public class SubmitSolutionFormView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Task task = (Task) request.getAttribute("task");
		Participation participation = (Participation) request.getAttribute("participation");

		template.printTemplateHeader("Abgabe starten", task);

		Session session = HibernateSessionHelper.getSessionFactory().openSession();
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(task, new SessionAdapter(request).getUser(session));

		StringBuffer setWithUser = new StringBuffer();
		if (submission == null && participation.getGroup() != null) {
			setWithUser.append("<p>Haben Sie diese Aufgabe zusammen mit einem Partner gelöst? Dann bitte hier auswählen: <select name=partnerid size=1>");
			int cnt = 0;
			setWithUser.append("<option value=0>alleine bearbeitet</option>");
			for (Participation part : participation.getGroup().getMembers()) {
				if (part.getId() != participation.getId() && submissionDAO.getSubmission(task, part.getUser()) == null) {
					cnt++;
					setWithUser.append("<option value=" + part.getId() + ">" + Util.mknohtml(part.getUser().getFullName()) + "</option>");
				}
			}
			setWithUser.append("</select><p>");
			if (cnt == 0) {
				setWithUser = new StringBuffer();
			}
		}

		if (!"-".equals(task.getFilenameRegexp())) {
			out.println("<FORM class=mid ENCTYPE=\"multipart/form-data\" method=POST action=\"?taskid=" + task.getTaskid() + "\">");
			out.println(setWithUser.toString());
			out.println("<p>Bitte wählen Sie eine Datei aus, die Sie einsenden möchten.</p>");
			out.println("<INPUT TYPE=file NAME=file>");
			out.println("<INPUT TYPE=submit VALUE=upload>");
			out.println("</FORM>");
			if (task.isShowTextArea()) {
				out.println("<p><hr>");
			}
		}

		if (task.isShowTextArea() || "-".equals(task.getFilenameRegexp())) {
			out.println("<FORM class=mid method=POST action=\"?taskid=" + task.getTaskid() + "\">");
			out.println(setWithUser.toString());
			out.println("<p>Bitte füllen Sie das Textfeld mit Ihrer Lösung:</p>");
			out.println("<p><textarea cols=60 rows=10 name=textsolution>" + Util.mknohtml((String) request.getAttribute("textsolution")) + "</textarea></p>");
			out.println("<INPUT TYPE=submit VALUE=speichern>");
			out.println("</FORM>");
		}

		template.printTemplateFooter();
	}
}
