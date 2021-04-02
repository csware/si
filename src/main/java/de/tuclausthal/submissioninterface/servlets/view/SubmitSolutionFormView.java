/*
 * Copyright 2009-2013, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieIf;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.MCOptionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.tasktypes.ClozeTaskType;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for the submission of files
 * @author Sven Strickroth
 */
@GATEView
public class SubmitSolutionFormView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Task task = (Task) request.getAttribute("task");
		Participation participation = (Participation) request.getAttribute("participation");

		template.addKeepAlive();
		template.printTemplateHeader("Abgabe starten", task);
		PrintWriter out = response.getWriter();

		Session session = RequestAdapter.getSession(request);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(task, RequestAdapter.getUser(request));

		if (submission != null && task.isAllowPrematureSubmissionClosing() && submission.isClosed()) {
			request.setAttribute("title", "Die Abgabe wurde bereits als endgültig abgeschlossen markiert. Eine Veränderung ist daher nicht mehr möglich.");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		StringBuilder setWithUser = new StringBuilder();
		if (task.getMaxSubmitters() > 1 && submission == null) {
			if (participation.getGroup() != null && participation.getGroup().isSubmissionGroup()) {
				setWithUser = new StringBuilder("<p>Diese Abgabe wird automatisch für alle Studierenden in Ihrer Gruppe durchgeführt.</p>");
			} else if (task.isAllowSubmittersAcrossGroups() || participation.getGroup() != null) {
				StringBuilder partnerField = new StringBuilder();
				setWithUser.append("<p>Haben Sie diese Aufgabe zusammen mit einer Partnerin oder einem Partner gelöst? Dann bitte hier auswählen:<br>");
				partnerField.append("<select name=partnerid size=1>");
				int cnt = 0;
				partnerField.append("<option value=0>-</option>");
				List<Participation> participations;
				if (task.isAllowSubmittersAcrossGroups()) {
					participations = DAOFactory.ParticipationDAOIf(session).getLectureParticipations(task.getTaskGroup().getLecture());
				} else {
					participations = DAOFactory.ParticipationDAOIf(session).getParticipationsOfGroup(participation.getGroup());
				}
				for (Participation part : participations) {
					// filter out students which already have a submission and users which are in a submissiongroup (otherwise the other participants of the submissiongroup cannot submit a solution any more)
					if (part.getId() != participation.getId() && part.getRoleType().equals(ParticipationRole.NORMAL) && (!task.isAllowSubmittersAcrossGroups() || part.getGroup() == null || !part.getGroup().isSubmissionGroup()) && submissionDAO.getSubmission(task, part.getUser()) == null) {
						cnt++;
						partnerField.append("<option value=" + part.getId() + ">" + Util.escapeHTML(part.getUser().getFullName()) + "</option>");
					}
				}
				partnerField.append("</select><br>");
				if (cnt == 0) {
					setWithUser = new StringBuilder("<p>Sie können im Moment keine Partnerin und keinen Partner für Ihre Abgabe auswählen. Um dies zu erreichen müssen Sie die folgenden Voraussetzungen erfüllen:<ol><li>Ihre Partnerin bzw. Ihr Partner muss sich auch (mindestens) einmal an diesem System angemeldet haben</li>");
					setWithUser.append("<li>Ihr Partner darf noch keine eigene Abgabe vorgenommen haben.</li>");
					if (!task.isAllowSubmittersAcrossGroups()) {
						setWithUser.append("<li>Sie, als auch Ihre Partnerin bzw. Ihr Partner, müssen von Ihrer Tutorin bzw. Ihrem Tutor in die gleiche Übungsgruppe aufgenommen worden sein.</li>");
					} else {
						setWithUser.append("<li>Ihre Partnerin bzw. Ihr Partner darf keiner Gruppe angehören, die für Gruppenabgaben konfiguriert ist.</li>");
					}
					setWithUser.append("</ol></p><hr>");
				} else {
					for (int i = 0; i < task.getMaxSubmitters() - 1; i++) {
						setWithUser.append(partnerField);
					}
					setWithUser.append("<br>");
				}
			} else if (participation.getGroup() == null) {
				setWithUser = new StringBuilder("<p>Sie können im Moment keine Partnerin und keinen Partner für Ihre Abgabe auswählen. Um dies zu erreichen müssen Sie zwei Voraussetzungen erfüllen:<ol><li>Ihre Partnerin bzw. Ihr Partner muss sich auch (mindestens) einmal an diesem System angemeldet haben</li><li>Sie, als auch Ihre Partnerin bzw. Ihr Partner, müssen von Ihrer Tutorin bzw. Ihrem Tutor in die gleiche Übungsgruppe aufgenommen worden sein.</li></ol></p><hr>");
			}
		}

		if (task.isSCMCTask() || task.isClozeTask()) {
			out.println("<FORM ENCTYPE=\"multipart/form-data\" method=POST action=\"" + Util.generateHTMLLink("?taskid=" + task.getTaskid(), response) + "\">");
			out.println(setWithUser.toString());
		}

		if (task.isShowTextArea() || task.isSCMCTask()) {
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Beschreibung:</th>");
			out.println("<td id=taskdescription>" + Util.makeCleanHTML(task.getDescription()) + "</td>");
			out.println("</tr>");

			if (task.isSCMCTask()) {
				out.println("<tr>");
				out.println("<th>Antwort:</th>");
				out.println("<td>");

				List<Integer> selected = new ArrayList<>();
				if (submission != null) {
					for (String checked : DAOFactory.ResultDAOIf(session).getResultsForSubmission(submission)) {
						selected.add(Integer.parseInt(checked));
					}
				}

				MCOptionDAOIf mcOptionDAO = DAOFactory.MCOptionDAOIf(session);
				List<MCOption> options = mcOptionDAO.getMCOptionsForTask(task);
				Collections.shuffle(options, new Random(participation.getId()));
				int i = 0;
				for (MCOption option : options) {
					if (task.isSCTask()) {
						out.println("<input type=radio required " + (selected.contains(option.getId()) ? "checked" : "") + " name=check value=" + i + " id=\"check" + i + "\"> <label for=\"check" + i + "\">" + Util.escapeHTML(option.getTitle()) + "</label><br>");
					} else {
						out.println("<input type=checkbox " + (selected.contains(option.getId()) ? "checked" : "") + " name=\"check" + i + "\" id=\"check" + i + "\"> <label for=\"check" + i + "\">" + Util.escapeHTML(option.getTitle()) + "</label><br>");
					}
					++i;
				}

				out.println("<br>");
				out.println("<INPUT TYPE=submit VALUE=speichern>");
				out.println("</td>");
				out.println("</tr>");

				out.println("</table>");
				out.println("</FORM>");
				template.printTemplateFooter();
				return;
			}
			out.println("</table>");
		} else if (task.isClozeTask()) {
			List<String> lastResults = null;
			if (submission != null) {
				lastResults = DAOFactory.ResultDAOIf(session).getResultsForSubmission(submission);
			}
			ClozeTaskType clozeHelper = new ClozeTaskType(task.getDescription(), lastResults, false, false);

			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Aufgabe:</th>");
			out.print("<td id=taskdescription>");
			out.println(clozeHelper.toHTML());
			out.println("<br>");
			out.println("<INPUT TYPE=submit VALUE=speichern>");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</FORM>");
			template.printTemplateFooter();
			return;
		}

		if (!"-".equals(task.getFilenameRegexp())) {
			out.println("<FORM class=mid ENCTYPE=\"multipart/form-data\" method=POST action=\"" + Util.generateHTMLLink("?taskid=" + task.getTaskid(), response) + "\">");
			out.println(setWithUser.toString());
			out.println("<p>Bitte wählen Sie eine oder mehrere Dateien aus, die Sie einsenden möchten.</p>");
			out.println("<INPUT TYPE=file NAME=file multiple required>");
			out.println("<INPUT TYPE=submit VALUE=upload>");
			out.println("<p>Hinweis: Bestehende Dateien werden überschrieben.</p>");
			out.println("</FORM>");
			if (task.isShowTextArea()) {
				out.println("<p><hr>");
			}
		}

		if (task.isShowTextArea()) {
			out.println("<FORM class=mid method=POST action=\"" + Util.generateHTMLLink("?taskid=" + task.getTaskid(), response) + "\">");
			out.println(setWithUser.toString());
			if (task.isADynamicTask()) {
				out.println("<p>Benutzen Sie bitte einen Punkt statt ein Komma zur Trennung von Dezimal-Stellen.</p>");
				DynamicTaskStrategieIf dynamicTask = task.getDynamicTaskStrategie(session);
				String[] resultFields = dynamicTask.getResultFields(false);
				List<String> studentResults = dynamicTask.getUserResults(submission);
				for (int i = 0; i < resultFields.length; i++) {
					out.println("<p>" + Util.escapeHTML(resultFields[i]) + ": <input type=text name=\"dynamicresult" + i + "\" size=35 autocomplete=\"off\" value=\"" + Util.escapeHTML(studentResults.get(i)) + "\"></p>");
				}
				out.println("<p>Bitte füllen Sie das Textfeld mit dem Rechnenweg zu Ihrer Lösung:</p>");
			} else {
				out.println("<p>Bitte füllen Sie das Textfeld mit Ihrer Lösung:</p>");
			}
			out.println("<p><textarea cols=60 rows=10 name=textsolution>" + Util.escapeHTML((String) request.getAttribute("textsolution")) + "</textarea></p>");
			out.println("<INPUT TYPE=submit VALUE=speichern>");
			out.println("</FORM>");
			out.println("<p class=mid><b>Achtung:</b> Bitte beachten Sie, dass Sie nach " + (request.getSession().getMaxInactiveInterval() / 60) + " Minuten Inaktivität automatisch ausgeloggt werden könnten. Kopieren Sie den Text vor dem Absenden sicherheitshalber in die Zwischenablage, wenn Sie nicht sicher sind, ob Sie die Zeit überschritten haben.</p>");
		}

		template.printTemplateFooter();
	}
}
