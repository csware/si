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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.CompileTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.RegExpTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for adding/editing a task
 * @author Sven Strickroth
 */
public class TaskManagerView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Task task = (Task) request.getAttribute("task");
		Lecture lecture = task.getLecture();

		template.addHead("<script type=\"text/javascript\" src=\"" + getServletContext().getContextPath() + "/tiny_mce/tiny_mce.js\"></script>");
		template.addHead("<script type=\"text/javascript\">\ntinyMCE.init({" +
							"mode : \"textareas\"," +
							"theme : \"advanced\"," +
							"plugins : \"safari,style,table,advimage,iespell,contextmenu,paste,nonbreaking\"," +
							"theme_advanced_buttons1 : \"newdocument,|,undo,redo,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,formatselect,fontsizeselect\"," +
							"theme_advanced_buttons2 : \"paste,pastetext,pasteword,|,bullist,numlist,|,outdent,indent,blockquote,|,link,unlink,anchor,image,cleanup,forecolor,backcolor\"," +
							"theme_advanced_buttons3 : \"tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,iespell,advhr,|,nonbreaking,blockquote,code\"," +
							"theme_advanced_toolbar_location : \"top\"," +
							"theme_advanced_toolbar_align : \"left\"," +
							"theme_advanced_statusbar_location : \"bottom\"," +
							"theme_advanced_resizing : true," +
							"content_css : \"/submissionsystem/si.css\"" +
							"});\n</script>");

		if (task.getTaskid() != 0) {
			template.printTemplateHeader("Aufgabe bearbeiten", task);
		} else {
			template.printTemplateHeader("neue Aufgabe", lecture);
		}

		out.println("<form action=\"" + response.encodeURL("?") + "\" method=post>");
		if (task.getTaskid() != 0) {
			out.println("<input type=hidden name=action value=saveTask>");
			out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		} else {
			out.println("<input type=hidden name=action value=saveNewTask>");
		}
		out.println("<input type=hidden name=lecture value=\"" + lecture.getId() + "\">");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text name=title value=\"" + Util.mknohtml(task.getTitle()) + "\"></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		out.println("<td><textarea cols=60 rows=10 name=description>" + Util.mknohtml(task.getDescription()) + "</textarea></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Filename Regexp:</th>");
		out.println("<td><input type=text name=filenameregexp value=\"" + Util.mknohtml(task.getFilenameRegexp()) + "\"> <b>Für Java-Dateien: &quot;[A-Z][A-Za-z0-9_]+\\.java&quot;, &quot;-&quot; = upload disabled</b></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Text-Eingabefeld:</th>");
		out.println("<td><input type=checkbox name=showtextarea " + (task.isShowTextArea() ? "checked" : "") + "></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Dateien bei Tutor aufklappen:</th>");
		out.println("<td><input type=text name=featuredfiles value=\"" + Util.mknohtml(task.getFeaturedFiles()) + "\"> Sollen alle Dateien zugeklappt sein: \"-\", sonst Komma-separierte Datei-Liste oder leer.</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Startdatum:</th>");
		out.println("<td><input type=text name=startdate value=\"" + Util.mknohtml(task.getStart().toLocaleString()) + "\"> (dd.MM.yyyy oder dd.MM.yyyy HH:mm:ss)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Enddatum:</th>");
		out.println("<td><input type=text name=deadline value=\"" + Util.mknohtml(task.getDeadline().toLocaleString()) + "\"> (dd.MM.yyyy oder dd.MM.yyyy HH:mm:ss)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Punktedatum:</th>");
		out.println("<td><input type=text name=pointsdate value=\"" + Util.mknohtml(task.getShowPoints().toLocaleString()) + "\"> (dd.MM.yyyy oder dd.MM.yyyy HH:mm:ss)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Punkte:</th>");
		out.println("<td><input type=text name=maxpoints value=\"" + Util.showPoints(task.getMaxPoints()) + "\"> <b>bei Änderung bereits vergebene Pkts. prüfen!</b></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		if (task.getTaskid() != 0) {
			out.println(response.encodeURL("ShowTask?taskid=" + task.getTaskid()));
		} else {
			out.println(response.encodeURL("ShowLecture?lecture=" + lecture.getId()));
		}
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		// don't show for new tasks
		if (task.getTaskid() != 0 && (task.isShowTextArea() == true || !"-".equals(task.getFilenameRegexp()))) {
			out.println("<h2>Ähnlichkeitsprüfungen</h2>");
			out.println("<ul>");
			for (SimilarityTest similarityTest : task.getSimularityTests()) {
				out.print(similarityTest + "<br>");
				out.println("Ignored Files: " + Util.mknohtml(similarityTest.getExcludeFiles()) + "<br>");
				out.print("Status: ");
				if (similarityTest.isNeedsToRun()) {
					out.println("in Queue, noch nicht ausgeführt<br>");
				} else {
					out.println("in Ausführung bzw. bereits ausgeführt - <a onclick=\"return confirmLink('Wirklich erneut ausführen?')\" href=\"" + response.encodeURL("DupeCheck?action=rerunSimilarityTest&amp;similaritytestid=" + similarityTest.getSimilarityTestId()) + "&amp;taskid=" + task.getTaskid() + "\">erneut ausführen</a><br>");
				}
				out.println("<li><a onclick=\"return confirmLink('Wirklich löschen?')\" href=\"" + response.encodeURL("DupeCheck?action=deleteSimilarityTest&amp;taskid=" + task.getTaskid() + "&amp;similaritytestid=" + similarityTest.getSimilarityTestId()) + "\">löschen</a><br>");
			}
			out.println("</ul>");
			out.println("<p class=mid><a href=\"" + response.encodeURL("DupeCheck?taskid=" + task.getTaskid()) + "\">Ähnlichkeitsprüfung hinzufügen</a><p>");
			out.println("<h2>Funktionstests der Abgaben</h2>");
			out.println("<p class=mid><a href=\"" + response.encodeURL("TestManager?action=newTest&amp;taskid=" + task.getTaskid()) + "\">Test hinzufügen</a></p>");
			out.println("<ul>");
			for (Test test : task.getTests()) {
				out.println("<li>&quot;" + Util.mknohtml(test.getTestTitle()) + "&quot;: ");
				if (test instanceof RegExpTest) {
					RegExpTest regexptest = (RegExpTest) test;
					out.println("RegExp-Test:<br>Prüfpattern: " + Util.mknohtml(regexptest.getRegularExpression()) + "<br>Parameter: " + Util.mknohtml(regexptest.getCommandLineParameter()) + "<br>Main-Klasse: " + Util.mknohtml(regexptest.getMainClass()) + "<br>");
				} else if (test instanceof CompileTest) {
					out.println("Compile-Test<br>");
				} else if (test instanceof JUnitTest) {
					out.println("JUnit-Test<br>");
				} else {
					out.println("unknown<br>");
				}
				out.println("# Ausführbar für Studenten: " + test.getTimesRunnableByStudents() + "<br>");
				out.println("Tutortest: " + test.isForTutors() + "<br>");
				if (test.isForTutors()) {
					out.print("Status: ");
					if (test.isNeedsToRun()) {
						out.println("in Queue, noch nicht ausgeführt<br>");
					} else {
						out.println("in Ausführung bzw. bereits ausgeführt - <a onclick=\"return confirmLink('Wirklich erneut ausführen?')\" href=\"" + response.encodeURL("TestManager?action=rerunTest&amp;testid=" + test.getId()) + "&amp;taskid=" + task.getTaskid() + "\">erneut ausführen</a><br>");
					}
				}
				out.println("<a onclick=\"return confirmLink('Wirklich löschen?')\" href=\"" + response.encodeURL("TestManager?action=deleteTest&amp;testid=" + test.getId()) + "&amp;taskid=" + task.getTaskid() + "\">Test löschen</a>");
				out.println("</li>");
			}
			out.println("</ul>");
		}
		template.printTemplateFooter();
	}
}
