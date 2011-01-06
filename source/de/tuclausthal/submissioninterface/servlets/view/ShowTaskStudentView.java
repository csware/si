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
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.PointGivenDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestCountDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointGiven;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a task in student view
 * @author Sven Strickroth
 */
public class ShowTaskStudentView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Session session = RequestAdapter.getSession(request);

		Task task = (Task) request.getAttribute("task");
		Submission submission = (Submission) request.getAttribute("submission");
		List<String> submittedFiles = (List<String>) request.getAttribute("submittedFiles");
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

		if (submission != null) {
			out.println("<p><h2>Informationen zu meiner Abgabe:</h2>");
			out.println("<table class=border>");
			if (submission.getSubmitters().size() > 1) {
				out.println("<tr>");
				out.println("<th>Bearbeitet von:</th>");
				out.println("<td>");
				out.println(submission.getSubmitterNames().replaceAll("; ", "<br>"));
				out.println("</td>");
				out.println("</tr>");
			}
			if (submission.getLastModified() != null) {
				out.println("<tr>");
				out.println("<th>Letzte Änderung:</th>");
				out.println("<td>");
				out.println(Util.mknohtml(submission.getLastModified().toLocaleString()));
				out.println("</td>");
				out.println("</tr>");
			}
			if (submittedFiles.size() > 0) {
				out.println("<tr>");
				out.println("<th>Besteht aus:</th>");
				out.println("<td>");
				for (String file : submittedFiles) {
					file = file.replace(System.getProperty("file.separator"), "/");
					out.println("<a target=\"_blank\" href=\"" + response.encodeURL("ShowFile/" + file + "?sid=" + submission.getSubmissionid()) + "\">" + Util.mknohtml(file) + "</a>");
					if (task.getDeadline().after(Util.correctTimezone(new Date()))) {
						out.println(" (<a onclick=\"return confirmLink('Wirklich löschen?')\" href=\"" + response.encodeURL("DeleteFile/" + file + "?sid=" + submission.getSubmissionid()) + "\">löschen</a>)");
					}
					out.println("<br>");
				}
				out.println("</td>");
				out.println("</tr>");
			}
			if (task.getShowPoints().before(Util.correctTimezone(new Date())) && submission.getPoints() != null) {
				out.println("<tr>");
				out.println("<th>Bewertung:</th>");
				out.println("<td>");
				if (submission.getPoints().getPointStatus() == PointStatus.ABGENOMMEN.ordinal()) {
					if (task.getPointCategories().size() > 0) {
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
							out.println("<li>" + Util.showPoints(issuedPoints) + "/" + Util.showPoints(category.getPoints()) + " " + Util.mknohtml(category.getDescription()) + "</li>");
						}
						out.println("</ul>");
					} else {
						out.println(Util.showPoints(submission.getPoints().getPointsByStatus()) + " von " + Util.showPoints(task.getMaxPoints()) + " Punkt(e)");
					}
				} else if (submission.getPoints().getPointStatus() == PointStatus.ABGENOMMEN_FAILED.ordinal()) {
					out.println("0 von " + Util.showPoints(task.getMaxPoints()) + ", Abhahme nicht bestanden");
				} else {
					out.println("0 von " + Util.showPoints(task.getMaxPoints()) + ", nicht abgenommen");
				}
				out.println("<p>Vergeben von: <a href=\"mailto:" + Util.mknohtml(submission.getPoints().getIssuedBy().getUser().getFullEmail()) + "\">" + Util.mknohtml(submission.getPoints().getIssuedBy().getUser().getFullName()) + "</a></p>");
				out.println("</td>");
				out.println("</tr>");
				if (submission.getPoints().getPublicComment() != null && !"".equals(submission.getPoints().getPublicComment())) {
					out.println("<tr>");
					out.println("<th>Kommentar:</th>");
					out.println("<td>");
					out.println(Util.mkTextToHTML(submission.getPoints().getPublicComment()));
					out.println("</td>");
					out.println("</tr>");
				}
			}
			out.println("</table>");

			out.println("<p>");
			if ("-".equals(task.getFilenameRegexp()) && task.isShowTextArea() == false) {
				out.println("<div class=mid>Keine Abgabe möglich.</div>");
			} else if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
				out.println("<div class=mid>Keine Abgabe mehr möglich.</div>");
			} else {
				if (submittedFiles.size() > 0) {
					out.println("<div class=mid><a href=\"" + response.encodeURL("SubmitSolution?taskid=" + task.getTaskid()) + "\">Abgabe erweitern</a></div");
				} else {
					out.println("<div class=mid><a href=\"" + response.encodeURL("SubmitSolution?taskid=" + task.getTaskid()) + "\">Abgabe starten</a></div");
				}
			}

			List<Test> tests = DAOFactory.TestDAOIf(session).getStudentTests(task);
			TestCountDAOIf testCountDAO = DAOFactory.TestCountDAOIf(session);
			if (submittedFiles.size() > 0 && tests.size() > 0 && task.getDeadline().after(Util.correctTimezone(new Date()))) {
				out.println("<p><h2>Mögliche Tests:</h2>");
				out.println("<table class=border>");
				for (Test test : tests) {
					out.println("<tr>");
					out.println("<th>" + Util.mknohtml(test.getTestTitle()) + "</th>");
					out.println("<td>");
					out.println(Util.mkTextToHTML(test.getTestDescription()));
					out.println("</td>");
					out.println("<td>");
					if (testCountDAO.canStillRunXTimes(test, submission) > 0) {
						out.println("<a href=\"" + response.encodeURL("PerformTest?sid=" + submission.getSubmissionid() + "&amp;testid=" + test.getId()) + "\">Test ausführen</a>");
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
			if ("-".equals(task.getFilenameRegexp()) && task.isShowTextArea() == false) {
				out.println("<div class=mid>Keine Abgabe möglich.</div>");
			} else if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
				out.println("<div class=mid>Keine Abgabe mehr möglich.</div>");
			} else {
				out.println("<div class=mid><a href=\"" + response.encodeURL("SubmitSolution?taskid=" + task.getTaskid()) + "\">Abgabe starten</a></div");
			}
		}
		template.printTemplateFooter();
	}
}
