/*
 * Copyright 2009-2012, 2020-2025 Sven Strickroth <email@cs-ware.de>
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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieIf;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.PointGivenDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointGiven;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.DownloadAsZip;
import de.tuclausthal.submissioninterface.servlets.controller.GotoNextUngradedSubmission;
import de.tuclausthal.submissioninterface.servlets.controller.PerformTest;
import de.tuclausthal.submissioninterface.servlets.controller.ShowFile;
import de.tuclausthal.submissioninterface.servlets.controller.ShowMarkHistory;
import de.tuclausthal.submissioninterface.servlets.controller.ShowSubmission;
import de.tuclausthal.submissioninterface.servlets.controller.ShowUser;
import de.tuclausthal.submissioninterface.servlets.view.fragments.ShowDockerTestResult;
import de.tuclausthal.submissioninterface.servlets.view.fragments.ShowJavaAdvancedIOTestResult;
import de.tuclausthal.submissioninterface.tasktypes.ClozeTaskType;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a submission to a tutor
 *
 * @author Sven Strickroth
 */
@GATEView
public class ShowSubmissionView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		template.addDiffJs();
		template.addKeepAlive();
		template.addHead("<script>function hideCodePreview(id) { document.getElementById('codepreview' + id).style.display='none';document.getElementById('showbtn' + id).style.display='block'; }</script>");

		RequestAdapter requestAdapter = new RequestAdapter(request);
		Session session = requestAdapter.getSession();

		Submission submission = (Submission) request.getAttribute("submission");
		@SuppressWarnings("unchecked")
		List<String> submittedFiles = (List<String>) request.getAttribute("submittedFiles");
		Task task = submission.getTask();

		if (requestAdapter.isPrivacyMode()) {
			template.printTemplateHeader("Abgabe " + String.valueOf(submission.getSubmissionid()), task);
		} else {
			template.printTemplateHeader(submission);
		}
		PrintWriter out = response.getWriter();
		StringBuilder javaScript = new StringBuilder();

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
		if (submission.getLastModified() != null) {
			out.println("<p>Letzte Änderung: " + Util.escapeHTML(dateFormatter.format(submission.getLastModified())) + "</p>");
		}
		if (task.isAllowPrematureSubmissionClosing() && submission.isClosed()) {
			out.println("<p>Abgabe endgültig eingereicht: " + Util.escapeHTML(dateFormatter.format(submission.getClosedTime())) + " von " + Util.escapeHTML(submission.getClosedBy().getUser().getLastNameFirstName()) + "</p>");
		}

		if (!requestAdapter.isPrivacyMode()) {
			for (Participation participation : submission.getSubmitters()) {
				out.println("<a href=\"" + Util.generateHTMLLink(ShowUser.class.getSimpleName() + "?uid=" + participation.getUser().getUid(), response) + "\">" + Util.escapeHTML(participation.getUser().getLastNameFirstName()) + "</a><br>");
			}

			if (!task.isAllowSubmittersAcrossGroups() && submission.getSubmitters().iterator().next().getGroup() != null) {
				out.println("<h2>Gruppe: " + Util.escapeHTML(submission.getSubmitters().iterator().next().getGroup().getName()) + "</h2>");
			}
		}

		if (task.getMaxSubmitters() > 1 && submission.getSubmitters().size() < task.getMaxSubmitters() && (task.isAllowSubmittersAcrossGroups() || submission.getSubmitters().iterator().next().getGroup() != null)) {
			StringBuilder setWithUser = new StringBuilder();
			setWithUser.append("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
			setWithUser.append("<input type=hidden name=sid value=\"" + submission.getSubmissionid() + "\">");
			SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
			setWithUser.append("<p>Fehlt ein(e) PartnerIn: <select name=partnerid size=1 required=required>");
			setWithUser.append("<option value=''></option>");
			int cnt = 0;
			List<Participation> participations;
			if (task.isAllowSubmittersAcrossGroups()) {
				participations = DAOFactory.ParticipationDAOIf(session).getLectureParticipationsOrderedByName(task.getTaskGroup().getLecture());
			} else {
				Participation participation = submission.getSubmitters().iterator().next();
				participations = DAOFactory.ParticipationDAOIf(session).getParticipationsOfGroup(participation.getGroup());
			}
			for (Participation part : participations) {
				if (part.getRoleType().equals(ParticipationRole.NORMAL) && (!task.isAllowSubmittersAcrossGroups() || part.getGroup() == null || !part.getGroup().isSubmissionGroup()) && submissionDAO.getSubmission(task, part.getUser()) == null) {
					cnt++;
					setWithUser.append("<option value=" + part.getId() + ">" + Util.escapeHTML(part.getUser().getLastNameFirstName()) + "</option>");
				}
			}
			setWithUser.append("</select> <input type=submit value= \"Hinzufügen\"></p></form>");
			if (cnt > 0) {
				out.println(setWithUser.toString());
			}
		}

		if ((task.getDeadline().isBefore(ZonedDateTime.now()) || (task.isAllowPrematureSubmissionClosing() && submission.isClosed())) || (task.showTextArea() == false && "-".equals(task.getFilenameRegexp()) && !task.isSCMCTask() && !task.isClozeTask())) {
			out.println("<h2>Bewertung: <a href=\"#\" onclick=\"toggleVisibility('mark'); return false;\">(+/-)</a></h2>");
			out.println("<table id=mark>");
			String oldPublicComment = "";
			String oldInternalComment = "";
			int points = 0;
			boolean pointsOk = false;
			boolean pointsFailed = false;
			boolean pointsBewertet = false;
			boolean isDupe = false;
			String duplicate = "";
			String pointsGivenBy = "";
			String pointsClass = "";
			if (submission.getPoints() != null) {
				oldPublicComment = submission.getPoints().getPublicComment();
				oldInternalComment = submission.getPoints().getInternalComment();
				points = submission.getPoints().getPoints();
				if (submission.getPoints().getDuplicate() != null) {
					isDupe = true;
					duplicate = String.valueOf(submission.getPoints().getDuplicate());
				}
				pointsOk = submission.getPoints().getPointStatus() == PointStatus.ABGENOMMEN.ordinal();
				pointsFailed = submission.getPoints().getPointStatus() == PointStatus.ABGENOMMEN_FAILED.ordinal();
				pointsGivenBy = " (bisher " + Util.showPoints(points) + " Punkte vergeben von: ";
				if (submission.getPoints().getIssuedBy() == null) {
					pointsGivenBy += "GATE, <a href=\"" + Util.generateHTMLLink(ShowMarkHistory.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">History</a>)";
				} else {
					pointsGivenBy += "<a href=\"mailto:" + Util.escapeHTML(submission.getPoints().getIssuedBy().getUser().getEmail()) + "\">" + Util.escapeHTML(submission.getPoints().getIssuedBy().getUser().getLastNameFirstName()) + "</a>, <a href=\"" + Util.generateHTMLLink(ShowMarkHistory.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">History</a>)";
				}
				pointsClass = Util.getPointsCSSClass(submission.getPoints());
				if (submission.getPoints().getPointStatus() == PointStatus.NICHT_BEWERTET.ordinal()) {
					out.println("<tr bgcolor=\"lightgrey\">");
					pointsBewertet = true;
				} else {
					out.println("<tr>");
				}
			} else {
				out.println("<tr class=\"notmarkedyet\">");
				if (!task.getTaskGroup().getLecture().isRequiresAbhnahme()) {
					pointsOk = true;
				}
			}
			out.println("<td>");
			if (!task.getSimilarityTests().isEmpty() && (task.getDeadline().isAfter(ZonedDateTime.now()) || task.getSimilarityTests().stream().anyMatch(test -> test.getStatus() > 0))) {
				out.println("<p class=\"bmid\" style=\"color: #8C1C00\">Achtung: Eine Ähnlichkeitsprüfung wurde noch nicht durchgeführt bzw. ist noch nicht vollständig abgeschlossen.</p>");
			}
			out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
			out.println("<input type=hidden name=sid value=\"" + submission.getSubmissionid() + "\">");
			// attention: quite similar code in MarkEmptyTaskView
			if (!task.getPointCategories().isEmpty()) {
				PointGivenDAOIf pointGivenDAO = DAOFactory.PointGivenDAOIf(session);
				Iterator<PointGiven> pointsGivenIterator = pointGivenDAO.getPointsGivenOfSubmission(submission).iterator();
				PointGiven lastPointGiven = null;
				if (pointsGivenIterator.hasNext()) {
					lastPointGiven = pointsGivenIterator.next();
				}
				out.println("<b>Punkte:</b> " + pointsGivenBy + "<br>");
				out.println("<input type=hidden name=points value=categories>");
				out.println("<ul>");
				for (PointCategory category : task.getPointCategories()) {
					while (lastPointGiven != null && category.getPointcatid() > lastPointGiven.getCategory().getPointcatid()) {
						if (pointsGivenIterator.hasNext()) {
							lastPointGiven = pointsGivenIterator.next();
						} else {
							lastPointGiven = null;
							break;
						}
					}
					int curPoints = 0;
					if (lastPointGiven != null && category.getPointcatid() == lastPointGiven.getCategory().getPointcatid()) {
						curPoints = lastPointGiven.getPoints();
					}
					if (category.getPoints() == task.getMinPointStep()) {
						String checked = "";
						if (curPoints > 0) {
							checked = " checked";
						}
						out.println("<li><input class=\"" + pointsClass + "\" type=checkbox id=\"point_" + category.getPointcatid() + "\" name=\"point_" + category.getPointcatid() + "\" value=\"" + category.getPoints() + "\" " + checked + "> <label for=\"point_" + category.getPointcatid() + "\">" + Util.escapeHTML(category.getDescription()) + " (" + Util.showPoints(category.getPoints()) + ")</label></li>");
					} else {
						out.println("<li><input class=\"" + pointsClass + "\" type=text size=3 id=\"point_" + category.getPointcatid() + "\" name=\"point_" + category.getPointcatid() + "\" value=\"" + Util.showPoints(curPoints) + "\"> <label for=\"point_" + category.getPointcatid() + "\">" + Util.escapeHTML(category.getDescription()) + " (max. " + Util.showPoints(category.getPoints()) + ")</label></li>");
					}
				}
				out.println("</ul>");
			} else {
				out.println("<b>Punkte:</b> <input type=text class=\"" + pointsClass + "\" name=points size=3 value=\"" + Util.showPoints(points) + "\"> (max. " + Util.showPoints(task.getMaxPoints()) + ")" + pointsGivenBy + "<br>");
			}
			out.println("<b>Öffentlicher Kommentar:</b><br><textarea cols=80 rows=8 name=publiccomment>" + Util.escapeHTML(oldPublicComment) + "</textarea><br>");
			out.println("<b>Interner Kommentar:</b>");
			if (requestAdapter.isPrivacyMode()) {
				out.print(" <a id=\"\" href=\"#\" onclick=\"toggleVisibility('stateInternalComment'); return false;\">(+/-)</a><br>");
				out.println("<div id=\"stateInternalComment\" style=\"display:none;\">");
			} else {
				out.print("<br>");
			}
			out.println("<textarea cols=80 rows=8 name=internalcomment id=internalcomment oninput=\"checkInternalComment()\">" + Util.escapeHTML(oldInternalComment) + "</textarea>");
			if (requestAdapter.isPrivacyMode()) {
				out.print("</div>");
			} else {
				out.print("<br>");
			}
			out.println("<b>Best&auml;tigtes Plagiat:</b> <input type=checkbox id=isdupe name=isdupe " + (isDupe ? "checked" : "") + " onclick=\"if (!document.getElementById('isdupe').checked) {document.getElementById('duplicatespan').style.display='none';} else {document.getElementById('duplicatespan').style.display='block';}return true;\" onchange=\"checkInternalComment()\"><span id=duplicatespan " + (isDupe ? "" : " style=\"display:none;\"") + "> <span class=\"red\"><strong>Bei einem Plagiat muss ein interner Kommentar verfasst werden!</strong></span><br><b>Plagiat-Bewertung:</b> <input type=text size=3 pattern=\"[0-9]*\" name=duplicate id=duplicate value=\"" + duplicate + "\"> (0 = keine Punkte, 2 = 1/2 Punktzahl, 3 = 1/3 Punktzahl, ...)<br></span><br>");
			out.println("<input id=\"nbewertet\" type=radio name=pointsstatus value=\"nbewertet\"" + (pointsBewertet ? " checked" : "") + "> <b><label for=\"nbewertet\">Nicht fertig bewertet</label></b><br><input id=\"nabgen\" type=radio name=pointsstatus value=\"nabgen\"" + (!pointsBewertet && !(pointsOk || pointsFailed) ? "checked" : "") + "> <b><label for=\"nabgen\">Nicht abgenommen</label></b><br><input id=\"abgen\" type=radio name=pointsstatus value=\"ok\"" + (pointsOk ? "checked" : "") + "> <b><label for=\"abgen\">Abgenommen (ok)</label></b> <br><input id=\"failed\" type=radio name=pointsstatus value=\"failed\" " + (pointsFailed ? "checked" : "") + "> <b><label for=\"failed\">Abnahme nicht bestanden</label></b></p>");
			out.println("<div style=\"display:none;\" id=statehelp><b>Hilfe:</b><dl><dt>Nicht fertig bewertet</dt><dd>Zeigt diese Abgabe in allen Listen als &quot;n/a&quot; bzw. &quot;noch unbenotet&quot; an (auch den Studierenden).</dd><dt>Nicht abgenommen</dt><dd>Wird in den Listen eingeklammert angezeigt, Punkte werden nicht gezählt, bei Studierendem steht &quot;0, nicht abgenommen&quot;</dd><dt>Abgenommen (ok)</dt><dd>Aufgabe wurde abschließend bewertet, Punkte werden regulär gezählt (sofern kein Plagiat; ggf. wird dem Studierenden &quot;Plagiat&quot; angezeigt)</dd><dt>Abnahme nicht bestanden</dt><dd>Aufgabe wurde abschließend bewertet, aber es werden keine Punkte gezählt (dem Studierenden wird &quot;0, Abnahme nicht bestanden&quot; angezeigt, überschreibt die Plagiat Option).</dd></dl></div>");
			if (request.getParameter("groupid") != null && Util.parseInteger(request.getParameter("groupid"), 0) > 0) {
				out.println("<input type=hidden name=groupid value=" + Util.parseInteger(request.getParameter("groupid"), 0) + ">");
			}
			out.println("<input type=submit name=submit value=Speichern> <a href=\"#\" onclick=\"toggleVisibility('statehelp'); return false;\">(?)</a>");
			if (!requestAdapter.isPrivacyMode()) {
				out.println("<input type=submit name=submit value=\"Speichern &amp; nächste\"> <input type=submit name=submit value=\"Speichern &amp; vorherige\">");
				String groupAdding = "";
				if (request.getParameter("groupid") != null && Util.parseInteger(request.getParameter("groupid"), 0) > 0) {
					groupAdding = "&groupid=" + Util.parseInteger(request.getParameter("groupid"), 0);
				}
				out.println("- <a href=\"" + Util.generateHTMLLink(GotoNextUngradedSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid() + "&taskid=" + task.getTaskid() + groupAdding, response) + "\">nächste</a>");
				out.println("- <a href=\"" + Util.generateHTMLLink(GotoNextUngradedSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid() + "&taskid=" + task.getTaskid() + groupAdding + "&prev", response) + "\">vorherige</a>");
			}
			out.println("</form>");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table>");

			out.println("<p>");
		}

		if (!submission.getSimilarSubmissions().isEmpty()) {
			out.println("<h2>Ähnliche Abgaben: <a href=\"#\" onclick=\"toggleVisibility('similarSubmissions'); return false;\">(+/-)</a></h2>");
			if (requestAdapter.isPrivacyMode()) {
				out.println("<table id=similarSubmissions style=\"display:none;\">");
			} else {
				out.println("<table id=similarSubmissions>");
			}
			out.println("<thead>");
			out.println("<tr>");
			for (SimilarityTest similarityTest : task.getSimilarityTests()) {
				out.println("<th><span title=\"Ähnlichkeit zu\">" + Util.escapeHTML(similarityTest.details()) + "</span></th>");
			}
			out.println("</tr>");
			out.println("</thead>");
			out.println("<tr>");
			for (SimilarityTest similarityTest : task.getSimilarityTests()) {
				out.println("<td>");
				out.println("<table>");
				out.println("<tr>");
				out.println("<th>Abgabe von</th>");
				out.println("<th>Ähnlichkeit</th>");
				out.println("</tr>");
				for (Similarity similarity : DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(similarityTest, submission)) {
					out.println("<tr>");
					out.println("<td><a href=\"" + Util.generateHTMLLink(ShowSubmission.class.getSimpleName() + "?sid=" + similarity.getSubmissionTwo().getSubmissionid(), response) + "\">" + Util.escapeHTML(similarity.getSubmissionTwo().getSubmitterNames()) + "</a></td>");
					out.println("<td class=points>" + similarity.getPercentage() + "%</td>");
					out.println("</tr>");
				}
				out.println("</table>");
				out.println("</td>");
			}
			out.println("</tr>");
			out.println("</table><p>");
		}

		if (!submission.getTestResults().isEmpty()) {
			out.println("<h2>Tests: <a href=\"#\" onclick=\"toggleVisibility('tests'); return false;\">(+/-)</a></h2>");
			out.println("<ul id=tests>");
			for (TestResult testResult : submission.getTestResults()) {
				out.println("<li>" + Util.escapeHTML(testResult.getTest().getTestTitle()) + "<br>");
				out.println("<b>Erfolgreich:</b> " + Util.boolToHTML(testResult.getPassedTest()));
				if (!testResult.getTestOutput().isEmpty()) {
					if (testResult.getTest() instanceof JavaAdvancedIOTest jaiot) {
						out.println("<br>");
						ShowJavaAdvancedIOTestResult.printTestResults(out, jaiot, testResult.getTestOutput(), false, javaScript);
					} else if (testResult.getTest() instanceof DockerTest dt) {
						out.println("<br>");
						ShowDockerTestResult.printTestResults(out, dt, testResult.getTestOutput(), false, javaScript);
					} else {
						out.println("<br><textarea id=\"testresult" + testResult.getId() + "\" cols=80 rows=15>" + Util.escapeHTML(testResult.getTestOutput()) + "</textarea>");
					}
				}
				out.println("</li>");
			}
			out.println("</ul>");
		}

		if (task.isSCMCTask()) {
			if (task.isSCTask()) {
				out.println("<h2>Single Choice: <a href=\"#\" onclick=\"toggleVisibility('mctask'); return false;\">(+/-)</a></h2>");
			} else {
				out.println("<h2>Multiple Choice: <a href=\"#\" onclick=\"toggleVisibility('mctask'); return false;\">(+/-)</a></h2>");
			}
			out.println("<ul id=mctask>");
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
					out.print("<ul>");
					out.print("<li>");
					if (correct) {
						out.print("Die Antwort des Studierenden ist <span class=green>korrekt</span>.");
					} else {
						out.print("Die Antwort des Studierenden ist <span class=red>falsch</span>.");
					}
					out.print("</li>");
					out.print("</ul>");
				}
				out.print("</li>");
				++i;
			}
			out.println("</ul></li>");
			out.println("<li><b>Korrekt beantwortet:</b> " + Util.boolToHTML(allCorrect) + "</li>");
			out.println("</ul>");
		} else if (task.isClozeTask()) {
			List<String> results = DAOFactory.ResultDAOIf(session).getResultsForSubmission(submission);
			ClozeTaskType clozeHelper = new ClozeTaskType(task.getDescription(), results, true, true);
			out.println("<h2>Cloze: <a href=\"#\" onclick=\"toggleVisibility('cloze'); return false;\">(+/-)</a></h2>");
			out.println("<ul id=cloze>");
			out.println("<li><b>Eingaben:</b><ul>");
			int i = 0;
			for (String result : results) {
				out.print("<li><span class=\"cloze_studentsolution\">" + Util.escapeHTML(result) + "</span>");
				if (i < clozeHelper.getClozeEntries() && clozeHelper.isAutoGradeAble(i)) {
					out.print(" (" + Util.escapeHTML(clozeHelper.getCorrect(i)) + ") ➜ " + Util.showPoints(clozeHelper.calculatePoints(i, result)) + "/" + Util.showPoints(clozeHelper.maxPoints(i)) + " Punkt(e)");
				}
				out.println("</li>");
				++i;
			}
			out.println("</ul></li>");
			out.println("<li><b>Berechnete Punkte:</b> " + Util.showPoints(clozeHelper.calculatePoints(results)) + "</li>");
			out.println("<li><div id=taskdescription>" + clozeHelper.toHTML() + "</div></li>");
			out.println("</ul>");
		} else if (task.isADynamicTask()) {
			out.println("<h2>Dynamische Aufgabe: <a href=\"#\" onclick=\"toggleVisibility('dynamictask'); return false;\">(+/-)</a></h2>");
			DynamicTaskStrategieIf dynamicTask = task.getDynamicTaskStrategie(session);
			out.println("<ul id=dynamictask>");
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

			out.println("<h2>Dateien: <a href=\"#\" onclick=\"toggleVisibility('files'); return false;\">(+/-)</a></h2>");
			out.println("<div id=files class=mid>");
			out.println("<p><a href=\"" + Util.generateHTMLLink(DownloadAsZip.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), response) + "\">alles als .zip herunterladen</a></p>");
			Pattern pattern = null;
			if (!"".equals(task.getFeaturedFiles().trim())) {
				if (task.getFeaturedFiles().startsWith("^")) {
					pattern = Pattern.compile("^(" + task.getFeaturedFiles().substring(1) + ")$");
				} else {
					pattern = Pattern.compile("(?:^|.*/)(" + task.getFeaturedFiles() + ")$");
				}
			}
			int id = 0;
			for (String file : submittedFiles) {
				file = file.replace(System.getProperty("file.separator"), "/");
				if (ShowFile.isInlineAble(file.toLowerCase())) {
					out.println("<h3 class=files>" + Util.escapeHTML(file) + " <a id=\"showbtn" + id + "\" style=\"display: none;\" href=\"#\" onclick='document.getElementById(\"codepreview" + id + "\").style.display=\"block\";document.getElementById(\"showbtn" + id + "\").style.display=\"none\";return false;'>(show)</a></h3>");
					out.println("<div id=\"codepreview" + id + "\" class=\"mid\">");
					out.println("<div class=\"inlinemenu\">");
					out.println("<a id=\"hidebtn" + id + "\" href=\"#\" onclick='hideCodePreview(\"" + id + "\");return false;'>(hide)</a>");
					out.println("</div>");
					out.println("<div id=\"resizablecodepreview" + id + "\" class=\"mid inlinefile resizer\">");
					out.println("<iframe name=\"iframe" + id + "\" id=\"iframe" + id + "\" scrolling=\"yes\" width=\"100%\" height=\"100%\" src=\"" + Util.generateHTMLLink(ShowFile.class.getSimpleName() + "/" + Util.encodeURLPathComponent(file) + "?sid=" + submission.getSubmissionid(), response) + "\"></iframe></div>");
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
		if (javaScript.length() != 0) {
			out.println("<script>" + javaScript.toString() + "</script>");
		}
		template.printTemplateFooter();
	}
}
