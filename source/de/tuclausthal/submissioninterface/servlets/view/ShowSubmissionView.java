/*
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.servlets.controller.ShowFile;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a submission to a tutor
 * @author Sven Strickroth
 */
public class ShowSubmissionView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		template.addHead("<script type=\"text/javascript\">function hideCodePreview(id) { $(\"#codepreview\" + id).hide();$(\"#showbtn\" + id).show(); } function testResultSetup(id) { $(\"#testresult\" + id).resizable({ handles: 'se' }); } function iframeSetup(id) { $(\"#resizablecodepreview\" + id).resizable({ helper: 'ui-resizable-helper', minWidth: 800, minHeight: 100, handles: 'se' }); }</script>");

		PrintWriter out = response.getWriter();

		Session session = HibernateSessionHelper.getSessionFactory().openSession();

		Submission submission = (Submission) request.getAttribute("submission");
		List<String> submittedFiles = (List<String>) request.getAttribute("submittedFiles");
		Task task = submission.getTask();

		template.printTemplateHeader(submission);
		StringBuffer javaScript = new StringBuffer();

		if (submission.getLastModified() != null) {
			out.println("<p>Letzte Änderung: " + Util.mknohtml(submission.getLastModified().toLocaleString()) + "</p>");
		}

		for (Participation participation : submission.getSubmitters()) {
			out.println("<a href=\"" + response.encodeURL("ShowUser?uid=" + participation.getUser().getUid()) + "\">" + Util.mknohtml(participation.getUser().getFullName()) + "</a><br>");
		}

		if (submission.getSubmitters().iterator().next().getGroup() != null) {
			out.println("<h2>Gruppe: " + submission.getSubmitters().iterator().next().getGroup().getName() + "</h2>");
		}

		if (task.getDeadline().before(Util.correctTimezone(new Date())) || (task.isShowTextArea() == false && "-".equals(task.getFilenameRegexp()))) {
			out.println("<h2>Bewertung: <a href=\"#\" onclick=\"$('#mark').toggle(); return false;\">(+/-)</a></h2>");
			out.println("<table id=mark class=border>");
			String oldPublicComment = "";
			String oldInternalComment = "";
			int points = 0;
			boolean pointsOk = false;
			String pointsGivenBy = "";
			if (submission.getPoints() != null) {
				oldPublicComment = submission.getPoints().getPublicComment();
				oldInternalComment = submission.getPoints().getInternalComment();
				points = submission.getPoints().getPoints();
				pointsOk = submission.getPoints().getPointsOk();
				pointsGivenBy = " (bisher " + Util.showPoints(points) + " Punkte  vergeben von: <a href=\"mailto:" + Util.mknohtml(submission.getPoints().getIssuedBy().getUser().getFullEmail()) + "\">" + Util.mknohtml(submission.getPoints().getIssuedBy().getUser().getFullName()) + "</a>, <a href=\"" + response.encodeURL("ShowMarkHistory?sid=" + submission.getSubmissionid()) + "\">History</a>)";
			}
			out.println("<tr>");
			out.println("<td>");
			out.println("<form action=\"?\" method=post>");
			out.println("<input type=hidden name=sid value=\"" + submission.getSubmissionid() + "\">");
			out.println("<b>Punkte:</b> <input type=text name=points size=3 value=\"" + Util.showPoints(points) + "\"> (max. " + Util.showPoints(task.getMaxPoints()) + ")" + pointsGivenBy + "<br>");
			out.println("<b>Öffentlicher Kommentar:</b><br><textarea cols=80 rows=8 name=publiccomment>" + Util.mknohtml(oldPublicComment) + "</textarea><br>");
			out.println("<b>Interner Kommentar:</b><br><textarea cols=80 rows=8 name=internalcomment>" + Util.mknohtml(oldInternalComment) + "</textarea><br>");
			out.println("<b>Abgenommen:</b> <input type=checkbox name=pointsok " + (pointsOk ? "checked" : "") + "><br>");
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
					out.println("<td><a href=\"" + response.encodeURL("ShowSubmission?sid=" + similarity.getSubmissionTwo().getSubmissionid()) + "\">" + Util.mknohtml(similarity.getSubmissionTwo().getSubmitterNames()) + "</a></td>");
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
					out.println("<br><textarea id=\"testresult" + testResult.getId() + "\" cols=80 rows=15>" + Util.mknohtml(testResult.getTestOutput()) + "</textarea>");
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
			List<String> featuredFiles = Arrays.asList(task.getFeaturedFiles().split(","));
			int id = 0;
			for (String file : submittedFiles) {
				file = file.replace(System.getProperty("file.separator"), "/");
				if (ShowFile.isInlineAble(file.toLowerCase())) {
					out.println("<h3 class=files>" + Util.mknohtml(file) + " <a id=\"showbtn" + id + "\" style=\"display: none;\" href=\"#\" onclick='$(\"#codepreview" + id + "\").show();$(\"#showbtn" + id + "\").hide();return false;'>(show)</a></h3>");
					out.println("<div id=\"codepreview" + id + "\" class=\"mid\">");
					out.println("<div class=\"inlinemenu\"><a href=\"#\" onclick='this.href=document.getElementById(\"iframe" + id + "\").contentWindow.location' target=\"_blank\">(new window)</a>");
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
					out.println("<h3 class=files>" + Util.mknohtml(file) + "</h3>");
				}
				out.println("<a href=\"" + response.encodeURL("ShowFile/" + file + "?download=true&amp;sid=" + submission.getSubmissionid()) + "\">Download " + Util.mknohtml(file) + "</a><p>");
				id++;
			}
			out.println("</div>");
		}
		out.println("<script type=\"text/javascript\">" + javaScript.toString() + "</script>");
		template.printTemplateFooter();
	}
}
