/*
 * Copyright 2009-2012, 2020-2024 Sven Strickroth <email@cs-ware.de>
 * Copyright 2023 Marvin Hager
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.CommonErrorDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommonError;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.ShowSubmission;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a task in tutor view
 * @author Sven Strickroth
 */
@GATEView
public class ShowTaskTutorTestOverView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		RequestAdapter requestAdapter = new RequestAdapter(request);
		Session session = requestAdapter.getSession();

		Task task = (Task) request.getAttribute("task");

		template.addChartJs();
		template.printTemplateHeader("Testübersicht", task);
		PrintWriter out = response.getWriter();

		List<Submission> submissions = DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOrdered(task, false);
		if (submissions.isEmpty()) {
			out.println("noch keine Abgaben");
			template.printTemplateFooter();
			return;
		}

		Map<Integer, Integer> testCorrectSubmissions = new HashMap<>();
		if (request.getAttribute("group") == null) {
			testCorrectSubmissions = DAOFactory.TestResultDAOIf(session).getCorrectSubmissionsForTests(task);
		} else {
			Group group = (Group) request.getAttribute("group");
			Set<Participation> groupParticipations = group.getMembers();
			List<Submission> submissionsFromParticipants = new ArrayList<>();
			for (Participation participant : groupParticipations) {
				submissionsFromParticipants.addAll(participant.getSubmissions());
			}
			submissions.retainAll(submissionsFromParticipants);
			testCorrectSubmissions = DAOFactory.TestResultDAOIf(session).getCorrectSubmissionsForTestsInGroup(task, submissionsFromParticipants);
		}

		out.println("Abgaben: " + submissions.size() + "<br>");
		out.println("<ul>");
		for (Test test : task.getTests()) {
			if (test.isForTutors() && test.isNeedsToRun()) {
				continue;
			}
			out.println("<li>" + Util.escapeHTML(test.getTestTitle()) + ": " + testCorrectSubmissions.getOrDefault(test.getId(), 0) + " korrekt (" + String.format("%.2f", 100. * testCorrectSubmissions.getOrDefault(test.getId(), 0) / submissions.size()) + " %)</li>");
		}
		out.println("</ul>");
		// https://www.chartjs.org/docs/latest/charts/doughnut.html#pie

		out.println("<h2>Häufige Fehler</h2>");
		boolean shown = false;
		CommonErrorDAOIf commonErrorsDAO = DAOFactory.CommonErrorDAOIf(session);
		for (Test test : task.getTests()) {
			if (test.isForTutors() && test.isNeedsToRun()) {
				continue;
			}

			Map<Submission, List<CommonError>> subCommonErrorMap = commonErrorsDAO.getErrorsForSubmissions(test, submissions);
			List<CommonError> commonErrors = new ArrayList<>();
			for (Submission sub : submissions) {
				if (subCommonErrorMap.containsKey(sub)) {
					commonErrors.addAll(subCommonErrorMap.get(sub));
				}
			}
			List<CommonError> commonErrorsDistinct = commonErrors.stream().distinct().collect(Collectors.toList());
			Map<CommonError, Integer> commonErrorFrequency = new HashMap<>();
			for (CommonError ce : commonErrorsDistinct) {
				commonErrorFrequency.put(ce, Collections.frequency(commonErrors, ce));
			}

			shown = true;
			out.println("<h3>" + Util.escapeHTML(test.getTestTitle()) + "</h3>");
			out.println("<div class=mid style=\"position: relative; height:400px; width:400px\"><canvas id=\"chart" + test.getId() + "\"></canvas></div>");
			/* @formatter:off */
			out.println("<script>\n"
					+ "  const data" + test.getId() + " = {\n"
					+ "    labels: ['passed', 'failed'],\n"
					+ "    datasets: [{\n"
					+ "      label: 'Overview',\n"
					+ "      backgroundColor: [\n"
					+ "      'rgb(54, 162, 235)',\n"
					+ "      'rgb(255, 99, 132)'\n"
					+ "      ],\n"
					+ "      data: [" + testCorrectSubmissions.getOrDefault(test.getId(), 0) + "," + (submissions.size() - testCorrectSubmissions.getOrDefault(test.getId(), 0)) + "],\n"
					+ "    }]\n"
					+ "  };\n"
					+ "  const config" + test.getId() + " = {\n"
					+ "    type: 'pie',\n"
					+ "    data: data" + test.getId() + ",\n"
					+ "    options: {}\n"
					+ "  };\n"
					+ "  const myChart" + test.getId() + " = new Chart(\n"
					+ "    document.getElementById('chart" + test.getId() + "'),\n"
					+ "    config" + test.getId() + "\n"
					+ "  );"
					+ "</script>");
			/* @formatter:on */
			out.println("<table>");
			out.println("<thead>");
			out.println("<tr>");
			//out.println("<th>Fehler</th>");
			out.println("<th>Fehlertitel</th>");
			out.println("<th>Anzahl</th>");
			//out.println("<th>Beispiel</th>");
			out.println("</tr>");
			out.println("</thead>");
			Iterator<CommonError> it = commonErrorsDistinct.stream().sorted((ce1, ce2) -> -Integer.compare(commonErrorFrequency.get(ce1), commonErrorFrequency.get(ce2))).iterator();
			while (it.hasNext()) {
				CommonError commonError = it.next();
				out.println("<tr>");
				//out.println("<td>" + Util.escapeHTML(commonError.getCommonErrorName()) + "</td>");
				out.println("<td>" + Util.escapeHTML(commonError.getTitle()) + "</td>");
				out.println("<td>" + commonErrorFrequency.get(commonError) + "</td>");
				//out.println("<th>Beispiel</th>");
				out.println("</tr>");
				out.println("<tr>");
				out.println("<td colspan=2>");
				for (Entry<Submission, List<CommonError>> entry : subCommonErrorMap.entrySet()) {
					if (entry.getValue().contains(commonError)) {
						out.println(" <a href=\"" + Util.generateHTMLLink(ShowSubmission.class.getSimpleName() + "?sid=" + entry.getKey().getSubmissionid(), response) + "\" target=_blank>" + entry.getKey().getSubmissionid() + "</a></div>");
					}
				}
				out.println("</td>");
				out.println("</tr>");
			}
			out.println("</table>");
		}
		if (!shown) {
			out.println("keine (noch) keine Häufigen Fehler identifiziert");
		}

		template.printTemplateFooter();
	}
}
