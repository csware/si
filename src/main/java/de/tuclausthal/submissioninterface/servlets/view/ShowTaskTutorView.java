/*
 * Copyright 2009-2012, 2020-2023, 2025 Sven Strickroth <email@cs-ware.de>
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
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
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.DownloadModelSolutionFile;
import de.tuclausthal.submissioninterface.servlets.controller.DownloadSubmissionsByGroup;
import de.tuclausthal.submissioninterface.servlets.controller.DownloadTaskFile;
import de.tuclausthal.submissioninterface.servlets.controller.MarkApproved;
import de.tuclausthal.submissioninterface.servlets.controller.MarkEmptyTask;
import de.tuclausthal.submissioninterface.servlets.controller.MassMarkTask;
import de.tuclausthal.submissioninterface.servlets.controller.PerformTest;
import de.tuclausthal.submissioninterface.servlets.controller.PublishGrades;
import de.tuclausthal.submissioninterface.servlets.controller.SearchSubmissions;
import de.tuclausthal.submissioninterface.servlets.controller.ShowSubmission;
import de.tuclausthal.submissioninterface.servlets.controller.ShowTask;
import de.tuclausthal.submissioninterface.servlets.controller.ShowTaskAllSubmissions;
import de.tuclausthal.submissioninterface.servlets.controller.SubmitSolution;
import de.tuclausthal.submissioninterface.servlets.controller.TaskManager;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a task in tutor view
 * @author Sven Strickroth
 */
@GATEView
public class ShowTaskTutorView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		RequestAdapter requestAdapter = new RequestAdapter(request);
		Session session = requestAdapter.getSession();

		Task task = (Task) request.getAttribute("task");
		Participation participation = (Participation) request.getAttribute("participation");
		@SuppressWarnings("unchecked")
		List<String> advisorFiles = (List<String>) request.getAttribute("advisorFiles");
		@SuppressWarnings("unchecked")
		List<String> modelSolutionFiles = (List<String>) request.getAttribute("modelSolutionFiles");

		template.addSortTableJs();
		template.printTemplateHeader(task);
		PrintWriter out = response.getWriter();
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		out.println("<td id=taskdescription>" + Util.makeCleanHTML(task.getDescription()) + "</td>");
		out.println("</tr>");
		if (task.isAllowPrematureSubmissionClosing() && task.getDeadline().isAfter(ZonedDateTime.now())) {
			out.println("<tr><th>Vorzeitige finale Abgabe:</th><td>Studierende können vor der Deadline die Abgabe als endgültig abgegeben markieren.");
			if (!task.getSimilarityTests().isEmpty()) {
				out.println("<br>Achtung: Die Ergebnisse der Ähnlichkeitsprüfung stehen erst nach Abgabeschluss zur Verfügung.");
			}
			out.println("</td></tr>");
		}
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
		out.println("<tr>");
		out.println("<th>Startdatum:</th>");
		out.println("<td>" + Util.escapeHTML(dateFormatter.format(task.getStart())) + (task.getStart().isAfter(ZonedDateTime.now()) ? " <img src=\"" + getServletContext().getContextPath() + "/assets/eyeslash.svg\" width=16 height=16 class=inlineicon border=0 alt=\"für Studierende nicht sichtbar\" title=\"für Studierende nicht sichtbar\">" : "") + "</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Enddatum:</th>");
		out.println("<td>" + Util.escapeHTML(dateFormatter.format(task.getDeadline())));
		if (task.getDeadline().isBefore(ZonedDateTime.now())) {
			out.println(" Keine Abgabe mehr möglich");
		}
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Punktedatum:</th>");
		if (task.getShowPoints() != null) {
			out.println("<td>" + Util.escapeHTML(dateFormatter.format(task.getShowPoints())) + (task.getShowPoints().isAfter(ZonedDateTime.now()) ? " <img src=\"" + getServletContext().getContextPath() + "/assets/eyeslash.svg\" width=16 height=16 class=inlineicon border=0 alt=\"für Studierende nicht sichtbar\" title=\"für Studierende nicht sichtbar\">" : "") + "</td>");
		} else if (participation.getRoleType() == ParticipationRole.ADVISOR) {
			out.println("<td><a href=\"" + Util.generateHTMLLink(PublishGrades.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">Punkte freigeben</a></td>");
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
				out.println("<li><a href=\"" + Util.generateHTMLLink(DownloadTaskFile.class.getSimpleName() + "/" + Util.encodeURLPathComponent(file) + "?taskid=" + task.getTaskid(), response) + "\">Download " + Util.escapeHTML(file) + "</a></li>");
			}
			out.println("</ul></td>");
			out.println("</tr>");
		}
		if (task.isSCMCTask()) {
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
			out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink(TaskManager.class.getSimpleName() + "?lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid() + "&action=editTask", response) + "\">Aufgabe bearbeiten</a></div>");
		}

		if (!task.isSCMCTask() && !task.isClozeTask() && !task.isADynamicTask() && (participation.getRoleType() == ParticipationRole.ADVISOR || task.isTutorsCanUploadFiles()) && (task.showTextArea() || !"-".equals(task.getFilenameRegexp()))) {
			out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink(SubmitSolution.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">Abgabe für Studierenden durchführen</a> (Achtung wenn Duplikatstest bereits gelaufen ist)</div>");
		}

		if (task.showTextArea() == false && "-".equals(task.getFilenameRegexp())) {
			out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink(MarkEmptyTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">Punkte vergeben</a></div>");
		} else if (!task.getTests().isEmpty() && task.getTests().stream().anyMatch(test -> test.TutorsCanRun())) {
			out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink(PerformTest.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">Test (manuell) durchführen</a></div>");
		}

		if (!task.isSCMCTask() && !task.isClozeTask() && participation.getRoleType() == ParticipationRole.ADVISOR && task.getMaxSubmitters() <= 1) {
			out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink(MassMarkTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">Bewertungen als CSV-Datei hochladen</a></div>");
		}

		if (!modelSolutionFiles.isEmpty()) {
			out.println("<h2>Musterlösung:</h2>");
			out.println("<ul>");
			for (String file : modelSolutionFiles) {
				out.println("<li><a href=\"" + Util.generateHTMLLink(DownloadModelSolutionFile.class.getSimpleName() + "/" + Util.encodeURLPathComponent(file) + "?taskid=" + task.getTaskid(), response) + "\">" + Util.escapeHTML(file) + "</a></li>");
			}
			out.println("</ul>");
		}

		if (task.getSubmissions() != null && !task.getSubmissions().isEmpty()) {
			if (!task.getTests().isEmpty() && task.getTests().stream().anyMatch(t -> t.isForTutors())) {
				if (task.getTests().stream().anyMatch(t -> t.isForTutors() && !t.isNeedsToRun())) {
					out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&show=testoverview", response) + "\">Übersicht der Testergebnisse</a></div>");
				}
				if (participation.getRoleType() == ParticipationRole.ADVISOR && task.getTaskGroup().getLecture().getName().contains("Live-Coding")) {
					out.println("<p><div class=mid><a onclick=\"return confirm('Are you sure?')\" href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&action=dotests", response) + "\">Alle Tests JETZT ausführen...</a></div>");
				}
			}
			Map<Integer, Map<Integer, Boolean>> testResults = DAOFactory.TestResultDAOIf(session).getResults(task);
			Map<Integer, Map<Integer, List<Similarity>>> similarities = DAOFactory.SimilarityDAOIf(session).getMaxSimilarities(task);
			out.println("<p><h2>Abgaben</h2><p>");
			out.println("<p class=mid>");
			out.println("<a href=\"" + Util.generateHTMLLink(ShowTaskAllSubmissions.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">alle Abgaben anzeigen</a><br>");
			if (task.isClozeTask() || task.isSCMCTask()) {
				out.println("<a href=\"" + Util.generateHTMLLink(ShowTaskAllSubmissions.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&schematic", response) + "\">alle Abgaben anzeigen mehr schematisch</a>");
			}
			out.println("</p>");
			if (!task.isSCMCTask()) {
				out.println("<p><div class=mid><a href=\"" + Util.generateHTMLLink(SearchSubmissions.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">Suchen...</a></div>");
			}
			boolean honorGroups = !task.isAllowSubmittersAcrossGroups();
			Iterator<Submission> submissionIterator = DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOrdered(task, honorGroups).iterator();
			Group lastGroup = null;
			boolean first = true;
			int sumOfSubmissions = 0;
			int sumOfPoints = 0;
			int groupSumOfSubmissions = 0;
			int groupSumOfAllSubmissions = 0;
			int groupSumOfPoints = 0;
			List<Test> tests = DAOFactory.TestDAOIf(session).getTutorTests(task);
			boolean hasUnapprochedPoints = false;
			boolean showAllColumns = (task.getDeadline().isBefore(ZonedDateTime.now()) || task.isAllowPrematureSubmissionClosing()) && !requestAdapter.isPrivacyMode();
			boolean showPrematureSubmissionColumn = task.isAllowPrematureSubmissionClosing() && task.getDeadline().isAfter(ZonedDateTime.now());
			// dynamic splitter for groups
			while (submissionIterator.hasNext()) {
				Submission submission = submissionIterator.next();
				Group group = null;
				for (Participation submitter : submission.getSubmitters()) {
					if (honorGroups && submitter.getGroup() != null && (lastGroup == null || lastGroup.getGid() <= submitter.getGroup().getGid())) {
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
							out.println("<tfoot><tr>");
							out.println("<td colspan=" + (1 + (task.isADynamicTask() ? 1 : 0) + ((task.getDeadline().isBefore(ZonedDateTime.now())) ? tests.size() + task.getSimilarityTests().size() : 0)) + ">Anzahl: " + groupSumOfAllSubmissions + " / Durchschnittspunkte:</td>");
							out.println("<td class=points>" + Util.showPoints(Float.valueOf(groupSumOfPoints / (float) groupSumOfSubmissions).intValue()) + "</td>");
							if (hasUnapprochedPoints) {
								out.println("<td><input type=submit value=Save></td>");
							} else {
								out.println("<td></td>");
							}
							if (showPrematureSubmissionColumn) {
								out.println("<td></td>");
							}
							out.println("</tr></tfoot>");
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
						if (honorGroups) {
							out.println("<h3>Teilnehmende ohne Gruppenzugehörigkeit</h3>");
						} else {
							out.println("<h3>Alle Abgaben</h3>");
						}
						out.println("<div id=\"contentgroup0\">");
						if (honorGroups) {
							out.println("<div class=mid><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&action=grouplist", response) + "\" target=\"_blank\">Druckbare Liste</a></div>");
							if (!task.isADynamicTask() && !task.isSCMCTask() && !task.isClozeTask()) {
								out.println("<div class=mid><a href=\"" + Util.generateHTMLLink(DownloadSubmissionsByGroup.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">Alle Abgaben der Gruppe herunterladen (ZIP-Archiv)</a></div>");
							}
						}
					} else {
						out.println("<h3>Gruppe: " + Util.escapeHTML(group.getName()) + " <a href=\"#\" onclick=\"toggleVisibility('contentgroup" + group.getGid() + "'); return false;\">(+/-)</a></h3>");
						String defaultState = "";
						if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) != 0 && !group.getTutors().isEmpty() && !group.getTutors().contains(participation) && !!"taskWise".equals(task.getTaskGroup().getLecture().getGradingMethod())) {
							defaultState = "style=\"display: none;\"";
						}
						out.println("<div " + defaultState + " id=\"contentgroup" + group.getGid() + "\">");
						out.println("<div class=mid><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&action=grouplist&groupid=" + group.getGid(), response) + "\" target=\"_blank\">Druckbare Liste</a></div>");
						if (task.getTests().stream().anyMatch(t -> t.isForTutors() && !t.isNeedsToRun())) {
							out.println("<div class=mid><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&show=testoverview&groupid=" + group.getGid(), response) + "\">Übersicht der Testergebnisse für die Gruppe</a></div>");
						}
						if (!task.isADynamicTask() && !task.isSCMCTask() && !task.isClozeTask()) {
							out.println("<div class=mid><a href=\"" + Util.generateHTMLLink(DownloadSubmissionsByGroup.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&groupid=" + group.getGid(), response) + "\">Alle Abgaben der Gruppe herunterladen (ZIP-Archiv)</a></div>");
						}
					}
					out.println("<form method=post action=\"" + Util.generateHTMLLink(MarkApproved.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">");
					out.println("<table class=sortable>");
					out.println("<thead>");
					out.println("<tr>");
					out.println("<th>Abgabe von</th>");
					if (showAllColumns) {
						if (task.isADynamicTask()) {
							out.println("<th>Berechnung</th>");
						}
						// show test columns only if the deadline is over
						if (task.getDeadline().isBefore(ZonedDateTime.now())) {
							for (Test test : tests) {
								out.println("<th>" + Util.escapeHTML(test.getTestTitle()) + "</th>");
							}
							for (SimilarityTest similarityTest : task.getSimilarityTests()) {
								String color = "\"\"";
								String hint = "";
								if (similarityTest.getStatus() > 0) {
									color = "red";
									hint = " (läuft)";
								}
								out.println("<th class=sorttable_numeric><span class=" + color + " title=\"Max. Ähnlichkeit\">" + Util.escapeHTML(similarityTest.details() + hint) + "</span></th>");
							}
						}
						out.println("<th>Punkte</th>");
						out.println("<th>Abnehmen</th>");
						if (showPrematureSubmissionColumn) {
							out.println("<th>Status der Abgabe</th>");
						}
					}
					out.println("</tr>");
					out.println("</thead>");
				}
				groupSumOfAllSubmissions++;
				out.println("<tr>");
				String groupAdding = "";
				if (group != null) {
					groupAdding = "&groupid=" + group.getGid();
				}
				if (requestAdapter.isPrivacyMode()) {
					out.println("<td><a href=\"" + Util.generateHTMLLink(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid() + groupAdding, response) + "\">" + submission.getSubmissionid() + "</a></td>");
				} else {
					out.println("<td><a href=\"" + Util.generateHTMLLink(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid() + groupAdding, response) + "\">" + Util.escapeHTML(submission.getSubmitterNames()) + "</a></td>");
				}
				if (showAllColumns) {
					if (task.isADynamicTask()) {
						out.println("<td>" + Util.boolToHTML(task.getDynamicTaskStrategie(session).isCorrect(submission)) + "</td>");
					}
					// show columns only if the results are in the database after the deadline
					if (task.getDeadline().isBefore(ZonedDateTime.now())) {
						for (Test test : tests) {
							Map<Integer, Boolean> testResultsSubmission = testResults.get(submission.getSubmissionid());
							if (testResultsSubmission != null && testResultsSubmission.containsKey(test.getId())) {
								out.println("<td>" + Util.boolToHTML(testResultsSubmission.get(test.getId()), "wird gerade getestet, bitte Seite neu laden") + "</td>");
							} else {
								out.println("<td>n/a</td>");
							}
						}
						Map<Integer, List<Similarity>> similaritiesSubmission = similarities.get(submission.getSubmissionid());
						for (SimilarityTest similarityTest : task.getSimilarityTests()) {
							String users = "";
							int maxSimilarity = 0;
							if (similaritiesSubmission != null) {
								List<Similarity> list = similaritiesSubmission.get(similarityTest.getSimilarityTestId());
								if (list != null) {
									for (Similarity similarity : list) {
										users += Util.escapeHTML(similarity.getSubmissionTwo().getSubmitterNames()) + "\n";
										maxSimilarity = similarity.getPercentage();
									}
								}
							}
							out.println("<td class=similarity><span title=\"" + users + "\">" + maxSimilarity + "</span></td>");
						}
					}
					if (submission.getPoints() != null && submission.getPoints().getPointStatus() != PointStatus.NICHT_BEWERTET.ordinal()) {
						if (submission.getPoints().getPointsOk()) {
							out.println("<td class=\"points" + Util.getPointsCSSClass(submission.getPoints()) + "\" sorttable_customkey=\"" + submission.getPoints().getPoints() + "\">" + Util.showPoints(submission.getPoints().getPointsByStatus(task.getMinPointStep())) + "</td>");
							out.println("<td></td>");
							sumOfPoints += submission.getPoints().getPointsByStatus(task.getMinPointStep());
							groupSumOfPoints += submission.getPoints().getPointsByStatus(task.getMinPointStep());
							sumOfSubmissions++;
							groupSumOfSubmissions++;
						} else {
							out.println("<td sorttable_customkey=\"-" + submission.getPoints().getPoints() + "\" class=\"points" + Util.getPointsCSSClass(submission.getPoints()) + "\">(" + Util.showPoints(submission.getPoints().getPlagiarismPoints(task.getMinPointStep())) + ")</td>");
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
			if (first == false) {
				out.println("<tfoot><tr>");
				if (showAllColumns) {
					out.println("<td colspan=" + (1 + (task.isADynamicTask() ? 1 : 0) + ((task.getDeadline().isBefore(ZonedDateTime.now())) ? tests.size() + task.getSimilarityTests().size() : 0)) + ">Anzahl: " + groupSumOfAllSubmissions + " / Durchschnittspunkte:</td>");
					out.println("<td class=points>" + Util.showPoints(Float.valueOf(groupSumOfPoints / (float) groupSumOfSubmissions).intValue()) + "</td>");
					if (hasUnapprochedPoints) {
						out.println("<td><input type=submit value=Save></td>");
					} else {
						out.println("<td></td>");
					}
					if (showPrematureSubmissionColumn) {
						out.println("<td></td>");
					}
				} else {
					out.println("<td>Anzahl: " + groupSumOfAllSubmissions + "</td>");
				}
				out.println("</tr></tfoot>");
				out.println("</table><p>");
				out.println("</form></div>");
				if (showAllColumns) {
					out.println("<p class=mid><strong>Gesamtanzahl Abgaben:</strong> " + task.getSubmissions().size() + "; <strong>Gesamtdurchschnitt:</strong> " + Util.showPoints(Float.valueOf(sumOfPoints / (float) sumOfSubmissions).intValue()) + "</p>");
				} else {
					out.println("<p class=mid><strong>Gesamtanzahl Abgaben:</strong> " + task.getSubmissions().size() + "</p>");
				}
			}
		}
		template.printTemplateFooter();
	}
}
