/*
 * Copyright 2009-2012, 2020 Sven Strickroth <email@cs-ware.de>
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieIf;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.PointGivenDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
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
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.ShowFile;
import de.tuclausthal.submissioninterface.servlets.view.fragments.ShowJavaAdvancedIOTestResult;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a submission to a tutor
 * @author Sven Strickroth
 */
public class ShowSubmissionView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		template.addJQuery();
		template.addKeepAlive();
		template.addHead("<script type=\"text/javascript\">function hideCodePreview(id) { $(\"#codepreview\" + id).hide();$(\"#showbtn\" + id).show(); } function testResultSetup(id) { $(\"#testresult\" + id).resizable({ handles: 'se' }); } function iframeSetup(id) { $(\"#resizablecodepreview\" + id).resizable({ helper: 'ui-resizable-helper', minWidth: 800, minHeight: 100, handles: 'se' }); }</script>");

		RequestAdapter requestAdapter = new RequestAdapter(request);
		Session session = requestAdapter.getSession();

		Submission submission = (Submission) request.getAttribute("submission");
		List<String> submittedFiles = (List<String>) request.getAttribute("submittedFiles");
		Task task = submission.getTask();

		template.printTemplateHeader(submission);
		PrintWriter out = response.getWriter();
		StringBuffer javaScript = new StringBuffer();

		if (submission.getLastModified() != null) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			out.println("<p>Letzte Änderung: " + Util.escapeHTML(dateFormatter.format(submission.getLastModified())) + "</p>");
		}

		for (Participation participation : submission.getSubmitters()) {
			out.println("<a href=\"" + response.encodeURL("ShowUser?uid=" + participation.getUser().getUid()) + "\">" + Util.escapeHTML(participation.getUser().getFullName()) + "</a><br>");
		}

		if (!task.isAllowSubmittersAcrossGroups() && submission.getSubmitters().iterator().next().getGroup() != null) {
			out.println("<h2>Gruppe: " + submission.getSubmitters().iterator().next().getGroup().getName() + "</h2>");
		}

		if (task.getMaxSubmitters() > 1 && submission.getSubmitters().size() < task.getMaxSubmitters() && (task.isAllowSubmittersAcrossGroups() || submission.getSubmitters().iterator().next().getGroup() != null)) {
			StringBuffer setWithUser = new StringBuffer();
			setWithUser.append("<form action=\"?\" method=post>");
			setWithUser.append("<input type=hidden name=sid value=\"" + submission.getSubmissionid() + "\">");
			SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
			setWithUser.append("<p>Fehlt ein(e) PartnerIn: <select name=partnerid size=1 required=required>");
			setWithUser.append("<option value=''></option>");
			int cnt = 0;
			List<Participation> participations;
			if (task.isAllowSubmittersAcrossGroups()) {
				participations = DAOFactory.ParticipationDAOIf(session).getLectureParticipations(task.getTaskGroup().getLecture());
			} else {
				Participation participation = submission.getSubmitters().iterator().next();
				participations = DAOFactory.ParticipationDAOIf(session).getParticipationsOfGroup(participation.getGroup());
			}
			for (Participation part : participations) {
				if (part.getRoleType().equals(ParticipationRole.NORMAL) && (!task.isAllowSubmittersAcrossGroups() || part.getGroup() == null || !part.getGroup().isSubmissionGroup()) && submissionDAO.getSubmission(task, part.getUser()) == null) {
					cnt++;
					setWithUser.append("<option value=" + part.getId() + ">" + Util.escapeHTML(part.getUser().getFullName()) + "</option>");
				}
			}
			setWithUser.append("</select> <input type=submit value= \"Hinzufügen\"></p></form>");
			if (cnt > 0) {
				out.println(setWithUser.toString());
			}
		}

		if (task.getDeadline().before(Util.correctTimezone(new Date())) || (task.isShowTextArea() == false && "-".equals(task.getFilenameRegexp()))) {
			out.println("<h2>Bewertung: <a href=\"#\" onclick=\"$('#mark').toggle(); return false;\">(+/-)</a></h2>");
			out.println("<table id=mark class=border>");
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
				pointsGivenBy = " (bisher " + Util.showPoints(points) + " Punkte  vergeben von: <a href=\"mailto:" + Util.escapeHTML(submission.getPoints().getIssuedBy().getUser().getFullEmail()) + "\">" + Util.escapeHTML(submission.getPoints().getIssuedBy().getUser().getFullName()) + "</a>, <a href=\"" + response.encodeURL("ShowMarkHistory?sid=" + submission.getSubmissionid()) + "\">History</a>)";
				pointsClass = Util.getPointsCSSClass(submission.getPoints());
				if (submission.getPoints().getPointStatus() == PointStatus.NICHT_BEWERTET.ordinal()) {
					out.println("<tr bgcolor=\"lightgrey\">");
					pointsBewertet = true;
				} else {
					out.println("<tr>");
				}
			} else {
				out.println("<tr bgcolor=\"lightgrey\">");
				if (!task.getTaskGroup().getLecture().isRequiresAbhnahme()) {
					pointsOk = true;
				}
			}
			out.println("<td>");
			out.println("<form action=\"?\" method=post>");
			out.println("<input type=hidden name=sid value=\"" + submission.getSubmissionid() + "\">");
			// attention: quite similar code in MarkEmptyTaskView
			if (task.getPointCategories().size() > 0) {
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
				out.print(" <a id=\"\" href=\"#\" onclick=\"$('#stateInternalComment').toggle(); return false;\">(+/-)</a><br>");
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
			out.println("<b>Best&auml;tigtes Plagiat:</b> <input type=checkbox id=isdupe name=isdupe " + (isDupe ? "checked" : "") + " onclick=\"if (!$('#isdupe').attr('checked')) {$('#duplicatespan').hide();} else {$('#duplicatespan').show();}return true;\" onchange=\"checkInternalComment()\"><span id=duplicatespan " + (isDupe ? "" : " style=\"display:none;\"") + "> <span class=\"red\"><strong>Bei einem Plagiat muss ein interner Kommentar verfasst werden!</strong></span><br><b>Plagiat-Bewertung:</b> <input type=text size=3 pattern=\"[0-9]*\" name=duplicate id=duplicate value=\"" + duplicate + "\"> (0 = keine Punkte, 2 = 1/2 Punktzahl, 3 = 1/3 Punktzahl, ...)<br></span><br>");
			out.println("<input id=\"nbewertet\" type=radio name=pointsstatus value=\"nbewertet\"" + (pointsBewertet ? " checked" : "") + "> <b><label for=\"nbewertet\">Nicht fertig bewertet</label></b><br><input id=\"nabgen\" type=radio name=pointsstatus value=\"nabgen\"" + (!pointsBewertet && !(pointsOk || pointsFailed) ? "checked" : "") + "> <b><label for=\"nabgen\">Nicht abgenommen</label></b><br><input id=\"abgen\" type=radio name=pointsstatus value=\"ok\"" + (pointsOk ? "checked" : "") + "> <b><label for=\"abgen\">Abgenommen (ok)</label></b> <br><input id=\"failed\" type=radio name=pointsstatus value=\"failed\" " + (pointsFailed ? "checked" : "") + "> <b><label for=\"failed\">Abnahme nicht bestanden</label></b></p>");
			out.println("<div style=\"display:none;\" id=statehelp><b>Hilfe:</b><dl><dt>Nicht fertig bewertet</dt><dd>Zeigt diese Abgabe in allen Listen als &quot;n/a&quot; bzw. &quot;noch unbenotet&quot; an (auch den Studierenden).</dd><dt>Nicht abgenommen</dt><dd>Wird in den Listen eingeklammert angezeigt, Punkte werden nicht gezählt, bei Studierendem steht &quot;0, nicht abgenommen&quot;</dd><dt>Abgenommen (ok)</dt><dd>Aufgabe wurde abschließend bewertet, Punkte werden regulär gezählt (sofern kein Plagiat; ggf. wird dem Studierenden &quot;Plagiat&quot; angezeigt)</dd><dt>Abnahme nicht bestanden</dt><dd>Aufgabe wurde abschließend bewertet, aber es werden keine Punkte gezählt (dem Studierenden wird &quot;0, Abnahme nicht bestanden&quot; angezeigt, überschreibt die Plagiat Option).</dd></dl></div>");
			out.println("<input type=submit id=submit value=Speichern> <a href=\"#\" onclick=\"$('#statehelp').toggle(); return false;\">(?)</a>");
			if (!requestAdapter.isPrivacyMode() && submission.getPoints() != null) {
				out.println("<input type=hidden name=sid value=\"" + submission.getSubmissionid() + "\">");
				String groupAdding = "";
				if (request.getParameter("groupid") != null && Util.parseInteger(request.getParameter("groupid"), 0) > 0) {
					groupAdding = "&amp;groupid=" + Util.parseInteger(request.getParameter("groupid"), 0);
				}
				out.println("- <a href=\"" + response.encodeURL("GotoNextUngradedSubmission?sid=" + submission.getSubmissionid() + "&amp;taskid=" + task.getTaskid() + groupAdding) + "\">nächste</a>");
			}
			out.println("</form>");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table>");

			out.println("<p>");
		}

		if (submission.getSimilarSubmissions().size() > 0) {
			out.println("<h2>Ähnliche Abgaben: <a href=\"#\" onclick=\"$('#similarSubmissions').toggle(); return false;\">(+/-)</a></h2>");
			if (requestAdapter.isPrivacyMode()) {
				out.println("<table id=similarSubmissions style=\"display:none;\">");
			} else {
				out.println("<table id=similarSubmissions>");
			}
			out.println("<tr>");
			for (SimilarityTest similarityTest : task.getSimularityTests()) {
				out.println("<th><span title=\"Ähnlichkeit zu\">" + similarityTest + "</span></th>");
			}
			out.println("</tr>");
			out.println("<tr>");
			for (SimilarityTest similarityTest : task.getSimularityTests()) {
				out.println("<td>");
				out.println("<table class=border>");
				out.println("<tr>");
				out.println("<th>Abgabe von</th>");
				out.println("<th>Ähnlichkeit</th>");
				out.println("</tr>");
				for (Similarity similarity : DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(similarityTest, submission)) {
					out.println("<tr>");
					out.println("<td><a href=\"" + response.encodeURL("ShowSubmission?sid=" + similarity.getSubmissionTwo().getSubmissionid()) + "\">" + Util.escapeHTML(similarity.getSubmissionTwo().getSubmitterNames()) + "</a></td>");
					out.println("<td class=points>" + similarity.getPercentage() + "%</td>");
					out.println("</tr>");
				}
				out.println("</table>");
				out.println("</td>");
			}
			out.println("</tr>");
			out.println("</table><p>");
		}

		if (submission.getTestResults().size() > 0) {
			out.println("<h2>Tests: <a href=\"#\" onclick=\"$('#tests').toggle(); return false;\">(+/-)</a></h2>");
			out.println("<ul id=tests>");
			for (TestResult testResult : submission.getTestResults()) {
				out.println("<li>" + testResult.getTest().getTestTitle() + "<br>");
				out.println("Erfolgreich: " + Util.boolToHTML(testResult.getPassedTest()));
				if (!testResult.getTestOutput().isEmpty()) {
					if (testResult.getTest() instanceof JavaAdvancedIOTest) {
						out.println("<br>");
						ShowJavaAdvancedIOTestResult.printTestResults(out, (JavaAdvancedIOTest) testResult.getTest(), testResult.getTestOutput(), false, javaScript);
					} else {
						out.println("<br><textarea id=\"testresult" + testResult.getId() + "\" cols=80 rows=15>" + Util.escapeHTML(testResult.getTestOutput()) + "</textarea>");
						javaScript.append("testResultSetup('" + testResult.getId() + "');");
					}
				}
				out.println("</li>");
			}
			out.println("</ul>");
		}

		if (task.isADynamicTask()) {
			out.println("<h2>Dynamische Aufgabe: <a href=\"#\" onclick=\"$('#dynamictask').toggle(); return false;\">(+/-)</a></h2>");
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
			out.println("<li>Ist korrekt: " + Util.boolToHTML(dynamicTask.isCorrect(submission)) + "</li>");
			out.println("</ul>");
		}

		if (submittedFiles.size() > 0) {
			out.println("<h2>Dateien: <a href=\"#\" onclick=\"$('#files').toggle(); return false;\">(+/-)</a></h2>");
			out.println("<div id=files class=mid>");
			out.println("<p><a href=\"" + response.encodeURL("DownloadAsZip?sid=" + submission.getSubmissionid()) + "\">alles als .zip herunterladen</a></p>");
			Pattern pattern = null;
			if (!"".equals(task.getFeaturedFiles().trim())) {
				if (task.getFeaturedFiles().startsWith("^")) {
					pattern = Pattern.compile("^(" + task.getFeaturedFiles().substring(1) + ")$");
				} else {
					pattern = Pattern.compile("^(([/a-zA-Z0-9_ .-]*?/)?(" + task.getFeaturedFiles() + "))$");
				}
			}
			int id = 0;
			for (String file : submittedFiles) {
				file = file.replace(System.getProperty("file.separator"), "/");
				if (ShowFile.isInlineAble(file.toLowerCase())) {
					out.println("<h3 class=files>" + Util.escapeHTML(file) + " <a id=\"showbtn" + id + "\" style=\"display: none;\" href=\"#\" onclick='$(\"#codepreview" + id + "\").show();$(\"#showbtn" + id + "\").hide();return false;'>(show)</a></h3>");
					out.println("<div id=\"codepreview" + id + "\" class=\"mid\">");
					out.println("<div class=\"inlinemenu\">");
					out.println("<a id=\"hidebtn" + id + "\" href=\"#\" onclick='hideCodePreview(\"" + id + "\");return false;'>(hide)</a>");
					out.println("</div>");
					out.println("<div id=\"resizablecodepreview" + id + "\" class=\"mid inlinefile\">");
					out.println("<iframe name=\"iframe" + id + "\" id=\"iframe" + id + "\" scrolling=\"yes\" width=\"100%\" height=\"100%\" src=\"" + response.encodeURL("ShowFile/" + file + "?sid=" + submission.getSubmissionid()) + "\"></iframe></div>");
					out.println("</div>");
					javaScript.append("iframeSetup('" + id + "');");
					if (pattern != null) {
						Matcher m = pattern.matcher(file);
						if (!m.matches()) {
							javaScript.append("hideCodePreview('" + id + "');");
						}
					}
				} else {
					out.println("<h3 class=files>" + Util.escapeHTML(file) + "</h3>");
				}
				out.println("<a href=\"" + response.encodeURL("ShowFile/" + file + "?download=true&amp;sid=" + submission.getSubmissionid()) + "\">Download " + Util.escapeHTML(file) + "</a><p>");
				id++;
			}
			out.println("</div>");
		}
		out.println("<script type=\"text/javascript\">" + javaScript.toString() + "</script>");
		template.printTemplateFooter();
	}
}
