/*
 * Copyright 2009-2013, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommentsMetricTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.CompileTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.ModelSolutionProvisionType;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.RegExpTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a form for adding/editing a task
 * @author Sven Strickroth
 */
public class TaskManagerView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Task task = (Task) request.getAttribute("task");
		Lecture lecture = task.getTaskGroup().getLecture();
		@SuppressWarnings("unchecked")
		List<String> advisorFiles = (List<String>) request.getAttribute("advisorFiles");
		@SuppressWarnings("unchecked")
		List<String> modelSolutionFiles = (List<String>) request.getAttribute("modelSolutionFiles");

		template.addJQuery();
		template.addKeepAlive();
		template.addHead("<script src=\"" + getServletContext().getContextPath() + "/tiny_mce/tiny_mce.js\"></script>");
		template.addHead("<script>\ntinyMCE.init({" +
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
		template.addHead("<script>\n" +
							"function checkRegexp() {" + 
							"$.ajax({" +
									"type: \"POST\"," +
									"url: \"" + Util.generateHTMLLink("?action=regexptest", response) + "\"," +
									"data: { regexp: document.getElementById(\"filenameregexp\").value, test: document.getElementById(\"regexptest\").value }, " +
									"success: function(msg){" +
										"alert(msg);" +
									"}" +
								"});" +
							"}" +
							"\n</script>");
		template.addHead("<script>\n" +
							"function getDynamicTaskHints() {" +
								"if (document.getElementById(\"dynamicTask\").value != \"\") {"+
									"document.getElementById(\"dynamictaskhints\").innerHTML = \"hole Hinweise...\";" +
									"$.ajax({" +
											"type: \"POST\"," +
											"url: \"" + Util.generateHTMLLink("?action=dynamictaskhints", response) + "\"," +
											"data: { dynamicTask: document.getElementById(\"dynamicTask\").value }, " +
											"success: function(msg){" +
												"document.getElementById(\"dynamictaskhints\").innerHTML = msg;" +
											"}" +
										"});" +
								"} else {" +
									"document.getElementById(\"dynamictaskhints\").innerHTML = \"\";" +
								"}" +
							"}" +
							"\n</script>");

		if (task.getTaskid() != 0) {
			template.printTemplateHeader("Aufgabe bearbeiten", task);
		} else {
			template.printTemplateHeader("neue Aufgabe", lecture);
		}

		PrintWriter out = response.getWriter();
		out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
		if (task.getTaskid() != 0) {
			out.println("<input type=hidden name=action value=saveTask>");
			out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
		} else {
			out.println("<input type=hidden name=action value=saveNewTask>");
		}
		out.println("<input type=hidden name=lecture value=\"" + lecture.getId() + "\">");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th width=\"30%\">Aufgabengruppe:</th>");
		out.println("<td><select size=1 name=taskGroup required=required>");
		for (TaskGroup taskGroup : lecture.getTaskGroups()) {
			String selected = "";
			if (taskGroup.getTaskGroupId() == task.getTaskGroup().getTaskGroupId()) {
				selected = " selected";
			}
			out.println("<option value=\"" + taskGroup.getTaskGroupId() + "\"" + selected + ">" + Util.escapeHTML(taskGroup.getTitle()) + "</option>");
		}
		out.println("</select></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Titel:</th>");
		out.println("<td><input type=text size=100 required=required name=title value=\"" + Util.escapeHTML(task.getTitle()) + "\"></td>");
		out.println("</tr>");
		if (!task.isADynamicTask()) {
			out.println("<tr>");
			out.println("<th>Multiple-Choice-Aufgabe:</th>");
			out.println("<td><input type=checkbox" + (task.getTaskid() != 0 ? " disabled" : "") + (task.isMCTask() ? " checked" : "") + " name=mctask id=mctask onchange=\"if (document.getElementById('mctask').checked) {document.getElementById('dynamicTask').disabled=true;document.getElementById('dynamicTask').selectedIndex=0} else {document.getElementById('dynamicTask').disabled=false;}return true;\"></td>");
			out.println("</tr>");
		}
		if (!task.isMCTask()) {
			out.println("<tr>");
			out.println("<th>Aufgabe mit dynamischen Werten:</th>");
			out.println("<td><select size=1 name=dynamicTask id=dynamicTask " + (task.getTaskid() != 0 ? " disabled" : " onchange=\"if (document.getElementById('dynamicTask').selectedIndex > 0) {document.getElementById('mctask').disabled=true;document.getElementById('mctask').checked=false;} else {document.getElementById('mctask').disabled=false;} getDynamicTaskHints();\"") + ">");
			out.println("<option value=\"\"" + (task.getDynamicTask() == null ? " selected" : "") + ">-</option>");
			for (int i = 0; i < DynamicTaskStrategieFactory.STRATEGIES.length; i++) {
				out.println("<option value=\"" + DynamicTaskStrategieFactory.STRATEGIES[i] + "\"" + (DynamicTaskStrategieFactory.STRATEGIES[i].equals(task.getDynamicTask()) ? " selected" : "") + ">" + DynamicTaskStrategieFactory.NAMES[i] + "</option>");
			}
			out.println("</select><div id=dynamictaskhints></div></td>");
			out.println("</tr>");
		}
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		out.print("<td><textarea cols=60 rows=20 name=description>" + Util.escapeHTML(task.getDescription()) + "</textarea>");
		if (task.getTaskid() != 0 && task.isADynamicTask()) {
			out.print("<br>nach dem Speichern eine Vorschau anzeigen: <input type=checkbox name=dynamiktaskpreview checked>");
		}
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Abgaben mit max. PartnerInnen:</th>");
		out.println("<td><input type=text size=5 id=maxSubmitters name=\"maxSubmitters\" value=\"" + task.getMaxSubmitters() + "\" onkeyup=\"if ($('#maxSubmitters').val()>1) {$('#submitteracrossgroups').show();} else {$('#submitteracrossgroups').hide();}return true;\"><span id=submitteracrossgroups" + (task.getMaxSubmitters() > 1 ? "" : " style=\"display:none;\"") + ">, <input type=checkbox name=allowSubmittersAcrossGroups " + (task.isAllowSubmittersAcrossGroups() ? "checked" : "") + "> über Gruppengrenzen hinweg</span> <a href=\"#\" onclick=\"$('#maxsubmittershelp').toggle(); return false;\">(?)</a><br><span style=\"display:none;\" id=maxsubmittershelp><b>Hilfe:</b><br>Sofern &quot;über Gruppengrenzen hinweg&quot; nicht gesetzt ist, müssen die Studierenden in Gruppen eingeteilt sein und können auch nur Studierende wählen, die in der gleichen Gruppe sind. Die Zahl wird als Gesamtanzahl der Studierenden, die eine Aufgabe gemeinsam bearbeiten dürfen, angesehen. Sind bei der Veranstaltung Abgabegruppen defininert und wird eine Zahl &gt; 1 angegeben, werden immer alle Studierenden der Gruppe bei der ersten Abgabe automatisch hinzugefügt (auch wenn mehr Studierende in der Gruppe sind; ist der abgebende Studierende in keiner Gruppe ist es automatisch eine Einzelabgabe, wenn gruppenübergreifende PartnerInnen verboten sind); die Angabe von &quot;1&quot; erlaubt auch bei Abgabegruppen Individualabgaben. Im Fall von Abgabegrupopen können Studierende, die in einer Abgabegruppe sind, keine gruppenübergreifenden oder beliebigen Partnerabgaben durchführen (auch nicht, wenn gruppenübergreifende Partnerschaften erlaubt sind).</span></td>");
		out.println("</tr>");
		if (task.getTaskid() != 0) {
			out.println("<tr>");
			out.println("<th>Filename Regexp:</th>");
			out.println("<td><input type=text size=100 required=required" + (task.isMCTask() || task.isADynamicTask() ? " disabled" : "") + " id=\"filenameregexp\" name=filenameregexp value=\"" + Util.escapeHTML(task.getFilenameRegexp()) + "\"> <a href=\"#\" onclick=\"$('#fileregexphelp').toggle(); return false;\">(?)</a><br><div style=\"display:none;\" id=fileregexphelp><b>Hilfe:</b><br>Dateinamen, die von Studierende hochgeladen werden, werden mit diesem regulären Ausdruck überprüft, bevor diese verarbeitet werden.<br><br><b>Beispiele (ohne Anführungszeichen):</b><br>Für Java-Dateien: &quot;[A-Z][A-Za-z0-9_]+\\.java&quot;<br>für alle Dateien: &quot;[A-Za-z0-9. _-]+&quot;<br>für DOC/PDF Dateien: &quot;[A-Za-z0-9 _-]+\\.(pdf|doc)&quot; (enthält nicht docx!)<br>ARGOUml: &quot;loesung\\.(xmi|zargo|png)&quot;<br>Java-Dateien und png-Bilder: &quot;([A-Z][A-Za-z0-9_]+\\.java|[A-Za-z0-9 _-]+\\.png)&quot;<br>&quot;-&quot; = Dateiupload nicht anbieten bzw. verbieten<p><b>Dateinamen testen:</b><br><input type=\"text\" id=\"regexptest\" name=\"regexptest\"> <button onclick=\"checkRegexp(); return false;\">Testen</button></div></td>");
			out.println("</tr>");
			if (!task.isADynamicTask() && !task.isMCTask()) {
				out.println("<tr>");
				out.println("<th>Archiv-Filename Regexp:</th>");
				out.println("<td><input type=text size=100 required=required name=archivefilenameregexp value=\"" + Util.escapeHTML(task.getArchiveFilenameRegexp()) + "\"> <a href=\"#\" onclick=\"$('#archivefileregexphelp').toggle(); return false;\">(?)</a><br><span style=\"display:none;\" id=archivefileregexphelp><b>Hilfe:</b><br>Das Hochladen von Archiven (.zip und .jar) muss im o.g. regulären Ausdruck erlaubt werden, um diese Funktion nutzen zu können. Mit diesem regulären Ausdruck werden die Dateien im Archiv geprüft und nur diese extrahiert, andere werden ignoriert. RegExp mit &quot;^&quot; beginnen, um Dateinamen inkl. Pfad festzulegen (&quot;/&quot; ist der Pfad-Separator)<br><br><b>Beispiele (ohne Anführungszeichen):</b><br>Für Java-Dateien: &quot;[A-Z][A-Za-z0-9_]+\\.java&quot;<br>für alle Dateien: &quot;[A-Za-z0-9. _-]+&quot;<br>für DOC/PDF Dateien: &quot;[A-Za-z0-9 _-]+\\.(pdf|doc)&quot; (enthält nicht docx!)<br>Java-Dateien und png-Bilder: &quot;([A-Z][A-Za-z0-9_]+\\.java|[A-Za-z0-9 _-]+\\.png)&quot;<br>&quot;-&quot; = Archive nicht automatisch entpacken</span></td>");
				out.println("</tr>");
			}
			out.println("<tr>");
			out.println("<th>Text-Eingabefeld:</th>");
			out.println("<td><input type=checkbox name=showtextarea" + (task.isADynamicTask() || task.isMCTask() ? " disabled" : "") + (task.isShowTextArea() ? " checked" : "") + "> (wird als textloesung.txt gespeichert)</td>");
			out.println("</tr>");
			if (!task.isADynamicTask() && !task.isMCTask()) {
				out.println("<tr>");
				out.println("<th>Dateien bei TutorInnen aufklappen:</th>");
				out.println("<td><input type=text name=featuredfiles size=100 value=\"" + Util.escapeHTML(task.getFeaturedFiles()) + "\"> <a href=\"#\" onclick=\"$('#featuredfileshelp').toggle(); return false;\">(?)</a><br><span style=\"display:none;\" id=featuredfileshelp><b>Hilfe:</b><br>Dieser reguläre Ausdruck bestimmt welche Dateien bei den Tutoren automatisch aufgeklappt sind. RegExp mit &quot;^&quot; beginnen, um Dateinamen inkl. Pfad festzulegen (&quot;/&quot; ist der Pfad-Separator)<br><br><b>Beispiele (ohne Anführungszeichen):</b><br>Für Java-Dateien: &quot;[A-Z][A-Za-z0-9_]+\\.java&quot;<br>für alle Dateien: &quot;[A-Za-z0-9. _-]+&quot; oder leer<br>für DOC/PDF Dateien: &quot;[A-Za-z0-9 _-]+\\.(pdf|doc)&quot; (enthält nicht docx!)<br>Java-Dateien und png-Bilder: &quot;([A-Z][A-Za-z0-9_]+\\.java|[A-Za-z0-9 _-]+\\.png)&quot;<br>&quot;-&quot; = keine Dateien aufklappen</span></td>");
				out.println("</tr>");
			}
		}
		if (task.getTaskid() != 0 && !task.isADynamicTask() && !task.isMCTask()) {
			out.println("<tr>");
			out.println("<th>TutorInnen dürfen Dateien für Studierende hochladen:</th>");
			out.println("<td><input type=checkbox name=tutorsCanUploadFiles" + (task.isMCTask() ? " disabled" : "") + (task.isTutorsCanUploadFiles() ? " checked" : "") + "></td>");
			out.println("</tr>");
		}
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		out.println("<tr>");
		out.println("<th>Startdatum:</th>");
		out.println("<td><input type=text required=required pattern=\"([012][1-9]|[123][01])\\.[01][0-9]\\.[0-9]{4}( ([0-1][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9])?\" name=startdate value=\"" + Util.escapeHTML(dateFormatter.format(task.getStart())) + "\"> (dd.MM.yyyy oder dd.MM.yyyy HH:mm:ss)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Enddatum:</th>");
		out.println("<td><input type=text required=required pattern=\"([012][1-9]|[123][01])\\.[01][0-9]\\.[0-9]{4}( ([0-1][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9])?\" name=deadline value=\"" + Util.escapeHTML(dateFormatter.format(task.getDeadline())) + "\"> (dd.MM.yyyy oder dd.MM.yyyy HH:mm:ss)</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Punktedatum:</th>");
		String pointsDate = "";
		if (task.getShowPoints() != null) {
			pointsDate = Util.escapeHTML(dateFormatter.format(task.getShowPoints()));
		}
		out.println("<td><input type=checkbox name=pointsmanual " + (task.getShowPoints() == null ? "checked" : "") + "> manuell freischalten oder <input type=text name=pointsdate pattern=\"([012][1-9]|[123][01])\\.[01][0-9]\\.[0-9]{4}( ([0-1][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9])?\" value=\"" + pointsDate + "\"> (dd.MM.yyyy oder dd.MM.yyyy HH:mm:ss)</td>");
		out.println("</tr>");
		if (task.getTaskid() != 0 && !task.isMCTask()) {
			out.println("<tr>");
			out.println("<th>Min. Punkt-Schrittweite:</th>");
			out.println("<td><input type=text size=5 name=minpointstep value=\"" + Util.showPoints(task.getMinPointStep()) + "\"> <b>bei Änderung bereits vergebene Pkts. prüfen!</b></td>");
			out.println("</tr>");
		}
		if (task.getPointCategories() == null || task.getPointCategories().isEmpty()) {
			out.println("<tr>");
			out.println("<th>Max. Punkte:</th>");
			out.println("<td><input type=text size=5 name=maxpoints value=\"" + Util.showPoints(task.getMaxPoints()) + "\">" + (task.getTaskid() != 0 ? " <b>bei Änderung bereits vergebene Pkts. prüfen!</b>" : "") + "</td>");
			out.println("</tr>");
		} else {
			out.println("<tr>");
			out.println("<th>Max. Punkte:</th>");
			out.println("<td><input type=text disabled name=maxpoints value=\"" + Util.showPoints(task.getMaxPoints()) + "\"> (berechnet)</td>");
			out.println("</tr>");
		}
		out.println("<tr>");
		out.println("<th>Vorzeitige finale Abgabe:</th>");
		out.println("<td><input type=checkbox name=prematureClosing" + (task.isAllowPrematureSubmissionClosing() ? " checked" : "") + "> Aufgabe kann vor der Deadline durch die Studierenden als abgeschlossen markiert werden</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.print("<td colspan=2 class=mid><input type=submit value=speichern> <a href=\"");
		if (task.getTaskid() != 0) {
			out.print(Util.generateHTMLLink("ShowTask?taskid=" + task.getTaskid(), response));
		} else {
			out.print(Util.generateHTMLLink("ShowLecture?lecture=" + lecture.getId(), response));
		}
		out.println("\">Abbrechen</a></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");

		if (task.isMCTask()) {
			out.println("<h2>Multiple Choice-Optionen</h2>");
			@SuppressWarnings("unchecked")
			List<MCOption> options = (List<MCOption>) request.getAttribute("mcOptions");
			if (!options.isEmpty()) {
				out.println("<ul>");
				for (MCOption option : options) {
					out.println("<li>" + Util.escapeHTML(option.getTitle()) + " (" + (option.isCorrect() ? "korrekt, " : "") + "<a onclick=\"return sendAsPost(this, 'Wirklich löschen?')\" href=\"" + Util.generateHTMLLink("TaskManager?lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid() + "&action=deleteMCOption&optionId=" + option.getId(), response) + "\">del</a>)</li>");
				}
				out.println("</ul>");
			}
			out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
			out.println("<input type=hidden name=action value=\"newMCOption\">");
			out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
			out.println("<input type=hidden name=lecture value=\"" + lecture.getId() + "\">");
			out.println("Option: <input type=text name=option maxlength=250 required=required style=\"width:90%\"><br>");
			out.println("Korrekt: <input type=checkbox name=correkt><br>");
			out.println("<input type=submit value=hinzufügen>");
			out.println("</form>");
		} else if (task.getTaskid() != 0) {
			out.println("<h2>Punkte</h2>");
			out.println("<p>Werden hier Kriterien angelegt, so wird den Tutoren eine differenzierte Bewertung ermöglicht (für " + Util.showPoints(task.getMinPointStep()) + " Punkte wird eine Checkbox angezeigt, für &gt; " + Util.showPoints(task.getMinPointStep()) + " Punkte erscheint ein Texteingabefeld).</p>");
			if (!task.getPointCategories().isEmpty()) {
				out.println("<ul>");
				for (PointCategory category : task.getPointCategories()) {
					out.println("<li>" + Util.escapeHTML(category.getDescription()) + "; " + Util.showPoints(category.getPoints()) + " Punkte" + (category.isOptional() ? ", optional" : "") + " (<a onclick=\"return sendAsPost(this, 'Wirklich löschen?')\" href=\"" + Util.generateHTMLLink("TaskManager?lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid() + "&action=deletePointCategory&pointCategoryId=" + category.getPointcatid(), response) + "\">del</a>)</li>");
				}
				out.println("</ul>");
			}
			out.println("<form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
			out.println("<input type=hidden name=action value=\"newPointCategory\">");
			out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
			out.println("<input type=hidden name=lecture value=\"" + lecture.getId() + "\">");
			out.println("Kriteria: <input type=text name=description required=required style=\"width:90%\"><br>");
			out.println("Punkte: <input size=5 type=text name=points value=\"" + Util.showPoints(task.getMinPointStep()) + "\"><br>");
			out.println("Optional: <input type=checkbox name=optional> (für Bonuspunkte)<br>");
			out.println("<input type=submit value=speichern>");
			out.println("</form>");
		}

		if (task.getTaskid() != 0) {
			out.println("<h2>Dateien hinterlegen</h2>");
			if (!advisorFiles.isEmpty()) {
				out.println("<ul>");
				for (String file : advisorFiles) {
					file = file.replace(System.getProperty("file.separator"), "/");
					out.println("<li><a href=\"" + Util.generateHTMLLink("DownloadTaskFile/" + Util.encodeURLPathComponent(file) + "?taskid=" + task.getTaskid(), response) + "\">Download " + Util.escapeHTML(file) + "</a> (<a onclick=\"return sendAsPost(this, 'Wirklich löschen?')\" href=\"" + Util.generateHTMLLink("DownloadTaskFile/" + Util.encodeURLPathComponent(file) + "?action=delete&taskid=" + task.getTaskid(), response) + "\">del</a>)</li>");
				}
				out.println("</ul>");
			}
			out.println("<FORM class=mid ENCTYPE=\"multipart/form-data\" method=POST action=\"" + Util.generateHTMLLink("?action=uploadTaskFile&lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid(), response) + "\">");
			out.println("<p>Bitte wählen Sie eine oder mehrere Dateien aus, die Sie den Studierenden zum Lösen der Aufgabe zur Verfügung stellen möchten:</p>");
			out.println("<INPUT TYPE=file NAME=file multiple required>");
			out.println("<INPUT TYPE=submit VALUE=upload>");
			out.println("</FORM>");

			out.println("<h2>Musterlösung hinterlegen</h2>");
			if (!modelSolutionFiles.isEmpty()) {
				out.println("<p><form action=\"" + Util.generateHTMLLink("?", response) + "\" method=post>");
				out.println("<input type=hidden name=action value=\"provideModelSolutionToStudents\">");
				out.println("<input type=hidden name=taskid value=\"" + task.getTaskid() + "\">");
				out.println("<input type=hidden name=lecture value=\"" + lecture.getId() + "\">");
				out.println("Downloadbar für: <select size=1 name=modelsolutiontype>");
				for (int i = 0; i < ModelSolutionProvisionType.values().length; i++) {
					out.println("<option value=\"" + Util.escapeHTML(ModelSolutionProvisionType.values()[i].toString()) + "\"" + (ModelSolutionProvisionType.values()[i].equals(task.getModelSolutionProvisionType()) ? " selected" : "") + ">" + Util.escapeHTML(ModelSolutionProvisionType.values()[i].getInfo()) + "</option>");
				}
				out.println("</select> <input type=submit value=speichern>");
				out.println("</form></p>");

				out.println("<ul>");
				for (String file : modelSolutionFiles) {
					file = file.replace(System.getProperty("file.separator"), "/");
					out.println("<li><a href=\"" + Util.generateHTMLLink("DownloadModelSolutionFile/" + Util.encodeURLPathComponent(file) + "?taskid=" + task.getTaskid(), response) + "\">Download " + Util.escapeHTML(file) + "</a> (<a onclick=\"return sendAsPost(this, 'Wirklich löschen?')\" href=\"" + Util.generateHTMLLink("DownloadModelSolutionFile/" + Util.encodeURLPathComponent(file) + "?action=delete&taskid=" + task.getTaskid(), response) + "\">del</a>)</li>");
				}
				out.println("</ul>");
			}
			out.println("<FORM class=mid ENCTYPE=\"multipart/form-data\" method=POST action=\"" + Util.generateHTMLLink("?action=uploadModelSolutionFile&lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid(), response) + "\">");
			out.println("<p>Bitte wählen Sie eine oder mehrere Dateien aus, die zur Musterlösung gehört bzw. diese darstellt:</p>");
			out.println("<INPUT TYPE=file NAME=file multiple required>");
			out.println("<INPUT TYPE=submit VALUE=upload>");
			out.println("</FORM>");
		}

		// don't show for new tasks
		if (task.getTaskid() != 0 && (task.isShowTextArea() == true || !"-".equals(task.getFilenameRegexp()))) {
			out.println("<h2>Ähnlichkeitsprüfungen</h2>");
			if (!task.getSimularityTests().isEmpty()) {
				out.println("<ul>");
				for (SimilarityTest similarityTest : task.getSimularityTests()) {
					out.print("<li>" + Util.escapeHTML(similarityTest.toString()) + "<br>");
					out.println("Ignored Files: " + Util.escapeHTML(similarityTest.getExcludeFiles()) + "<br>");
					out.print("Status: ");
					if (similarityTest.getStatus() == 1) {
						out.println("in Queue, noch nicht ausgeführt<br>");
					} else if (similarityTest.getStatus() == 2) {
						out.println("in Ausführung<br>");
					} else {
						out.println("bereits ausgeführt - <a onclick=\"return sendAsPost(this, 'Wirklich erneut ausführen?')\" href=\"" + Util.generateHTMLLink("DupeCheck?action=rerunSimilarityTest&similaritytestid=" + similarityTest.getSimilarityTestId() + "&taskid=" + task.getTaskid(), response) + "\">erneut ausführen</a><br>");
					}
					out.println("<a onclick=\"return sendAsPost(this, 'Wirklich löschen?')\" href=\"" + Util.generateHTMLLink("DupeCheck?action=deleteSimilarityTest&taskid=" + task.getTaskid() + "&similaritytestid=" + similarityTest.getSimilarityTestId(), response) + "\">löschen</a></li>");
				}
				out.println("</ul>");
			}
			out.println("<p class=mid><a href=\"" + Util.generateHTMLLink("DupeCheck?taskid=" + task.getTaskid(), response) + "\">Ähnlichkeitsprüfung hinzufügen</a><p>");

			if (!task.isADynamicTask()) {
				out.println("<h2>Funktionstests der Abgaben</h2>");
				out.println("<p class=mid><a href=\"" + Util.generateHTMLLink("TestManager?action=newTest&taskid=" + task.getTaskid(), response) + "\">Test hinzufügen</a></p>");
				if (!task.getTests().isEmpty()) {
					out.println("<ul>");
					for (Test test : task.getTests()) {
						out.println("<li>&quot;" + Util.escapeHTML(test.getTestTitle()) + "&quot;: ");
						if (test instanceof RegExpTest) {
							RegExpTest regexptest = (RegExpTest) test;
							out.println("RegExp-Test:<br>Prüfpattern: " + Util.escapeHTML(regexptest.getRegularExpression()) + "<br>Parameter: " + Util.escapeHTML(regexptest.getCommandLineParameter()) + "<br>Main-Klasse: " + Util.escapeHTML(regexptest.getMainClass()) + "<br>");
						} else if (test instanceof CompileTest) {
							out.println("Compile-Test<br>");
						} else if (test instanceof JUnitTest) {
							out.println("JUnit-Test<br>");
						} else if (test instanceof CommentsMetricTest) {
							out.println("Kommentar-Metrik-Test<br>");
						} else if (test instanceof JavaAdvancedIOTest) {
							out.println("Erweiterer Java-IO-Test<br>");
						} else {
							out.println("unknown<br>");
						}
						out.println("# ausführbar für Studierende: " + test.getTimesRunnableByStudents() + " (" + (test.isGiveDetailsToStudents() ? "mit" : "ohne") + " Details)<br>");
						out.println("Tutortest: " + test.isForTutors() + "<br>");
						if (test.isForTutors()) {
							out.print("Status: ");
							if (test.isNeedsToRun()) {
								out.println("in Queue, noch nicht ausgeführt<br>");
							} else {
								out.println("in Ausführung bzw. bereits ausgeführt - <a onclick=\"return sendAsPost(this, 'Wirklich erneut ausführen?')\" href=\"" + Util.generateHTMLLink("TestManager?action=rerunTest&testid=" + test.getId() + "&taskid=" + task.getTaskid(), response) + "\">erneut ausführen</a><br>");
							}
						}
						if (test instanceof JavaAdvancedIOTest) {
							out.println("Bestehend aus " + ((JavaAdvancedIOTest) test).getTestSteps().size() + " Schritten<br>");
							out.println("<a href=\"" + Util.generateHTMLLink("JavaAdvancedIOTestManager?testid=" + test.getId(), response) + "\">Test bearbeiten</a><br>");
						}
						out.println("<a onclick=\"return sendAsPost(this, 'Wirklich löschen?')\" href=\"" + Util.generateHTMLLink("TestManager?action=deleteTest&testid=" + test.getId() + "&taskid=" + task.getTaskid(), response) + "\">Test löschen</a>");
						out.println("</li>");
					}
					out.println("</ul>");
				}
			}
		}
		template.printTemplateFooter();
	}
}
