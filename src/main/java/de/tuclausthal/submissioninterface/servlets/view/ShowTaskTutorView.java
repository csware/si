/*
 * Copyright 2009-2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import java.text.SimpleDateFormat;
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
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
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
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);
		template.addJQuery();

		RequestAdapter requestAdapter = new RequestAdapter(request);
		Session session = requestAdapter.getSession();

		Task task = (Task) request.getAttribute("task");
		Participation participation = (Participation) request.getAttribute("participation");
		@SuppressWarnings("unchecked")
		List<String> advisorFiles = (List<String>) request.getAttribute("advisorFiles");
		@SuppressWarnings("unchecked")
		List<String> modelSolutionFiles = (List<String>) request.getAttribute("modelSolutionFiles");

		template.printTemplateHeader(task);
		PrintWriter out = response.getWriter();
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		out.println("<td id=taskdescription>" + Util.makeCleanHTML(task.getDescription()) + "</td>");
		out.println("</tr>");
		if (task.isAllowPrematureSubmissionClosing() && task.getDeadline().after(Util.correctTimezone(new Date()))) {
			out.println("<tr><th>Vorzeitige finale Abgabe:</th><td>Studierende können vor der Deadline die Abgabe als endgültig abgegeben markieren.");
			if (!task.getSimularityTests().isEmpty()) {
				out.println("<br>Achtung: Die Ergebnisse der Ähnlichkeitsprüfung stehen erst nach Abgabeschluss zur Verfügung.");
			}
			out.println("</td></tr>");
		}
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		out.println("<tr>");
		out.println("<th>Startdatum:</th>");
		out.println("<td>" + Util.escapeHTML(dateFormatter.format(task.getStart())) + "</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Enddatum:</th>");
		out.println("<td>" + Util.escapeHTML(dateFormatter.format(task.getDeadline())));
		if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
			out.println(" Keine Abgabe mehr möglich");
		}
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Punktedatum:</th>");
		if (task.getShowPoints() != null) {
			out.println("<td>" + Util.escapeHTML(dateFormatter.format(task.getShowPoints())) + "</td>");
		} else if (participation.getRoleType() == ParticipationRole.ADVISOR) {
			out.println("<td><a href=\"" + response.encodeURL("PublishGrades?taskid=" + task.getTaskid()) + "\">Punkte freigeben</a></td>");
		} else {
			out.println("<td>Punkte werden manuell freigegeben</td>");
		}
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Punkte:</th>");
		out.println("<td class=points>" + Util.showPoints(task.getMaxPoints()) + "</td>");
		out.println("</tr>");
		if (!advisorFiles.isEmpty()) {
			out.println("<tr>");
			out.println("<th>Hinterlegte Dateien:</th>");
			out.println("<td><ul class=taskfiles>");
			for (String file : advisorFiles) {
				file = file.replace(System.getProperty("file.separator"), "/");
				out.println("<li><a href=\"" + response.encodeURL("DownloadTaskFile/" + file + "?taskid=" + task.getTaskid()) + "\">Download " + Util.escapeHTML(file) + "</a></li>");
			}
			out.println("</ul></td>");
			out.println("</tr>");
		}
		if (task.isMCTask()) {
			out.println("<tr>");
			out.println("<th>MC-Antworten:</th>");
			out.println("<td><ul>");
			for (MCOption option : DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(task)) {
				out.println("<li>" + Util.escapeHTML(option.getTitle()) + (option.isCorrect() ? " (korrekt)" : "") + "</li>");
			}
			out.println("</ul></td>");
			out.println("</tr>");
		}
		out.println("</table>");

		if (participation.getRoleType() == ParticipationRole.ADVISOR) {
			out.println("<p><div class=mid><a href=\"" + response.encodeURL("TaskManager?lecture=" + task.getTaskGroup().getLecture().getId() + "&amp;taskid=" + task.getTaskid() + "&amp;action=editTask") + "\">Aufgabe bearbeiten</a></div>");
			out.println("<p><div class=mid><a onclick=\"return confirmLink('Wirklich löschen?')\" href=\"" + response.encodeURL("TaskManager?lecture=" + task.getTaskGroup().getLecture().getId() + "&amp;taskid=" + task.getTaskid() + "&amp;action=deleteTask") + "\">Aufgabe löschen</a></div>");
		}

		if (!task.isMCTask() && !task.isADynamicTask() && (participation.getRoleType() == ParticipationRole.ADVISOR || task.isTutorsCanUploadFiles()) && (task.isShowTextArea() == true || !"-".equals(task.getFilenameRegexp()))) {
			out.println("<p><div class=mid><a href=\"" + response.encodeURL("SubmitSolution?taskid=" + task.getTaskid()) + "\">Abgabe für Studierenden durchführen</a> (Achtung wenn Duplikatstest bereits gelaufen ist)</div>");
		}

		if (task.isShowTextArea() == false && "-".equals(task.getFilenameRegexp())) {
			out.println("<p><div class=mid><a href=\"" + response.encodeURL("MarkEmptyTask?taskid=" + task.getTaskid()) + "\">Punkte vergeben</a></div>");
		} else if (!task.getTests().isEmpty()) {
			out.println("<p><div class=mid><a href=\"" + response.encodeURL("PerformTest?taskid=" + task.getTaskid()) + "\">Test (manuell) durchführen</a></div>");
		}

		if (!modelSolutionFiles.isEmpty()) {
			out.println("<h2>Musterlösung:</h2>");
			out.println("<ul>");
			for (String file : modelSolutionFiles) {
				out.println("<li><a href=\"" + response.encodeURL("DownloadModelSolutionFile/" + file + "?taskid=" + task.getTaskid()) + "\">" + file + "</a></li>");
			}
			out.println("</ul>");
		}

		if (task.getSubmissions() != null && !task.getSubmissions().isEmpty()) {
			out.println("<p><h2>Abgaben</h2><p>");
			if (!task.isMCTask()) {
				out.println("<p><div class=mid><a href=\"" + response.encodeURL("SearchSubmissions?taskid=" + task.getTaskid()) + "\">Suchen...</a></div>");
			}
			Iterator<Submission> submissionIterator = DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOrdered(task).iterator();
			Group lastGroup = null;
			boolean first = true;
			int sumOfSubmissions = 0;
			int sumOfPoints = 0;
			int groupSumOfSubmissions = 0;
			int groupSumOfAllSubmissions = 0;
			int groupSumOfPoints = 0;
			int lastSID = 0;
			TestResultDAOIf testResultDAO = DAOFactory.TestResultDAOIf(session);
			List<Test> tests = DAOFactory.TestDAOIf(session).getTutorTests(task);
			boolean hasUnapprochedPoints = false;
			boolean showAllColumns = (task.getDeadline().before(Util.correctTimezone(new Date())) || task.isAllowPrematureSubmissionClosing()) && !requestAdapter.isPrivacyMode();
			boolean showPrematureSubmissionColumn = task.isAllowPrematureSubmissionClosing() && task.getDeadline().after(Util.correctTimezone(new Date()));
			// dynamic splitter for groups
			while (submissionIterator.hasNext()) {
				Submission submission = submissionIterator.next();
				Group group = null;
				for (Participation submitter : submission.getSubmitters()) {
					if (submitter.getGroup() != null && (lastGroup == null || lastGroup.getGid() <= submitter.getGroup().getGid())) {
						if (group == null) {
							group = submitter.getGroup();
						} else if (group.getGid() > submitter.getGroup().getGid()) {
							group = submitter.getGroup();
						}
					}
				}
				if (first == true || lastGroup != group) {
					lastGroup = group;
					if (first == false) {
						if (showAllColumns) {
							out.println("<tr>");
							out.println("<td colspan=" + (1 + (task.isADynamicTask() ? 1 : 0) + ((task.getDeadline().before(Util.correctTimezone(new Date()))) ? tests.size() + task.getSimularityTests().size() : 0)) + ">Anzahl: " + groupSumOfAllSubmissions + " / Durchschnittspunkte:</td>");
							out.println("<td class=points>" + Util.showPoints(Float.valueOf(groupSumOfPoints / (float) groupSumOfSubmissions).intValue()) + "</td>");
							if (hasUnapprochedPoints) {
								out.println("<td><input type=submit value=Save></td>");
							} else {
								out.println("<td></td>");
							}
							if (showPrematureSubmissionColumn) {
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
						out.println("<div class=mid><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid() + "&amp;action=grouplist") + "\" target=\"_blank\">Druckbare Liste</a></div>");
						if (!task.isADynamicTask() && !task.isMCTask()) {
							out.println("<div class=mid><a href=\"" + response.encodeURL("DownloadSubmissionsByGroup?taskid=" + task.getTaskid()) + "\">Alle Abgaben der Gruppe herunterladen (ZIP-Archiv)</a></div>");
						}
					} else {
						out.println("<h3>Gruppe: " + Util.escapeHTML(group.getName()) + " <a href=\"#\" onclick=\"$('#contentgroup" + group.getGid() + "').toggle(); return false;\">(+/-)</a></h3>");
						String defaultState = "";
						if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) != 0 && !group.getTutors().isEmpty() && !group.getTutors().contains(participation) && !!"taskWise".equals(task.getTaskGroup().getLecture().getGradingMethod())) {
							defaultState = "style=\"display: none;\"";
						}
						out.println("<div " + defaultState + " id=\"contentgroup" + group.getGid() + "\">");
						out.println("<div class=mid><a href=\"" + response.encodeURL("ShowTask?taskid=" + task.getTaskid() + "&amp;action=grouplist&amp;groupid=" + group.getGid()) + "\" target=\"_blank\">Druckbare Liste</a></div>");
						if (!task.isADynamicTask() && !task.isMCTask()) {
							out.println("<div class=mid><a href=\"" + response.encodeURL("DownloadSubmissionsByGroup?taskid=" + task.getTaskid() + "&amp;groupid=" + group.getGid()) + "\">Alle Abgaben der Gruppe herunterladen (ZIP-Archiv)</a></div>");
						}
					}
					out.println("<form method=post action=\"" + response.encodeURL("MarkApproved?taskid=" + task.getTaskid()) + "\">");
					out.println("<table class=border>");
					out.println("<tr>");
					out.println("<th>Abgabe von</th>");
					if (showAllColumns) {
						if (task.isADynamicTask()) {
							out.println("<th>Berechnung</th>");
						}
						// show test columns only if the deadline is over
						if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
							for (Test test : tests) {
								out.println("<th>" + Util.escapeHTML(test.getTestTitle()) + "</th>");
							}
							for (SimilarityTest similarityTest : task.getSimularityTests()) {
								String color = "\"\"";
								String hint = "";
								if (similarityTest.getStatus() > 0) {
									color = "red";
									hint = " (läuft)";
								}
								out.println("<th><span class=" + color + " title=\"Max. Ähnlichkeit\">" + similarityTest + hint + "</span></th>");
							}
						}
						out.println("<th>Punkte</th>");
						out.println("<th>Abnehmen</th>");
						if (showPrematureSubmissionColumn) {
							out.println("<th>Status der Abgabe</th>");
						}
					}
					out.println("</tr>");
				}
				if (lastSID != submission.getSubmissionid()) {
					groupSumOfAllSubmissions++;
					out.println("<tr>");
					String groupAdding = "";
					if (group != null) {
						groupAdding = "&amp;groupid=" + group.getGid();
					}
					out.println("<td><a href=\"" + response.encodeURL("ShowSubmission?sid=" + submission.getSubmissionid() + groupAdding) + "\">" + Util.escapeHTML(submission.getSubmitterNames()) + "</a></td>");
					lastSID = submission.getSubmissionid();
					if (showAllColumns) {
						if (task.isADynamicTask()) {
							out.println("<td>" + Util.boolToHTML(task.getDynamicTaskStrategie(session).isCorrect(submission)) + "</td>");
						}
						// show columns only if the results are in the database after the deadline
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
								int maxSimilarity = 0;
								for (Similarity similarity : DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(similarityTest, submission)) {
									users += Util.escapeHTML(similarity.getSubmissionTwo().getSubmitterNames()) + "\n";
									maxSimilarity = similarity.getPercentage();
								}
								out.println("<td align=right><span title=\"" + users + "\">" + maxSimilarity + "</span></td>");
							}
						}
						if (submission.getPoints() != null && submission.getPoints().getPointStatus() != PointStatus.NICHT_BEWERTET.ordinal()) {
							if (submission.getPoints().getPointsOk()) {
								out.println("<td class=\"points" + Util.getPointsCSSClass(submission.getPoints()) + "\">" + Util.showPoints(submission.getPoints().getPointsByStatus(task.getMinPointStep())) + "</td>");
								out.println("<td></td>");
								sumOfPoints += submission.getPoints().getPointsByStatus(task.getMinPointStep());
								groupSumOfPoints += submission.getPoints().getPointsByStatus(task.getMinPointStep());
								sumOfSubmissions++;
								groupSumOfSubmissions++;
							} else {
								out.println("<td class=\"points" + Util.getPointsCSSClass(submission.getPoints()) + "\">(" + Util.showPoints(submission.getPoints().getPlagiarismPoints(task.getMinPointStep())) + ")</td>");
								out.println("<td><input type=checkbox name=\"sid" + submission.getSubmissionid() + "\"></td>");
								hasUnapprochedPoints = true;
							}
						} else {
							out.println("<td>n/a</td>");
							out.println("<td></td>");
						}
						if (showPrematureSubmissionColumn) {
							out.println("<td>" + (submission.isClosed() ? "<span class=b>abgeschlossen</span>" : "<small>nicht abgeschlossen</small>") + "</td>");
						}
					}
					out.println("</tr>");
				}
			}
			if (first == false) {
				if (showAllColumns) {
					out.println("<tr>");
					out.println("<td colspan=" + (1 + (task.isADynamicTask() ? 1 : 0) + ((task.getDeadline().before(Util.correctTimezone(new Date()))) ? tests.size() + task.getSimularityTests().size() : 0)) + ">Anzahl: " + groupSumOfAllSubmissions + " / Durchschnittspunkte:</td>");
					out.println("<td class=points>" + Util.showPoints(Float.valueOf(groupSumOfPoints / (float) groupSumOfSubmissions).intValue()) + "</td>");
					if (hasUnapprochedPoints) {
						out.println("<td><input type=submit value=Save></td>");
					} else {
						out.println("<td></td>");
					}
					if (showPrematureSubmissionColumn) {
						out.println("<td></td>");
					}
					out.println("</tr>");
				}
				out.println("</table><p>");
				out.println("</form></div>");
				if (showAllColumns) {
					out.println("<h3>Gesamtdurchschnitt: " + Util.showPoints(Float.valueOf(sumOfPoints / (float) sumOfSubmissions).intValue()) + "</h3>");
				}
			}
		}
		template.printTemplateFooter();
	}
}
