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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieIf;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.ChecklistTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.ChecklistTestCheckItem;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.DownloadAsZip;
import de.tuclausthal.submissioninterface.servlets.controller.PerformTest;
import de.tuclausthal.submissioninterface.servlets.controller.ShowFile;
import de.tuclausthal.submissioninterface.servlets.controller.ShowMarkHistory;
import de.tuclausthal.submissioninterface.servlets.controller.ShowSubmission;
import de.tuclausthal.submissioninterface.servlets.controller.ShowTask;
import de.tuclausthal.submissioninterface.servlets.controller.ShowTaskAllSubmissions;
import de.tuclausthal.submissioninterface.servlets.view.fragments.ShowDockerTestResult;
import de.tuclausthal.submissioninterface.servlets.view.fragments.ShowJavaAdvancedIOTestResult;
import de.tuclausthal.submissioninterface.tasktypes.ClozeTaskType;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.ParameterHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying all submissions for a task
 * @author Sven Strickroth
 */
@GATEView
public class ShowTaskTutorAllSubmissionsView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		Task task = (Task) request.getAttribute("task");

		Template template = TemplateFactory.getTemplate(request, response);
		template.printTemplateHeader("Alle Abgaben", task);
		PrintWriter out = response.getWriter();
		out.println("<h2><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">" + Util.escapeHTML(task.getTitle()) + "</a></h2>");
		ParameterHelper parameterHelper = new ParameterHelper(request, ShowTaskAllSubmissions.class.getSimpleName() + "?taskid=" + task.getTaskid());
		boolean skipFullScore = parameterHelper.register(new ParameterHelper.Parameter("skipFullScore", "Abgaben mit voller Punktzahl"));
		boolean skipManuallyGraded = parameterHelper.register(new ParameterHelper.Parameter("skipManuallyGraded", "manuell bewertete Abgaben"));
		boolean showGrading = parameterHelper.register(new ParameterHelper.Parameter("showGrading", "Bewertungen"));
		boolean hideAutoGraded = parameterHelper.register(new ParameterHelper.Parameter("skipAutoGraded", "automatische Bewertungen"));
		String wrap = parameterHelper.register(new ParameterHelper.Parameter("wrap", "Textfelder", "automatisch umbrechen", "nicht automatisch umbrechen")) ? "&wrap=yes" : "";
		parameterHelper.constructLinks(response, out);

		out.println("<table>");
		out.println("<tr>");
		out.println("<td id=taskdescription>" + Util.makeCleanHTML(task.getDescription()) + "</td>");
		out.println("</tr>");
		out.println("</table>");

		int id = 0;
		StringBuilder javaScript = new StringBuilder();
		List<ChecklistTest> checklistTests = task.getTests().stream().filter(test -> test instanceof ChecklistTest).map(test -> (ChecklistTest) test).collect(Collectors.toList());
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
			out.println("<h3><a href=\"" + Util.generateHTMLLink(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">" + submission.getSubmissionid() + "</a></h3>");

			if (!submission.getTestResults().isEmpty() || !checklistTests.isEmpty()) {
				out.println("<h2>Tests:</h2>");
				out.println("<ul>");
				for (TestResult testResult : submission.getTestResults()) {
					out.println("<li>" + Util.escapeHTML(testResult.getTest().getTestTitle()) + "<br>");
					out.println("<b>Erfolgreich:</b> " + Util.boolToHTML(testResult.getPassedTest()));
					if (!testResult.getTestOutput().isEmpty()) {
						if (testResult.getTest() instanceof JavaAdvancedIOTest) {
							out.println("<br>");
							ShowJavaAdvancedIOTestResult.printTestResults(out, (JavaAdvancedIOTest) testResult.getTest(), testResult.getTestOutput(), false, javaScript);
						} else if (testResult.getTest() instanceof DockerTest) {
							out.println("<br>");
							ShowDockerTestResult.printTestResults(out, (DockerTest) testResult.getTest(), testResult.getTestOutput(), false, javaScript);
						} else {
							out.println("<br><textarea cols=80 rows=15>" + Util.escapeHTML(testResult.getTestOutput()) + "</textarea>");
						}
					}
					out.println("</li>");
				}
				if (!checklistTests.isEmpty()) {
					for (ChecklistTest test : checklistTests) {
						out.println("<li>Checkliste: " + Util.escapeHTML(test.getTestTitle()) + "<br>");
						out.println("<ul>");
						for (ChecklistTestCheckItem item : test.getCheckItems()) {
							out.println("<li>" + Util.escapeHTML(item.getTitle()) + "</li>");
						}
						out.println("</ul>");
					}
				}
				out.println("</ul>");
			}

			if (task.isSCMCTask()) {
				if (task.isSCTask()) {
					out.println("<h2>Single Choice</h2>");
				} else {
					out.println("<h2>Multiple Choice</h2>");
				}
				out.println("<ul>");
				out.println("<li><b>Optionen:</b><ul>");
				List<Integer> selected = new ArrayList<>();
				for (String checked : DAOFactory.ResultDAOIf(session).getResultsForSubmission(submission)) {
					selected.add(Integer.parseInt(checked));
				}
				List<MCOption> options = DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(task);
				boolean allCorrect = true;
				int i = 0;
				for (MCOption option : options) {
					boolean optionSelected = selected.contains(option.getId());
					boolean correct = (option.isCorrect() && optionSelected) || (!option.isCorrect() && !optionSelected);
					allCorrect &= correct;
					if (task.isSCTask()) {
						out.println("<li><input disabled type=radio " + (optionSelected ? "checked" : "") + " name=check value=" + i + " id=\"check" + i + "\"> <label for=\"check" + i + "\">" + Util.escapeHTML(option.getTitle()) + (option.isCorrect() ? " (richtige Antwort)" : "") + "</label>");
					} else {
						out.println("<li><input disabled type=checkbox " + (optionSelected ? "checked" : "") + " name=\"check" + i + "\" id=\"check" + i + "\"> <label for=\"check" + i + "\" class=" + (correct ? "mccorrect" : "mcwrong") + ">" + Util.escapeHTML(option.getTitle()) + "</label>");
					}
					out.print("<ul>");
					out.print("<li>");
					if (correct) {
						out.print("Die Antwort des Studierenden ist <span class=green>korrekt</span>.");
					} else {
						out.print("Die Antwort des Studierenden ist <span class=red>falsch</span>.");
					}
					out.print("</li>");
					out.print("</ul>");
					out.print("</li>");
					++i;
				}
				out.println("</ul></li>");
				out.println("<li><b>Korrekt beantwortet:</b> " + Util.boolToHTML(allCorrect) + "</li>");
				out.println("</ul>");
			} else if (task.isClozeTask()) {
				List<String> results = DAOFactory.ResultDAOIf(session).getResultsForSubmission(submission);
				ClozeTaskType clozeHelper = new ClozeTaskType(task.getDescription(), results, true, true);
				out.println("<h2>Cloze</h2>");
				out.println("<ul>");
				out.println("<li><b>Eingaben:</b><ul>");
				int i = 0;
				for (String result : results) {
					out.print("<li><span class=\"cloze_studentsolution\">" + Util.escapeHTML(result) + "</span>");
					if (i < clozeHelper.getClozeEntries() && clozeHelper.isAutoGradeAble(i)) {
						out.print(" (" + Util.escapeHTML(clozeHelper.getCorrect(i)) + ") ➜ " + (clozeHelper.calculatePoints(i, result) > 0 ? "<span class=green>ok</span>" : "<span class=red>nicht ok</span>"));
					}
					out.println("</li>");
					++i;
				}
				out.println("</ul></li>");
				out.println("<li><b>Berechnete Punkte:</b> " + Util.showPoints(clozeHelper.calculatePoints(results)) + "</li>");
				out.println("<li><div id=taskdescription>" + clozeHelper.toHTML() + "</div></li>");
				out.println("</ul>");
			} else if (task.isADynamicTask()) {
				out.println("<h2>Dynamische Aufgabe</h2>");
				DynamicTaskStrategieIf dynamicTask = task.getDynamicTaskStrategie(session);
				out.println("<ul>");
				out.println("<li><b>Benutzer-Werte:</b><br>");
				int variableCounter = 0;
				for (TaskNumber tn : dynamicTask.getVariables(submission)) {
					out.print(Util.escapeHTML(dynamicTask.getVariableNames()[variableCounter]) + ": " + Util.escapeHTML(tn.getNumber()));
					if (!tn.getNumber().equals(tn.getOrigNumber())) {
						out.print(" (" + Util.escapeHTML(tn.getOrigNumber()) + ")");
					}
					out.println("<br>");
					variableCounter++;
				}
				out.println("</li>");
				out.println("<li><b>Lösung:</b><br>");
				List<String> correctResults = dynamicTask.getCorrectResults(submission, true);
				List<String> studentResults = dynamicTask.getUserResults(submission);
				String[] resultFields = dynamicTask.getResultFields(true);
				int resultCounter = 0;
				int studentResultCounter = 0;
				for (String result : correctResults) {
					if (resultFields[resultCounter].startsWith("-")) {
						out.println(Util.escapeHTML(resultFields[resultCounter]) + ": (" + Util.escapeHTML(result) + ")<br>");
					} else {
						out.println(Util.escapeHTML(resultFields[resultCounter]) + ": <span class=\"dt_studentsolution\">" + Util.escapeHTML(studentResults.get(studentResultCounter)) + "</span> (" + Util.escapeHTML(result) + ")<br>");
						studentResultCounter++;
					}
					resultCounter++;
				}
				out.println("</li>");
				out.println("<li><b>Ist korrekt:</b> " + Util.boolToHTML(dynamicTask.isCorrect(submission)) + "</li>");
				out.println("</ul>");
			}

			List<String> submittedFiles = Util.listFilesAsRelativeStringList(new File(Configuration.getInstance().getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator")));
			if (!submittedFiles.isEmpty()) {
				if (task.getDeadline().isAfter(ZonedDateTime.now()) && task.getTests().stream().anyMatch(atest -> atest.TutorsCanRun())) {
					out.println("<FORM class=mid method=POST action=\"" + Util.generateHTMLLink(PerformTest.class.getSimpleName(), response) + "\">");
					out.println("<p>Test auf Abgabe ausführen: <select name=testid size=1 required>");
					for (Test test : task.getTests()) {
						if (!test.TutorsCanRun()) {
							continue;
						}
						out.println("<option value=" + test.getId() + ">" + Util.escapeHTML(test.getTestTitle()) + (test.isForTutors() ? " (Tutortest)" : "") + "</option>");
					}
					out.println("</select>");
					out.println("<INPUT TYPE=hidden NAME=sid value=" + submission.getSubmissionid() + ">");
					out.println("<INPUT TYPE=submit VALUE=Testen></p>");
					out.println("</FORM>");
				}

				out.println("<h2>Dateien:</h2>");
				out.println("<div class=mid>");
				out.println("<p><a href=\"" + Util.generateHTMLLink(DownloadAsZip.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">alles als .zip herunterladen</a></p>");
				Pattern pattern = null;
				if (!"".equals(task.getFeaturedFiles().trim())) {
					if (task.getFeaturedFiles().startsWith("^")) {
						pattern = Pattern.compile("^(" + task.getFeaturedFiles().substring(1) + ")$");
					} else {
						pattern = Pattern.compile("(?:^|.*/)(" + task.getFeaturedFiles() + ")$");
					}
				}
				for (String file : submittedFiles) {
					file = file.replace(System.getProperty("file.separator"), "/");
					if (ShowFile.isInlineAble(file.toLowerCase())) {
						out.println("<h3 class=files>" + Util.escapeHTML(file) + " <a id=\"showbtn" + id + "\" style=\"display: none;\" href=\"#\" onclick='document.getElementById(\"codepreview" + id + "\").style.display=\"block\";document.getElementById(\"showbtn" + id + "\").style.display=\"none\";return false;'>(show)</a></h3>");
						out.println("<div id=\"codepreview" + id + "\" class=\"mid\">");
						out.println("<div class=\"inlinemenu\">");
						out.println("<a id=\"hidebtn" + id + "\" href=\"#\" onclick='hideCodePreview(\"" + id + "\");return false;'>(hide)</a>");
						out.println("</div>");
						out.println("<div id=\"resizablecodepreview" + id + "\" class=\"mid inlinefile resizer\">");
						out.println("<iframe name=\"iframe" + id + "\" id=\"iframe" + id + "\" scrolling=\"yes\" width=\"100%\" height=\"100%\" src=\"" + Util.generateHTMLLink(ShowFile.class.getSimpleName() + "/" + Util.encodeURLPathComponent(file) + "?sid=" + submission.getSubmissionid() + wrap, response) + "\"></iframe></div>");
						out.println("</div>");
						if (pattern != null) {
							Matcher m = pattern.matcher(file);
							if (!m.matches()) {
								javaScript.append("hideCodePreview('" + id + "');");
							}
						}
					} else {
						out.println("<h3 class=files>" + Util.escapeHTML(file) + "</h3>");
					}
					out.println("<a href=\"" + Util.generateHTMLLink(ShowFile.class.getSimpleName() + "/" + Util.encodeURLPathComponent(file) + "?download=true&sid=" + submission.getSubmissionid(), response) + "\">Download " + Util.escapeHTML(file) + "</a><p>");
					id++;
				}
				out.println("</div>");
			}
			if (showGrading) {
				out.println("<h2>Bewertung:</h2>");
				out.println("<p>");
				if (submission.getPoints() != null && submission.getPoints().getPointStatus() != PointStatus.NICHT_BEWERTET.ordinal()) {
					if (submission.getPoints().getPointsOk()) {
						out.print("Punkte: <span class=\"points" + Util.getPointsCSSClass(submission.getPoints()) + "\">" + Util.showPoints(submission.getPoints().getPointsByStatus(task.getMinPointStep())) + "</span>");
					} else {
						out.print("Punkte: <span class=\"points" + Util.getPointsCSSClass(submission.getPoints()) + "\">(" + Util.showPoints(submission.getPoints().getPlagiarismPoints(task.getMinPointStep())) + ")</span>");
					}
					if (submission.getPoints().getIssuedBy() == null) {
						out.println(" von GATE, <a href=\"" + Util.generateHTMLLink(ShowMarkHistory.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">History</a>)");
					} else {
						out.println(" von <a href=\"mailto:" + Util.escapeHTML(submission.getPoints().getIssuedBy().getUser().getEmail()) + "\">" + Util.escapeHTML(submission.getPoints().getIssuedBy().getUser().getLastNameFirstName()) + "</a>, <a href=\"" + Util.generateHTMLLink(ShowMarkHistory.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">History</a>)");
					}
				} else {
					if (task.getDeadline().isAfter(ZonedDateTime.now())) {
						out.println("<span class=points>(noch unbewertet)</span>");
					} else {
						out.println("<span class=points>noch unbewertet</span>");
					}
				}
				out.println("</p>");
				if (submission.getPoints() != null && submission.getPoints().getPublicComment() != null && !submission.getPoints().getPublicComment().isBlank()) {
					out.println("<p>Öffentlicher Kommentar:<br><div class=\"comments_archive\">" + Util.escapeHTML(submission.getPoints().getPublicComment()) + "</div></p>");
				}
				if (submission.getPoints() != null && submission.getPoints().getInternalComment() != null && !submission.getPoints().getInternalComment().isBlank()) {
					out.println("<p>Interner Kommentar:<br><div class=\"comments_archive\">" + Util.escapeHTML(submission.getPoints().getInternalComment()) + "</div></p>");
				}
			}

			out.println("<hr>");
		}
		if (javaScript.length() != 0) {
			out.println("<script>" + javaScript.toString() + "</script>");
		}
		template.printTemplateFooter();
	}
}
