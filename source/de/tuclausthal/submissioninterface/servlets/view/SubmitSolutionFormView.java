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

package de.tuclausthal.submissioninterface.servlets.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
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

		Session session = RequestAdapter.getSession(request);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(task, RequestAdapter.getUser(request));

		StringBuffer setWithUser = new StringBuffer();
		if (task.getMaxSubmitters() > 1) {
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
					setWithUser = new StringBuffer("<p>Sie können im Moment keinen Partner für Ihre Abgabe auswählen. Um dies zu erreichen müssen Sie zwei Voraussetzungen erfüllen:<ol><li>Ihr Partner muss sich auch (mindestens) einmal an diesem System angemeldet haben</li><li>Sie, als auch Ihr Partner, müssen von Ihrem Tutor in die gleiche Übungsgruppe aufgenommen worden sein.</li></ol></p><hr>");
				}
			} else if (submission == null && participation.getGroup() == null) {
				setWithUser = new StringBuffer("<p>Sie können im Moment keinen Partner für Ihre Abgabe auswählen. Um dies zu erreichen müssen Sie zwei Voraussetzungen erfüllen:<ol><li>Ihr Partner muss sich auch (mindestens) einmal an diesem System angemeldet haben</li><li>Sie, als auch Ihr Partner, müssen von Ihrem Tutor in die gleiche Übungsgruppe aufgenommen worden sein.</li></ol></p><hr>");
			}
		}

		if (!"-".equals(task.getFilenameRegexp())) {
			out.println("<FORM class=mid ENCTYPE=\"multipart/form-data\" method=POST action=\"" + response.encodeURL("?taskid=" + task.getTaskid()) + "\">");
			out.println(setWithUser.toString());
			out.println("<p>Bitte wählen Sie eine Datei aus, die Sie einsenden möchten.</p>");
			out.println("<INPUT TYPE=file NAME=file>");
			out.println("<INPUT TYPE=submit VALUE=upload>");
			out.println("<p>Hinweis: Bestehende Dateien werde überschrieben.</p>");
			out.println("</FORM>");
			if (task.isShowTextArea()) {
				out.println("<p><hr>");
			}
		}

		if (task.isShowTextArea()) {
			out.println("<FORM class=mid method=POST action=\"" + response.encodeURL("?taskid=" + task.getTaskid()) + "\">");
			out.println(setWithUser.toString());
			out.println("<p>Bitte füllen Sie das Textfeld mit Ihrer Lösung:</p>");
			out.println("<p><textarea cols=60 rows=10 name=textsolution>" + Util.mknohtml((String) request.getAttribute("textsolution")) + "</textarea></p>");
			out.println("<INPUT TYPE=submit VALUE=speichern>");
			out.println("</FORM>");
			out.println("<p class=mid><b>Achtung:</b> Bitte beachten Sie, dass Sie nach 5 Minuten Inaktivität automatisch ausgeloggt werden. Kopieren Sie den Text vor dem Absenden sicherheitshalber in die Zwischenablage, wenn Sie nicht sicher sind, ob Sie die Zeit überschritten haben.</p>");
		}

		template.printTemplateFooter();
	}
}
