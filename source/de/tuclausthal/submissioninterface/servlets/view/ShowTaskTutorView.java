/*
 * Copyright 2009 - 2011 Sven Strickroth <email@cs-ware.de>
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.TestResultDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a task in tutor view
 * @author Sven Strickroth
 */
public class ShowTaskTutorView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);
		template.addJQuery();

		PrintWriter out = response.getWriter();

		Session session = RequestAdapter.getSession(request);

		Task task = (Task) request.getAttribute("task");
		Participation participation = (Participation) request.getAttribute("participation");
		List<String> advisorFiles = (List<String>) request.getAttribute("advisorFiles");

		template.printTemplateHeader(task);

		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		// HTML must be possible here
		out.println("<td id=taskdescription>" + task.getDescription() + "</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Startdatum:</th>");
		out.println("<td>" + Util.mknohtml(task.getStart().toLocaleString()) + "</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Enddatum:</th>");
		out.println("<td>" + Util.mknohtml(task.getDeadline().toLocaleString()));
		if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
			out.println(" Keine Abgabe mehr möglich");
		}
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Punkte:</th>");
		out.println("<td class=points>" + Util.showPoints(task.getMaxPoints()) + "</td>");
		out.println("</tr>");
		if (advisorFiles.size() > 0) {
			out.println("<tr>");
			out.println("<th>Hinterlegte Dateien:</th>");
			out.println("<td><ul class=taskfiles>");
			for (String file : advisorFiles) {
				file = file.replace(System.getProperty("file.separator"), "/");
				out.println("<li><a href=\"" + response.encodeURL("DownloadTaskFile/" + file + "?taskid=" + task.getTaskid()) + "\">Download " + Util.mknohtml(file) + "</a></li>");
			}
			out.println("</ul></td>");
			out.println("</tr>");
		}
		out.println("</table>");

		if (participation.getRoleType() == ParticipationRole.ADVISOR) {
			out.println("<p><div class=mid><a href=\"" + response.encodeURL("TaskManager?lecture=" + task.getTaskGroup().getLecture().getId() + "&amp;taskid=" + task.getTaskid() + "&amp;action=editTask") + "\">Aufgabe bearbeiten</a></div>");
			out.println("<p><div class=mid><a onclick=\"return confirmLink('Wirklich löschen?')\" href=\"" + response.encodeURL("TaskManager?lecture=" + task.getTaskGroup().getLecture().getId() + "&amp;taskid=" + task.getTaskid() + "&amp;action=deleteTask") + "\">Aufgabe löschen</a></div>");
		}

		if ((participation.getRoleType() == ParticipationRole.ADVISOR || task.isTutorsCanUploadFiles()) && (task.isShowTextArea() == true || !"-".equals(task.getFilenameRegexp()))) {
			out.println("<p><div class=mid><a href=\"" + response.encodeURL("SubmitSolution?taskid=" + task.getTaskid()) + "\">Abgabe für Studenten durchführen</a> (Achtung wenn Duplikatstest bereits gelaufen ist)</div>");
		}

		if (task.isShowTextArea() == false && "-".equals(task.getFilenameRegexp())) {
			out.println("<p><div class=mid><a href=\"" + response.encodeURL("MarkEmptyTask?taskid=" + task.getTaskid()) + "\">Punkte vergeben</a></div>");
		}

		if (task.getSubmissions() != null && task.getSubmissions().size() > 0) {
			out.println("<p><h2>Abgaben</h2><p>");
			Iterator<Submission> submissionIterator = DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOrdered(task).iterator();
			Group lastGroup = null;
			boolean first = true;
			int sumOfSubmissions = 0;
			int sumOfPoints = 0;
			int groupSumOfSubmissions = 0;
			int groupSumOfAllSubmissions = 0;
			int groupSumOfPoints = 0;
			int testCols = 0;
			for (Test test : task.getTests()) {
				if (test.isForTutors()) {
					testCols++;
				}
			}
			int lastSID = 0;
			TestResultDAOIf testResultDAO = DAOFactory.TestResultDAOIf(session);
			List<Test> tests = DAOFactory.TestDAOIf(session).getTutorTests(task);
			boolean hasUnapprochedPoints = false;
			// dynamic splitter for groups
			while (submissionIterator.hasNext()) {
				Submission submission = submissionIterator.next();
				Group group = submission.getSubmitters().iterator().next().getGroup();
				if (first == true || lastGroup != group) {
					lastGroup = group;
					if (first == false) {
						if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
							out.println("<tr>");
							out.println("<td colspan=" + (1 + testCols + task.getSimularityTests().size()) + ">Durchschnittspunkte: (#: " + groupSumOfAllSubmissions + ")</td>");
							out.println("<td class=points>" + Util.showPoints(Float.valueOf(groupSumOfPoints / (float) groupSumOfSubmissions).intValue()) + "</td>");
							if (hasUnapprochedPoints) {
								out.println("<td><input type=submit value=Save></td>");
							} else {
								out.println("<td></td>");
							}
							out.println("</tr>");
						}
						out.println("</table><p>");
						out.println("</form></div>");
						groupSumOfAllSubmissions = 0;
						groupSumOfSubmissions = 0;
						groupSumOfPoints = 0;
					}
					first = false;
					hasUnapprochedPoints = false;
					if (group == null) {
						out.println("<h3>Ohne Gruppe</h3>");
						out.println("<div id=\"contentgroup0\">");
					} else {
						out.println("<h3>Gruppe: " + Util.mknohtml(group.getName()) + " <a href=\"#\" onclick=\"$('#contentgroup" + group.getGid() + "').toggle(); return false;\">(+/-)</a></h3>");
						String defaultState = "";
						if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) != 0 && group.getTutors().size() > 0 && !group.getTutors().contains(participation)) {
							defaultState = "style=\"display: none;\"";
						}
						out.println("<div " + defaultState + " id=\"contentgroup" + group.getGid() + "\">");
						out.println("<div class=mid><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid() + "&amp;action=grouplist&amp;groupid=" + group.getGid()) + "\" target=\"_blank\">Druckbare Liste</a></div>");
					}
					out.println("<form method=post action=\"" + response.encodeURL("MarkApproved?taskid=" + task.getTaskid()) + "\">");
					out.println("<table class=border>");
					out.println("<tr>");
					out.println("<th>Benutzer</th>");
					if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
						for (Test test : tests) {
							out.println("<th>" + Util.mknohtml(test.getTestTitle()) + "</th>");
						}
						for (SimilarityTest similarityTest : task.getSimularityTests()) {
							String color = "";
							String hint = "";
							if (similarityTest.getStatus() > 0) {
								color = "red";
								hint = " (läuft)";
							}
							out.println("<th><span class=" + color + " title=\"Max. Ähnlichkeit\">" + similarityTest + hint + "</span></th>");
						}
						out.println("<th>Punkte</th>");
						out.println("<th>Abnehmen</th>");
					}
					out.println("</tr>");
				}
				if (lastSID != submission.getSubmissionid()) {
					groupSumOfAllSubmissions++;
					out.println("<tr>");
					out.println("<td><a href=\"" + response.encodeURL("ShowSubmission?sid=" + submission.getSubmissionid()) + "\">" + Util.mknohtml(submission.getSubmitterNames()) + "</a></td>");
					lastSID = submission.getSubmissionid();
					if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
						for (Test test : tests) {
							if (testResultDAO.getResult(test, submission) != null) {
								out.println("<td>" + Util.boolToHTML(testResultDAO.getResult(test, submission).getPassedTest()) + "</td>");
							} else {
								out.println("<td>n/a</td>");
							}
						}
						for (SimilarityTest similarityTest : task.getSimularityTests()) {
							String users = "";
							for (Similarity similarity : DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(similarityTest, submission)) {
								users += Util.mknohtml(similarity.getSubmissionTwo().getSubmitterNames()) + "\n";
							}
							out.println("<td class=points><span title=\"" + users + "\">" + DAOFactory.SimilarityDAOIf(session).getMaxSimilarity(similarityTest, submission) + "</span></td>");
						}
						if (submission.getPoints() != null) {
							if (submission.getPoints().getPointsOk()) {
								out.println("<td align=right>" + Util.showPoints(submission.getPoints().getPointsByStatus()) + "</td>");
								out.println("<td></td>");
							} else {
								out.println("<td align=right>(" + Util.showPoints(submission.getPoints().getPointsByStatus()) + ")</td>");
								out.println("<td><input type=checkbox name=\"sid" + submission.getSubmissionid() + "\"></td>");
								hasUnapprochedPoints = true;
							}
							sumOfPoints += submission.getPoints().getPointsByStatus();
							groupSumOfPoints += submission.getPoints().getPointsByStatus();
							sumOfSubmissions++;
							groupSumOfSubmissions++;
						} else {
							out.println("<td>n/a</td>");
							out.println("<td></td>");
						}
					}
					out.println("</tr>");
				}
			}
			if (first == false) {
				if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
					out.println("<tr>");
					out.println("<td colspan=" + (1 + testCols + task.getSimularityTests().size()) + ">Durchschnittspunkte: (#: " + groupSumOfAllSubmissions + ")</td>");
					out.println("<td class=points>" + Util.showPoints(Float.valueOf(groupSumOfPoints / (float) groupSumOfSubmissions).intValue()) + "</td>");
					if (hasUnapprochedPoints) {
						out.println("<td><input type=submit value=Save></td>");
					} else {
						out.println("<td></td>");
					}
					out.println("</tr>");
				}
				out.println("</table><p>");
				out.println("</form></div>");
				if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
					out.println("<h3>Gesamtdurchschnitt: " + Util.showPoints(Float.valueOf(sumOfPoints / (float) sumOfSubmissions).intValue()) + "</h3>");
				}
			}
		}
		template.printTemplateFooter();
	}
}
