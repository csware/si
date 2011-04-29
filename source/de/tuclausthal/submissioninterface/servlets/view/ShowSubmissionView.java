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
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.PointGivenDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointGiven;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.ShowFile;
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
		template.addHead("<script type=\"text/javascript\">function hideCodePreview(id) { $(\"#codepreview\" + id).hide();$(\"#showbtn\" + id).show(); } function testResultSetup(id) { $(\"#testresult\" + id).resizable({ handles: 'se' }); } function iframeSetup(id) { $(\"#resizablecodepreview\" + id).resizable({ helper: 'ui-resizable-helper', minWidth: 800, minHeight: 100, handles: 'se' }); }</script>");

		PrintWriter out = response.getWriter();

		Session session = RequestAdapter.getSession(request);

		Submission submission = (Submission) request.getAttribute("submission");
		List<String> submittedFiles = (List<String>) request.getAttribute("submittedFiles");
		Task task = submission.getTask();

		template.printTemplateHeader(submission);
		StringBuffer javaScript = new StringBuffer();

		if (submission.getLastModified() != null) {
			out.println("<p>Letzte Änderung: " + Util.escapeHTML(submission.getLastModified().toLocaleString()) + "</p>");
		}

		for (Participation participation : submission.getSubmitters()) {
			out.println("<a href=\"" + response.encodeURL("ShowUser?uid=" + participation.getUser().getUid()) + "\">" + Util.escapeHTML(participation.getUser().getFullName()) + "</a><br>");
		}

		if (submission.getSubmitters().iterator().next().getGroup() != null) {
			out.println("<h2>Gruppe: " + submission.getSubmitters().iterator().next().getGroup().getName() + "</h2>");
			if (task.getMaxSubmitters() > 1 && submission.getSubmitters().size() < task.getMaxSubmitters()) {
				StringBuffer setWithUser = new StringBuffer();
				setWithUser.append("<form action=\"?\" method=post>");
				setWithUser.append("<input type=hidden name=sid value=\"" + submission.getSubmissionid() + "\">");
				SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
				Participation participation = submission.getSubmitters().iterator().next();
				setWithUser.append("<p>Fehlt ein Partner: <select name=partnerid size=1>");
				setWithUser.append("<option value='0'></option>");
				int cnt = 0;
				for (Participation part : participation.getGroup().getMembers()) {
					if (part.getId() != participation.getId() && submissionDAO.getSubmission(task, part.getUser()) == null) {
						cnt++;
						setWithUser.append("<option value=" + part.getId() + ">" + Util.escapeHTML(part.getUser().getFullName()) + "</option>");
					}
				}
				setWithUser.append("</select> <input type=submit value= \"Hinzufügen\"></p></form>");
				if (cnt > 0) {
					out.println(setWithUser.toString());
				}
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
			boolean isDupe = false;
			String pointsGivenBy = "";
			if (submission.getPoints() != null) {
				oldPublicComment = submission.getPoints().getPublicComment();
				oldInternalComment = submission.getPoints().getInternalComment();
				points = submission.getPoints().getPoints();
				if (submission.getPoints().getIsDupe() != null) {
					isDupe = submission.getPoints().getIsDupe();
				}
				pointsOk = submission.getPoints().getPointStatus() == PointStatus.ABGENOMMEN.ordinal();
				pointsFailed = submission.getPoints().getPointStatus() == PointStatus.ABGENOMMEN_FAILED.ordinal();
				pointsGivenBy = " (bisher " + Util.showPoints(points) + " Punkte  vergeben von: <a href=\"mailto:" + Util.escapeHTML(submission.getPoints().getIssuedBy().getUser().getFullEmail()) + "\">" + Util.escapeHTML(submission.getPoints().getIssuedBy().getUser().getFullName()) + "</a>, <a href=\"" + response.encodeURL("ShowMarkHistory?sid=" + submission.getSubmissionid()) + "\">History</a>)";
			}
			out.println("<tr>");
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
						out.println("<li><input type=checkbox id=\"point_" + category.getPointcatid() + "\" name=\"point_" + category.getPointcatid() + "\" value=\"" + category.getPoints() + "\" " + checked + "> <label for=\"point_" + category.getPointcatid() + "\">" + Util.escapeHTML(category.getDescription()) + " (" + Util.showPoints(category.getPoints()) + ")</label></li>");
					} else {
						out.println("<li><input type=text size=3 id=\"point_" + category.getPointcatid() + "\" name=\"point_" + category.getPointcatid() + "\" value=\"" + Util.showPoints(curPoints) + "\"> <label for=\"point_" + category.getPointcatid() + "\">" + Util.escapeHTML(category.getDescription()) + " (max. " + Util.showPoints(category.getPoints()) + ")</label></li>");
					}
				}
				out.println("</ul>");
			} else {
				out.println("<b>Punkte:</b> <input type=text name=points size=3 value=\"" + Util.showPoints(points) + "\"> (max. " + Util.showPoints(task.getMaxPoints()) + ")" + pointsGivenBy + "<br>");
			}
			out.println("<b>Öffentlicher Kommentar:</b><br><textarea cols=80 rows=8 name=publiccomment>" + Util.escapeHTML(oldPublicComment) + "</textarea><br>");
			out.println("<b>Interner Kommentar:</b><br><textarea cols=80 rows=8 name=internalcomment>" + Util.escapeHTML(oldInternalComment) + "</textarea><br>");
			out.println("<b>Best&auml;tigtes Plagiat:</b> <input type=checkbox name=isdupe " + (isDupe ? "checked" : "") + "><br>");
			out.println("<b><label for=\"nabgen\">Nicht abgenommen:</label></b> <input id=\"nabgen\" type=radio name=pointsstatus value=\"nabgen\"" + (!(pointsOk || pointsFailed) ? "checked" : "") + ">, <b><label for=\"abgen\">Abgenommen (ok):</label></b> <input id=\"abgen\" type=radio name=pointsstatus value=\"ok\"" + (pointsOk ? "checked" : "") + ">, <b><label for=\"failed\">Abnahme nicht bestanden:</b> <input id=\"failed\" type=radio name=pointsstatus value=\"failed\" " + (pointsFailed ? "checked" : "") + "><br>");
			out.println("<input type=submit value=Speichern>");
			out.println("</form>");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table>");

			out.println("<p>");
		}

		if (submission.getSimilarSubmissions().size() > 0) {
			out.println("<h2>Ähnliche Abgaben: <a href=\"#\" onclick=\"$('#similarSubmissions').toggle(); return false;\">(+/-)</a></h2>");
			out.println("<table id=similarSubmissions>");
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
				out.println("<th>Student</th>");
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
					out.println("<br><textarea id=\"testresult" + testResult.getId() + "\" cols=80 rows=15>" + Util.escapeHTML(testResult.getTestOutput()) + "</textarea>");
					javaScript.append("testResultSetup('" + testResult.getId() + "');");
				}
				out.println("</li>");
			}
			out.println("</ul>");
		}

		if (submittedFiles.size() > 0) {
			out.println("<h2>Dateien: <a href=\"#\" onclick=\"$('#files').toggle(); return false;\">(+/-)</a></h2>");
			out.println("<div id=files class=mid>");
			out.println("<p><a href=\"" + response.encodeURL("DownloadAsZip?sid=" + submission.getSubmissionid()) + "\">alles als .zip herunterladen</a></p>");
			List<String> featuredFiles = new LinkedList<String>();
			if (!"".equals(task.getFeaturedFiles().trim())) {
				featuredFiles = Arrays.asList(task.getFeaturedFiles().split(","));
			}
			int id = 0;
			for (String file : submittedFiles) {
				file = file.replace(System.getProperty("file.separator"), "/");
				if (ShowFile.isInlineAble(file.toLowerCase())) {
					out.println("<h3 class=files>" + Util.escapeHTML(file) + " <a id=\"showbtn" + id + "\" style=\"display: none;\" href=\"#\" onclick='$(\"#codepreview" + id + "\").show();$(\"#showbtn" + id + "\").hide();return false;'>(show)</a></h3>");
					out.println("<div id=\"codepreview" + id + "\" class=\"mid\">");
					out.println("<div class=\"inlinemenu\"><a href=\"#\" onclick='this.href=$(\"iframe" + id + "\").contentWindow.location' target=\"_blank\">(new window)</a>");
					out.println(" <a id=\"hidebtn" + id + "\" href=\"#\" onclick='hideCodePreview(\"" + id + "\");return false;'>(hide)</a>");
					out.println("</div>");
					out.println("<div id=\"resizablecodepreview" + id + "\" class=\"mid inlinefile\">");
					out.println("<iframe name=\"iframe" + id + "\" id=\"iframe" + id + "\" scrolling=\"yes\" width=\"100%\" height=\"100%\" src=\"" + response.encodeURL("ShowFile/" + file + "?sid=" + submission.getSubmissionid()) + "\"></iframe></div>");
					out.println("</div>");
					javaScript.append("iframeSetup('" + id + "');");
					if (featuredFiles.size() > 0 && !featuredFiles.contains(file)) {
						javaScript.append("hideCodePreview('" + id + "');");
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
