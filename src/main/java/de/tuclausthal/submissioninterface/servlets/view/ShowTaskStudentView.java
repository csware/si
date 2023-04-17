/*
 * Copyright 2009-2012, 2020-2023 Sven Strickroth <email@cs-ware.de>
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
import de.tuclausthal.submissioninterface.persistence.dao.PointGivenDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestCountDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointGiven;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.CloseSubmissionByStudent;
import de.tuclausthal.submissioninterface.servlets.controller.DeleteFile;
import de.tuclausthal.submissioninterface.servlets.controller.DownloadModelSolutionFile;
import de.tuclausthal.submissioninterface.servlets.controller.DownloadTaskFile;
import de.tuclausthal.submissioninterface.servlets.controller.PerformStudentTest;
import de.tuclausthal.submissioninterface.servlets.controller.ShowFile;
import de.tuclausthal.submissioninterface.servlets.controller.ShowLecture;
import de.tuclausthal.submissioninterface.servlets.controller.ShowTask;
import de.tuclausthal.submissioninterface.servlets.controller.SubmitSolution;
import de.tuclausthal.submissioninterface.servlets.controller.WebStart;
import de.tuclausthal.submissioninterface.tasktypes.ClozeTaskType;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a task in student view
 *
 * @author Sven Strickroth
 */
@GATEView
public class ShowTaskStudentView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Session session = RequestAdapter.getSession(request);

		Task task = (Task) request.getAttribute("task");
		Submission submission = (Submission) request.getAttribute("submission");
		Participation participation = (Participation) request.getAttribute("participation");
		@SuppressWarnings("unchecked")
		List<String> submittedFiles = (List<String>) request.getAttribute("submittedFiles");
		@SuppressWarnings("unchecked")
		List<String> advisorFiles = (List<String>) request.getAttribute("advisorFiles");
		@SuppressWarnings("unchecked")
		List<String> modelSolutionFiles = (List<String>) request.getAttribute("modelSolutionFiles");

		template.printTemplateHeader(task);
		PrintWriter out = response.getWriter();
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		out.print("<td id=taskdescription>");
		if (task.isClozeTask()) {
			List<String> lastResults = null;
			if (submission != null) {
				lastResults = DAOFactory.ResultDAOIf(session).getResultsForSubmission(submission);
			}
			ClozeTaskType clozeHelper = new ClozeTaskType(task.getDescription(), lastResults, true, submission != null && submission.isPointsVisibleToStudents());
			out.print(clozeHelper.toHTML());
		} else {
			out.print(Util.makeCleanHTML(task.getDescription()));
		}
		out.println("</td>");
		out.println("</tr>");
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
		out.println("<tr>");
		out.println("<th>Startdatum:</th>");
		out.println("<td>" + Util.escapeHTML(dateFormatter.format(task.getStart())) + "</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Enddatum:</th>");
		out.println("<td>" + Util.escapeHTML(dateFormatter.format(task.getDeadline())));
		out.println("</td>");
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
		out.println("</table>");

		if (submission != null) {
			out.println("<p><h2>Informationen zu meiner Abgabe:</h2>");
			out.println("<table>");
			if (submission.getSubmitters().size() > 1) {
				out.println("<tr>");
				out.println("<th>Bearbeitet von:</th>");
				out.println("<td>");
				out.println(Util.textToHTML(submission.getSubmitterNames().replaceAll("; ", "\n")));
				out.println("</td>");
				out.println("</tr>");
			}
			if (submission.getLastModified() != null) {
				out.println("<tr>");
				out.println("<th>Letzte Änderung:</th>");
				out.println("<td>");
				out.println(Util.escapeHTML(dateFormatter.format(submission.getLastModified())));
				out.println("</td>");
				out.println("</tr>");
			}
			if (!submittedFiles.isEmpty()) {
				out.println("<tr>");
				out.println("<th>Besteht aus:</th>");
				out.println("<td>");
				for (String file : submittedFiles) {
					file = file.replace(System.getProperty("file.separator"), "/");
					out.println("<a target=\"_blank\" href=\"" + Util.generateHTMLLink(ShowFile.class.getSimpleName() + "/" + Util.encodeURLPathComponent(file) + "?sid=" + submission.getSubmissionid(), response) + "\">" + Util.escapeHTML(file) + "</a>");
					if (task.getDeadline().isAfter(ZonedDateTime.now()) && !(task.isAllowPrematureSubmissionClosing() && submission.isClosed())) {
						out.println(" (<a href=\"" + Util.generateHTMLLink(DeleteFile.class.getSimpleName() + "/" + Util.encodeURLPathComponent(file) + "?sid=" + submission.getSubmissionid(), response) + "\">löschen</a>)");
					}
					out.println("<br>");
				}
				out.println("</td>");
				out.println("</tr>");
			}
			if (task.isAllowPrematureSubmissionClosing() && !submission.isClosed() && task.getDeadline().isAfter(ZonedDateTime.now())) {
				out.println("<tr>");
				out.println("<th>Vorzeitige finale Abgabe</th>");
				out.println("<td>Diese Abgabe kann vor der Abgabefrist als endgültig abgeschlossen markiert werden.<br><br>");
				out.println("<a href=\"" + Util.generateHTMLLink(CloseSubmissionByStudent.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">Meine Lösung vorzeitig endgültig abgeben</a></td>");
				out.println("</tr>");
			}
			if (task.isADynamicTask()) {
				out.println("<tr>");
				out.println("<th>Berechnete Lösung(en):</th>");
				out.println("<td>");
				DynamicTaskStrategieIf dynamicTask = task.getDynamicTaskStrategie(session);
				String[] resultFields = dynamicTask.getResultFields(false);
				int resultCounter = 0;
				for (String result : dynamicTask.getUserResults(submission)) {
					out.println(Util.escapeHTML(resultFields[resultCounter]) + ": <span class=\"dt_studentsolution\">" + Util.escapeHTML(result) + "</span><br>");
					resultCounter++;
				}
				out.println("</td>");
				out.println("</tr>");
			} else if (task.isSCMCTask()) {
				out.println("<tr>");
				out.println("<th>Antwort:</th>");
				out.println("<td><ul>");

				List<Integer> selected = new ArrayList<>();
				for (String checked : DAOFactory.ResultDAOIf(session).getResultsForSubmission(submission)) {
					selected.add(Integer.parseInt(checked));
				}

				boolean showResults = submission.isPointsVisibleToStudents();
				MCOptionDAOIf mcOptionDAO = DAOFactory.MCOptionDAOIf(session);
				List<MCOption> options = mcOptionDAO.getMCOptionsForTask(task);
				Collections.shuffle(options, new Random(participation.getId()));
				boolean allCorrect = true;
				int i = 0;
				for (MCOption option : options) {
					boolean optionSelected = selected.contains(option.getId());
					boolean correct = (option.isCorrect() && optionSelected) || (!option.isCorrect() && !optionSelected);
					allCorrect &= correct;
					if (task.isSCTask()) {
						out.println("<li><input disabled type=radio " + (optionSelected ? "checked" : "") + " name=check value=" + i + " id=\"check" + i + "\"> <label for=\"check" + i + "\">" + Util.escapeHTML(option.getTitle()) + "</label>");
					} else {
						out.println("<li><input disabled type=checkbox " + (optionSelected ? "checked" : "") + " name=\"check" + i + "\" id=\"check" + i + "\"> <label for=\"check" + i + "\">" + Util.escapeHTML(option.getTitle()) + "</label>");
						if (showResults) {
							out.print("<ul>");
							out.print("<li>");
							if ((option.isCorrect() && optionSelected) || (!option.isCorrect() && !optionSelected)) {
								out.print("Ihre Antwort ist <span class=green>korrekt</span>.");
							} else {
								out.print("Ihre Antwort ist <span class=red>falsch</span>.");
								if (option.isCorrect()) {
									out.print("</li><li>Korrektur: Diese Aussage ist korrekt und hätte ausgewählt werden müssen.");
								} else {
									out.print("</li><li>Korrektur: Diese Aussage ist falsch und hätte nicht ausgewählt werden dürfen.");
								}
							}
							out.print("</li>");
							out.print("</ul>");
						}
					}
					out.println("</li>");
					++i;
				}
				out.println("</ul>");

				if (task.isSCTask() && showResults) {
					if (allCorrect) {
						out.print("Ihre Antwort ist <span class=green>korrekt</span>.");
					} else {
						out.print("Ihre Antwort ist <span class=red>falsch</span>.");
						MCOption correctOption = options.stream().filter(option -> option.isCorrect()).findFirst().orElse(null);
						if (correctOption != null) {
							out.print("<br>Die richtige Antwort lautet: \"" + Util.escapeHTML(correctOption.getTitle()) + "\"");
						}
					}
				}

				out.println("</td>");
				out.println("</tr>");
			}
			if (submission.isPointsVisibleToStudents()) {
				out.println("<tr>");
				out.println("<th>Bewertung:</th>");
				out.println("<td>");
				if (submission.getPoints().getPointStatus() == PointStatus.ABGENOMMEN.ordinal()) {
					out.println(Util.showPoints(submission.getPoints().getPointsByStatus(task.getMinPointStep())) + " von " + Util.showPoints(task.getMaxPoints()) + " Punkt(e)");
					if (submission.getPoints().getDuplicate() != null) {
						out.println(" (Plagiat)");
					}
					if (!task.getPointCategories().isEmpty()) {
						PointGivenDAOIf pointGivenDAO = DAOFactory.PointGivenDAOIf(session);
						Iterator<PointGiven> pointsGivenIterator = pointGivenDAO.getPointsGivenOfSubmission(submission).iterator();
						PointGiven lastPointGiven = null;
						if (pointsGivenIterator.hasNext()) {
							lastPointGiven = pointsGivenIterator.next();
						}
						out.println("<ul>");
						for (PointCategory category : task.getPointCategories()) {
							int issuedPoints = 0;
							while (lastPointGiven != null && category.getPointcatid() > lastPointGiven.getCategory().getPointcatid()) {
								if (pointsGivenIterator.hasNext()) {
									lastPointGiven = pointsGivenIterator.next();
								} else {
									lastPointGiven = null;
									break;
								}
							}
							if (lastPointGiven != null && category.getPointcatid() == lastPointGiven.getCategory().getPointcatid()) {
								issuedPoints = lastPointGiven.getPoints();
							} else if (category.isOptional()) {
								continue;
							}
							out.println("<li>" + Util.showPoints(issuedPoints) + "/" + Util.showPoints(category.getPoints()) + " " + Util.escapeHTML(category.getDescription()) + (category.isOptional() ? " (Bonus)" : "") + "</li>");
						}
						out.println("</ul>");
					}
				} else if (submission.getPoints().getPointStatus() == PointStatus.ABGENOMMEN_FAILED.ordinal()) {
					out.println("0 von " + Util.showPoints(task.getMaxPoints()) + ", Abnahme nicht bestanden");
				} else {
					out.println("0 von " + Util.showPoints(task.getMaxPoints()) + ", nicht abgenommen");
				}
				String marker;
				if (submission.getPoints().getIssuedBy() == null) {
					marker = "GATE";
				} else {
					marker = "<a href=\"mailto:" + Util.escapeHTML(submission.getPoints().getIssuedBy().getUser().getEmail()) + "\">" + Util.escapeHTML(submission.getPoints().getIssuedBy().getUser().getLastNameFirstName()) + "</a>";
				}
				out.println("<p>Vergeben von: " + marker + "</p>");
				out.println("</td>");
				out.println("</tr>");
				if (submission.getPoints().getPublicComment() != null && !"".equals(submission.getPoints().getPublicComment())) {
					out.println("<tr>");
					out.println("<th>Kommentar:</th>");
					out.print("<td class=feedback>");
					out.print(Util.escapeHTML(submission.getPoints().getPublicComment()));
					out.println("</td>");
					out.println("</tr>");
				}
			}
			out.println("</table>");

			out.println("<p>");
			if ("loesung\\.(xmi|zargo|png)".equals(task.getFilenameRegexp())) {
				out.println("<p><div class=mid><a onclick=\"return confirmLink('ArgoUML öffnen')\" href=\"" + Util.generateHTMLLink(WebStart.class.getSimpleName() + "?tool=argouml&taskid=" + task.getTaskid(), response) + "\">ArgoUML öffnen</a></div>");
				out.println("<script>if (!navigator.javaEnabled() || document.applets[0].Version < 1.4){ document.write(\"Sie benötigen mindestens Java 1.6 (JRE), um diese Funktion nutzen zu können. <a href=\"http://www.java.com/\">Download</a>\");</script>");
			} else if ("-".equals(task.getFilenameRegexp()) && task.showTextArea() == false && !task.isSCMCTask() && !task.isClozeTask()) {
				out.println("<div class=mid>Keine Abgabe möglich.</div>");
			} else if (task.getDeadline().isBefore(ZonedDateTime.now())) {
				out.println("<div class=mid>Keine Abgabe mehr möglich.</div>");
			} else if (task.isAllowPrematureSubmissionClosing() && submission.isClosed()) {
				out.println("<div class=mid>Die Abgabe wurde als endgültig abgeschlossen markiert.<br>Eine Veränderung ist jetzt nicht mehr möglich.</div>");
			} else if (task.isSCMCTask() || task.isClozeTask()) {
				out.println("<div class=mid><a href=\"" + Util.generateHTMLLink(SubmitSolution.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">Abgabe bearbeiten</a></div>");
			} else {
				if (!submittedFiles.isEmpty()) {
					out.println("<div class=mid><a href=\"" + Util.generateHTMLLink(SubmitSolution.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">Abgabe bearbeiten/erweitern</a></div>");
				} else {
					out.println("<div class=mid><a href=\"" + Util.generateHTMLLink(SubmitSolution.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">Abgabe starten</a></div>");
				}
			}

			List<Test> tests = DAOFactory.TestDAOIf(session).getStudentTests(task);
			TestCountDAOIf testCountDAO = DAOFactory.TestCountDAOIf(session);
			if (!submittedFiles.isEmpty() && !tests.isEmpty() && task.getDeadline().isAfter(ZonedDateTime.now()) && !(task.isAllowPrematureSubmissionClosing() && submission.isClosed())) {
				out.println("<p><h2>Mögliche Tests:</h2>");
				out.println("<table>");
				for (Test test : tests) {
					out.println("<tr>");
					out.println("<th>" + Util.escapeHTML(test.getTestTitle()) + "</th>");
					out.println("<td>");
					out.println(Util.textToHTML(test.getTestDescription()));
					out.println("</td>");
					out.println("<td>");
					int runnable = testCountDAO.canStillRunXTimes(test, submission);
					if (runnable > 0) {
						out.println("<form method=post action=\"" + Util.generateHTMLLink(PerformStudentTest.class.getSimpleName() + "?testid=" + test.getId(), response) + "\"><input type=submit value=\"Test ausführen\"></form>");
						out.println(" (noch " + runnable + "x ausführbar)");
					} else {
						out.println("Limit erreicht");
					}
					out.println("</td>");
					out.println("</tr>");
				}
				out.println("</table>");
			}
		} else {
			out.println("<p>");
			if ("loesung\\.(xmi|zargo|png)".equals(task.getFilenameRegexp())) {
				out.println("<p><div class=mid><a onclick=\"return confirmLink('ArgoUML öffnen')\" href=\"" + Util.generateHTMLLink(WebStart.class.getSimpleName() + "?tool=argouml&taskid=" + task.getTaskid(), response) + "\">ArgoUML öffnen</a></div>");
				out.println("<script>if (!navigator.javaEnabled() || document.applets[0].Version < 1.4){ document.write(\"Sie benötigen mindestens Java 1.6 (JRE), um diese Funktion nutzen zu können. <a href=\"http://www.java.com/\">Download</a>\");</script>");
			} else if ("-".equals(task.getFilenameRegexp()) && task.showTextArea() == false && !task.isSCMCTask() && !task.isClozeTask()) {
				out.println("<div class=mid>Keine Abgabe möglich.</div>");
			} else if (task.getDeadline().isBefore(ZonedDateTime.now())) {
				out.println("<div class=mid>Keine Abgabe mehr möglich.</div>");
			} else {
				out.println("<div class=mid><a href=\"" + Util.generateHTMLLink(SubmitSolution.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">Abgabe starten</a></div>");
			}
		}

		if (modelSolutionFiles != null && !modelSolutionFiles.isEmpty()) {
			out.println("<h2>Musterlösung:</h2>");
			out.println("<p>Für diese Aufgabe werden Ihnen folgende Dateien als Musterlösung zur Verfügung gestellt:</p>");
			out.println("<ul>");
			for (String file : modelSolutionFiles) {
				out.println("<li><a href=\"" + Util.generateHTMLLink(DownloadModelSolutionFile.class.getSimpleName() + "/" + Util.encodeURLPathComponent(file) + "?taskid=" + task.getTaskid(), response) + "\">" + Util.escapeHTML(file) + "</a></li>");
			}
			out.println("</ul>");
		}

		List<Task> taskGroupTask = task.getTaskGroup().getTasks();
		if (taskGroupTask.size() > 1) {
			out.println("<p><hr>");
			out.println("<div class=mid>");
			int index = taskGroupTask.indexOf(task);
			if (index > 0) {
				out.print("<a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + taskGroupTask.get(index - 1).getTaskid(), response) + "\">&lt; Vorherige Aufgabe</a>");
				out.print(" | ");
			}
			out.print("<a href=\"" + Util.generateHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + task.getTaskGroup().getLecture().getId(), response) + "\">Übersicht</a>");
			if (index < taskGroupTask.size() - 1) {
				out.print(" | <a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + taskGroupTask.get(index + 1).getTaskid(), response) + "\">Nächste Aufgabe &gt;</a>");
			}
			out.println("</div>");
		}

		template.printTemplateFooter();
	}
}
