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

		template.addHead("<script type=\"text/javascript\">function iframeSetup(id) { $(\"#codepreview\" + id).resizable({ helper: 'ui-resizable-helper', minWidth: 800, minHeight: 100, handles: 'se' }); $(\"#codepreview\" + id).mouseenter(function(){ $(\"#menu\" + id).show(); }); $(\"#codepreview\" + id).mouseleave(function(){ $(\"#menu\" + id).hide(); }); }</script>");

		PrintWriter out = response.getWriter();

		Session session = HibernateSessionHelper.getSessionFactory().openSession();

		Submission submission = (Submission) request.getAttribute("submission");
		List<String> submittedFiles = (List<String>) request.getAttribute("submittedFiles");
		Task task = submission.getTask();

		template.printTemplateHeader(submission);

		for (Participation participation : submission.getSubmitters()) {
			out.println("<a href=\"ShowUser?uid=" + participation.getUser().getUid() + "\">" + Util.mknohtml(participation.getUser().getFullName()) + "</a><br>");
		}

		if (submission.getSubmitters().iterator().next().getGroup() != null) {
			out.println("<h2>Gruppe: " + submission.getSubmitters().iterator().next().getGroup().getName() + "</h2>");
		}

		if (task.getDeadline().before(Util.correctTimezone(new Date())) || (task.isShowTextArea() == false && "-".equals(task.getFilenameRegexp()))) {
			out.println("<h2>Bewertung:</h2>");
			out.println("<table class=border>");
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
				pointsGivenBy = " (bisher " + Util.showPoints(points) + " Punkte  vergeben von: <a href=\"mailto:" + Util.mknohtml(submission.getPoints().getIssuedBy().getUser().getFullEmail()) + "\">" + Util.mknohtml(submission.getPoints().getIssuedBy().getUser().getFullName()) + "</a>, <a href=\"ShowMarkHistory?sid=" + submission.getSubmissionid() + "\">History</a>)";
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
			out.println("<h2>Ähnliche Abgaben:</h2>");
			out.println("<table>");
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
			out.println("<h2>Tests:</h2>");
			out.println("<ul>");
			for (TestResult testResult : submission.getTestResults()) {
				out.println("<li>" + testResult.getTest().getTestTitle() + "<br>");
				out.println("Erfolgreich: " + Util.boolToHTML(testResult.getPassedTest()));
				if (!testResult.getTestOutput().isEmpty()) {
					out.println("<br><textarea cols=80 rows=15>" + Util.mknohtml(testResult.getTestOutput()) + "</textarea>");
				}
				out.println("</li>");
			}
			out.println("</ul>");
		}

		if (submittedFiles.size() > 0) {
			out.println("<h2>Dateien:</h2>");
			out.println("<div class=mid>");
			out.println("<p><a href=\"DownloadAsZip?sid=" + submission.getSubmissionid() + "\">alles als .zip herunterladen</a></p>");
			int id = 0;
			StringBuffer javaScript = new StringBuffer();
			for (String file : submittedFiles) {
				file = file.replace(System.getProperty("file.separator"), "/");
				if (ShowFile.isInlineAble(file.toLowerCase())) {
					out.println("<h3 class=files>" + Util.mknohtml(file) + " <a id=\"showbtn" + id + "\" style=\"display: none;\" href=\"#\" onclick='$(\"#codepreview" + id + "\").show();$(\"#showbtn" + id + "\").hide();return false;'>(show)</a></h3>");
					out.println("<div id=\"codepreview" + id + "\" class=\"mid inlinefile\">");
					out.println("<div id=\"menu" + id + "\" class=\"inlinemenu\"><a href=\"#\" onclick='this.href=document.getElementById(\"iframe" + id + "\").contentWindow.location' target=\"_blank\">(new window)</a>");
					if (file.toLowerCase().endsWith(".java")) {
						out.println(" <a href=\"" + response.encodeURL("ShowFile/" + file + "?sid=" + submission.getSubmissionid()) + "\" onclick='toggleComments(this)' target=\"iframe" + id + "\">(toggle comments)</a>");
					}
					out.println(" <a id=\"hidebtn" + id + "\" href=\"#\" onclick='$(\"#codepreview" + id + "\").hide();$(\"#showbtn" + id + "\").show();return false;'>(hide)</a>");
					out.println("</div>");
					out.println("<iframe name=\"iframe" + id + "\" id=\"iframe" + id + "\" scrolling=\"yes\" width=\"100%\" height=\"100%\" src=\"" + response.encodeURL("ShowFile/" + file + "?sid=" + submission.getSubmissionid()) + "\"></iframe></div>");
					javaScript.append("iframeSetup('" + id + "');");
				} else {
					out.println("<h3 class=files>" + Util.mknohtml(file) + "</h3>");
				}
				out.println("<a href=\"" + response.encodeURL("ShowFile/" + file + "?download=true&amp;sid=" + submission.getSubmissionid()) + "\">Download " + Util.mknohtml(file) + "</a><p>");
				id++;
			}
			out.println("</div>");
			out.println("<script type=\"text/javascript\">" + javaScript.toString() + "</script>");
		}
		template.printTemplateFooter();
	}
}
