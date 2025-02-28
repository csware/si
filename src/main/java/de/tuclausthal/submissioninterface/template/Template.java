/*
 * Copyright 2009-2011, 2017, 2020-2023, 2025 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.template;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jakarta.json.Json;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;

import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.AdminMenue;
import de.tuclausthal.submissioninterface.servlets.controller.Noop;
import de.tuclausthal.submissioninterface.servlets.controller.Overview;
import de.tuclausthal.submissioninterface.servlets.controller.ShowLecture;
import de.tuclausthal.submissioninterface.servlets.controller.ShowSubmission;
import de.tuclausthal.submissioninterface.servlets.controller.ShowTask;
import de.tuclausthal.submissioninterface.servlets.controller.TaskManager;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * An easy template
 * @author Sven Strickroth
 */
public abstract class Template {
	protected HttpServletRequest servletRequest;
	protected HttpServletResponse servletResponse;
	protected RequestAdapter requestAdapter;
	protected String prefix;
	private List<String> headers = new ArrayList<>();
	private boolean tinyMCEAdded = false;

	public Template(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		this.servletRequest = servletRequest;
		this.servletResponse = servletResponse;
		prefix = servletRequest.getContextPath();
		requestAdapter = new RequestAdapter(servletRequest);
		addHead("<script src=\"" + prefix + "/assets/scripts.js\"></script>");
	}

	/**
	 * print a HTML page heading
	 * @param title
	 * @throws IOException
	 */
	public void printTemplateHeader(String title) throws IOException {
		if (requestAdapter.getUser() == null) {
			printTemplateHeader(title, "<li><a href=\"" + Util.generateAbsoluteHTMLLink("", servletRequest, servletResponse) + "\">Home</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(Overview.class.getSimpleName(), servletRequest, servletResponse) + "\">GATE Übersicht/Login</a></li>");
			return;
		}
		printTemplateHeader(title, "<li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(Overview.class.getSimpleName(), servletRequest, servletResponse) + "\">Meine Veranstaltungen</a></li><li>" + title + "</li>");
	}

	public void printAdminMenueTemplateHeader() throws IOException {
		printTemplateHeader("Admin-Menü", "<li><a href=\"" + Util.generateHTMLLink(Overview.class.getSimpleName(), servletResponse) + "\">Meine Veranstaltungen</a> – Admin-Menü</li>");
	}

	public void printAdminMenueTemplateHeader(String title) throws IOException {
		printTemplateHeader(title, "<li><a href=\"" + Util.generateHTMLLink(Overview.class.getSimpleName(), servletResponse) + "\">Meine Veranstaltungen</a> – <a href=\"" + Util.generateHTMLLink(AdminMenue.class.getSimpleName(), servletResponse) + "\">Admin-Menü</a></li><li>" + title + "</li>");
	}

	public void printTemplateHeader(Lecture lecture) throws IOException {
		printTemplateHeader("Veranstaltung \"" + Util.escapeHTML(lecture.getName()) + "\"", "<li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(Overview.class.getSimpleName(), servletRequest, servletResponse) + "\">Meine Veranstaltungen</a></li><li>Veranstaltung \"" + Util.escapeHTML(lecture.getName()) + "\"</li>");
	}

	public void printTemplateHeader(String title, Lecture lecture) throws IOException {
		printTemplateHeader(title, "<li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(Overview.class.getSimpleName(), servletRequest, servletResponse) + "\">Meine Veranstaltungen</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + lecture.getId(), servletRequest, servletResponse) + "\">Veranstaltung \"" + Util.escapeHTML(lecture.getName()) + "\"</a></li><li>" + title + "</li>");
	}

	public void printTemplateHeader(Task task) throws IOException {
		printTemplateHeader("Aufgabe \"" + Util.escapeHTML(task.getTitle()) + "\"", "<li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(Overview.class.getSimpleName(), servletRequest, servletResponse) + "\">Meine Veranstaltungen</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + task.getTaskGroup().getLecture().getId(), servletRequest, servletResponse) + "\">Veranstaltung \"" + Util.escapeHTML(task.getTaskGroup().getLecture().getName()) + "\"</a></li><li>Aufgabe \"" + Util.escapeHTML(task.getTitle()) + "\"</li>");
	}

	public void printTemplateHeader(String title, Task task) throws IOException {
		printTaskTemplateHeader(title, task, false);
	}

	public void printEditTaskTemplateHeader(String title, Task task) throws IOException {
		printTaskTemplateHeader(title, task, true);
	}

	private void printTaskTemplateHeader(String title, Task task, boolean editTask) throws IOException {
		if (editTask) {
			printTemplateHeader(title, "<li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(Overview.class.getSimpleName(), servletRequest, servletResponse) + "\">Meine Veranstaltungen</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + task.getTaskGroup().getLecture().getId(), servletRequest, servletResponse) + "\">Veranstaltung \"" + Util.escapeHTML(task.getTaskGroup().getLecture().getName()) + "\"</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), servletRequest, servletResponse) + "\">Aufgabe \"" + Util.escapeHTML(task.getTitle()) + "\"</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(TaskManager.class.getSimpleName() + "?lecture=" + task.getTaskGroup().getLecture().getId() + "&taskid=" + task.getTaskid() + "&action=editTask", servletRequest, servletResponse) + "\">Aufgabe bearbeiten</a></li><li>" + title + "</li>");
			return;
		}
		printTemplateHeader(title, "<li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(Overview.class.getSimpleName(), servletRequest, servletResponse) + "\">Meine Veranstaltungen</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + task.getTaskGroup().getLecture().getId(), servletRequest, servletResponse) + "\">Veranstaltung \"" + Util.escapeHTML(task.getTaskGroup().getLecture().getName()) + "\"</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), servletRequest, servletResponse) + "\">Aufgabe \"" + Util.escapeHTML(task.getTitle()) + "\"</a></li><li>" + title + "</li>");
	}

	public void printTemplateHeader(Submission submission) throws IOException {
		printTemplateHeader("Abgabe von \"" + Util.escapeHTML(submission.getSubmitterNames()) + "\"", "<li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(Overview.class.getSimpleName(), servletRequest, servletResponse) + "\">Meine Veranstaltungen</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + submission.getTask().getTaskGroup().getLecture().getId(), servletRequest, servletResponse) + "\">Veranstaltung \"" + Util.escapeHTML(submission.getTask().getTaskGroup().getLecture().getName()) + "\"</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + submission.getTask().getTaskid(), servletRequest, servletResponse) + "\">Aufgabe \"" + Util.escapeHTML(submission.getTask().getTitle()) + "\"</a></li><li>Abgabe von \"" + Util.escapeHTML(submission.getSubmitterNames()) + "\"</li>");
	}

	public void printTemplateHeader(String title, Submission submission) throws IOException {
		printTemplateHeader("Abgabe von \"" + Util.escapeHTML(submission.getSubmitterNames()) + "\"", "<li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(Overview.class.getSimpleName(), servletRequest, servletResponse) + "\">Meine Veranstaltungen</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + submission.getTask().getTaskGroup().getLecture().getId(), servletRequest, servletResponse) + "\">Veranstaltung \"" + Util.escapeHTML(submission.getTask().getTaskGroup().getLecture().getName()) + "\"</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + submission.getTask().getTaskid(), servletRequest, servletResponse) + "\">Aufgabe \"" + Util.escapeHTML(submission.getTask().getTitle()) + "\"</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowSubmission.class.getSimpleName() + "?sid=" + submission.getSubmissionid(), servletRequest, servletResponse) + "\">Abgabe von \"" + Util.escapeHTML(submission.getSubmitterNames()) + "\"</a></li><li>" + title + "</li>");
	}

	public void printTemplateHeader(Group group) throws IOException {
		printTemplateHeader("Gruppe \"" + Util.escapeHTML(group.getName()) + "\"", "<li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(Overview.class.getSimpleName(), servletRequest, servletResponse) + "\">Meine Veranstaltungen</a></li><li><a href=\"" + Util.generateAbsoluteServletsHTMLLink(ShowLecture.class.getSimpleName() + "?lecture=" + group.getLecture().getId(), servletRequest, servletResponse) + "\">Veranstaltung \"" + Util.escapeHTML(group.getLecture().getName()) + "\"</a></li><li>Gruppe \"" + Util.escapeHTML(group.getName()) + "\"</li>");
	}

	final public void addHead(String header) {
		headers.add(header);
	}

	final protected void getHeads() throws IOException {
		for (String header : headers) {
			servletResponse.getWriter().println(header + "\n");
		}
	}

	final public void addDiffJs() {
		addHead("<script src=\"" + prefix + "/assets/diff.min.js\"></script>");
	}

	final public void addKeepAlive() {
		addHead("<script>keepAlive(\"" + Util.generateAbsoluteServletsHTMLLink(Noop.class.getSimpleName(), servletRequest, servletResponse) + "\", 120);</script>");
	}

	public abstract List<String> getStyleSheetsForWYSIWYGEditor();

	public abstract void printStyleSheets(PrintWriter out);

	/**
	 * Prints the HTML page header with a title and breadcrums
	 * @param title
	 * @param breadCrumb
	 * @throws IOException
	 */
	public abstract void printTemplateHeader(String title, String breadCrumb) throws IOException;

	/**
	 * print a HTML page footer
	 * @throws IOException
	 */
	public abstract void printTemplateFooter() throws IOException;

	final public void addTinyMCE(String selector) {
		if (!tinyMCEAdded) {
			addHead("<script src=\"" + prefix + "/assets/tiny_mce/tinymce.min.js\"></script>");
			tinyMCEAdded = true;
		}
		/* @formatter:off */
		addHead("<script>\ntinyMCE.init({" +
							"selector: '" + StringEscapeUtils.escapeEcmaScript(selector) + "'," +
							"promotion: false," +
							"entity_encoding: 'raw',"+
							"branding: false," +
							"language: 'de'," +
							"resize: 'both'," +
							"browser_spellcheck: true," +
							"block_formats: 'Paragraph=p;Header 1=h1;Header 2=h2;Header 3=h3'," + 
							"plugins: \"table,code,codesample,link,lists,image,quickbars,media,searchreplace,charmap,nonbreaking,help\"," +
							"content_css: " + Json.createArrayBuilder(getStyleSheetsForWYSIWYGEditor()).build().toString() + "," +
							"formats: {" +
							"    alignleft: { selector: 'p,h1,h2,h3,h4,h5,h6,td,th,div,ul,ol,li,table,img', classes: 'left' }," +
							"    aligncenter: { selector: 'p,h1,h2,h3,h4,h5,h6,td,th,div,ul,ol,li,table,img', classes: 'mid' }," +
							"    alignright: { selector: 'p,h1,h2,h3,h4,h5,h6,td,th,div,ul,ol,li,table,img', classes: 'right' }," +
							"    bold: { inline: 'b' }," +
							"    italic: { inline: 'i' }," +
							"    strikethrough: { inline: 'del' }," +
							"    code: { inline: 'code' }," +
							"  }," +
							"menubar: 'edit insert view format table tools help'," +
							"menu: {" +
							"    edit: { title: 'Edit', items: 'undo redo | cut copy paste pastetext | selectall | searchreplace | newdocument' }," +
							"    insert: { title: 'Insert', items: 'image link media codeexample | template charmap hr | nonbreaking' }," +
							"    view: { title: 'View', items: 'code | visualaid' }," +
							"    format: { title: 'Format', items: 'bold italic strikethrough superscript subscript | styles fontfamily fontsize align | forecolor backcolor | removeformat' }," +
							"    table: { title: 'Table', items: 'inserttable | cell row column | tableprops deletetable' }," +
							"    help: { title: 'Help', items: 'help' }" +
							"  },"+
							"toolbar: [" +
							"    { name: 'history', items: [ 'undo', 'redo' ] }," +
							"    { name: 'styles', items: [ 'styles' ] }," +
							"    { name: 'formatting', items: [ 'bold', 'italic', 'numlist', 'bullist' ] }," +
							"    { name: 'alignment', items: [ 'alignleft', 'aligncenter', 'alignright', 'alignjustify' ] }," +
							"    { name: 'indentation', items: [ 'outdent', 'indent' ] }" +
							"  ]"+
							"});\n</script>");
		/* @formatter:on */
	}

	public void addSortTableJs() {
		addHead("<script src=\"" + prefix + "/assets/sorttable.min.js\"></script>");
		addHead("<script>sorttable.sort_alpha = function(a,b) { return a[0].localeCompare(b[0], 'de'); }</script>");
	}
}
