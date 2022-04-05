/*
 * Copyright 2022-2023 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.ShowMarkHistory;
import de.tuclausthal.submissioninterface.servlets.controller.ShowSubmission;
import de.tuclausthal.submissioninterface.servlets.controller.ShowTask;
import de.tuclausthal.submissioninterface.servlets.controller.ShowTaskAllSubmissions;
import de.tuclausthal.submissioninterface.tasktypes.ClozeTaskType;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.ParameterHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying all cloze and MC answers for a task in a table
 * @author Sven Strickroth
 */
@GATEView
public class ShowTaskTutorAllSubmissionsSchematicView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		Task task = (Task) request.getAttribute("task");

		Template template = TemplateFactory.getTemplate(request, response);
		template.printTemplateHeader("Alle Abgaben (schematisch)", task);
		PrintWriter out = response.getWriter();
		out.println("<h2><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">" + Util.escapeHTML(task.getTitle()) + "</a></h2>");
		ParameterHelper parameterHelper = new ParameterHelper(request, ShowTaskAllSubmissions.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&schematic");
		boolean skipFullScore = parameterHelper.register(new ParameterHelper.Parameter("skipFullScore", "Abgaben mit voller Punktzahl"));
		boolean skipManuallyGraded = parameterHelper.register(new ParameterHelper.Parameter("skipManuallyGraded", "manuell bewertete Abgaben"));
		boolean hideAutoGraded = parameterHelper.register(new ParameterHelper.Parameter("skipAutoGraded", "automatische Bewertungen"));
		boolean showGrading = parameterHelper.register(new ParameterHelper.Parameter("showGrading", "Bewertungen"));
		parameterHelper.constructLinks(response, out);

		out.println("<table>");
		out.println("<tr>");
		out.println("<td id=taskdescription>" + Util.makeCleanHTML(task.getDescription()) + "</td>");
		out.println("</tr>");
		out.println("</table>");

		List<MCOption> options = DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(task);
		out.println("<table>");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<th>Submission</th>");
		if (task.isSCMCTask()) {
			for (MCOption mcOption : options) {
				out.println("<th>" + Util.escapeHTML(mcOption.getTitle()) + "</th>");
			}
			out.println("<th>Korrekt?</th>");
		} else if (task.isClozeTask()) {
			ClozeTaskType clozeHelper = new ClozeTaskType(task.getDescription(), null, true, false);
			for (int i = 0; i < clozeHelper.getClozeEntries(); ++i) {
				out.println("<th>Lücke " + (i + 1) + "</th>");
			}
		}
		out.println("<th>Autopoints</th>");
		if (showGrading) {
			out.println("<th>Points</th>");
			out.println("<th>Öff. Comment</th>");
			out.println("<th>Int. Comment</th>");
			out.println("<th>Bewerter</th>");
		}
		out.println("</tr>");
		out.println("</thead>");

		for (Submission submission : task.getSubmissions()) {
			if (skipFullScore && submission.getPoints() != null && submission.getPoints().getPoints() == task.getMaxPoints()) {
				continue;
			}
			if (skipManuallyGraded && submission.getPoints() != null && (submission.getPoints().getIssuedBy() != null || submission.getPoints().getPointStatus() == PointStatus.ABGENOMMEN_FAILED.ordinal()) && submission.getPoints().getPointStatus() != PointStatus.NICHT_BEWERTET.ordinal()) {
				continue;
			}
			if (hideAutoGraded && submission.getPoints() != null && submission.getPoints().getIssuedBy() == null) {
				continue;
			}
			out.println("<tr>");
			out.println("<td><a href=\"" + Util.generateHTMLLink(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">" + submission.getSubmissionid() + "</a></td>");
			if (task.isSCMCTask()) {
				List<Integer> selected = new ArrayList<>();
				for (String checked : DAOFactory.ResultDAOIf(session).getResultsForSubmission(submission)) {
					selected.add(Integer.parseInt(checked));
				}
				boolean allCorrect = true;
				for (MCOption option : options) {
					boolean optionSelected = selected.contains(option.getId());
					boolean correct = (option.isCorrect() && optionSelected) || (!option.isCorrect() && !optionSelected);
					allCorrect &= correct;
					out.println("<td class=" + (correct ? "green" : "red") + ">" + optionSelected + "</td>");
				}
				out.println("<td>" + Util.boolToHTML(allCorrect) + "</td>");
			} else if (task.isClozeTask()) {
				List<String> results = DAOFactory.ResultDAOIf(session).getResultsForSubmission(submission);
				ClozeTaskType clozeHelper = new ClozeTaskType(task.getDescription(), results, true, true);
				int i = 0;
				for (String result : results) {
					out.print("<td><span class=\"cloze_studentsolution");
					if (clozeHelper.isAutoGradeAble(i)) {
						out.print((clozeHelper.calculatePoints(i, result) > 0 ? " green" : " red"));
					}
					out.println("\">" + Util.escapeHTML(result) + "</span></td>");
					++i;
				}
				out.println("<td class=points>" + Util.showPoints(clozeHelper.calculatePoints(results)) + "</td>");
				if (showGrading && submission.getPoints() != null) {
					out.println("<td class=\"points " + Util.getPointsCSSClass(submission.getPoints()) + "\">" + Util.showPoints(submission.getPoints().getPoints()) + "</td>");
					out.println("<td class=feedback>" + Util.escapeHTML(submission.getPoints().getPublicComment()) + "</td>");
					out.println("<td class=feedback>" + Util.escapeHTML(submission.getPoints().getInternalComment()) + "</td>");
					String pointsGivenBy;
					if (submission.getPoints().getIssuedBy() == null) {
						pointsGivenBy = "GATE, <a href=\"" + Util.generateHTMLLink(ShowMarkHistory.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">History</a>)";
					} else {
						pointsGivenBy = "<a href=\"mailto:" + Util.escapeHTML(submission.getPoints().getIssuedBy().getUser().getEmail()) + "\">" + Util.escapeHTML(submission.getPoints().getIssuedBy().getUser().getLastNameFirstName()) + "</a>, <a href=\"" + Util.generateHTMLLink(ShowMarkHistory.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">History</a>)";
					}
					out.println("<td>" + pointsGivenBy + "</td>");
				}
			}
			out.println("</tr>");
		}
		out.println("</table>");
		template.printTemplateFooter();
	}
}
