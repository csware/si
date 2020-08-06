/*
 * Copyright 2009-2011, 2017 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.template;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * An easy template
 * @author Sven Strickroth
 */
public abstract class Template {
	protected HttpServletResponse servletResponse;
	protected RequestAdapter requestAdapter;
	protected String prefix;
	private List<String> headers = new LinkedList<>();

	public Template(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		this.servletResponse = servletResponse;
		prefix = servletRequest.getContextPath();
		requestAdapter = new RequestAdapter(servletRequest);
	}

	/**
	 * print a HTML page heading
	 * @param title
	 * @throws IOException
	 */
	public void printTemplateHeader(String title) throws IOException {
		printTemplateHeader(title, "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; " + title);
	}

	public void printTemplateHeader(Lecture lecture) throws IOException {
		printTemplateHeader("Veranstaltung \"" + Util.escapeHTML(lecture.getName()) + "\"", "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; Veranstaltung \"" + Util.escapeHTML(lecture.getName()) + "\"");
	}

	public void printTemplateHeader(String title, Lecture lecture) throws IOException {
		printTemplateHeader(title, "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowLecture?lecture=" + lecture.getId()) + "\">Veranstaltung \"" + Util.escapeHTML(lecture.getName()) + "\"</a> &gt; " + title);
	}

	public void printTemplateHeader(Task task) throws IOException {
		printTemplateHeader("Aufgabe \"" + Util.escapeHTML(task.getTitle()) + "\"", "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowLecture?lecture=" + task.getTaskGroup().getLecture().getId()) + "\">Veranstaltung \"" + Util.escapeHTML(task.getTaskGroup().getLecture().getName()) + "\"</a> &gt; " + "Aufgabe \"" + Util.escapeHTML(task.getTitle()) + "\"");
	}

	public void printTemplateHeader(String title, Task task) throws IOException {
		printTemplateHeader(title, "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowLecture?lecture=" + task.getTaskGroup().getLecture().getId()) + "\">Veranstaltung \"" + Util.escapeHTML(task.getTaskGroup().getLecture().getName()) + "\"</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">Aufgabe \"" + Util.escapeHTML(task.getTitle()) + "\"</a> &gt; " + title);
	}

	public void printTemplateHeader(Submission submission) throws IOException {
		printTemplateHeader("Abgabe von \"" + Util.escapeHTML(submission.getSubmitterNames()) + "\"", "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowLecture?lecture=" + submission.getTask().getTaskGroup().getLecture().getId()) + "\">Veranstaltung \"" + Util.escapeHTML(submission.getTask().getTaskGroup().getLecture().getName()) + "\"</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowTask?taskid=" + submission.getTask().getTaskid()) + "\">Aufgabe \"" + Util.escapeHTML(submission.getTask().getTitle()) + "\"</a> &gt; Abgabe von \"" + Util.escapeHTML(submission.getSubmitterNames()) + "\"");
	}

	public void printTemplateHeader(String title, Submission submission) throws IOException {
		printTemplateHeader("Abgabe von \"" + Util.escapeHTML(submission.getSubmitterNames()) + "\"", "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowLecture?lecture=" + submission.getTask().getTaskGroup().getLecture().getId()) + "\">Veranstaltung \"" + Util.escapeHTML(submission.getTask().getTaskGroup().getLecture().getName()) + "\"</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowTask?taskid=" + submission.getTask().getTaskid()) + "\">Aufgabe \"" + Util.escapeHTML(submission.getTask().getTitle()) + "\"</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowSubmission?sid=" + submission.getSubmissionid()) + "\">Abgabe von \"" + Util.escapeHTML(submission.getSubmitterNames()) + "\"</a> &gt; " + title);
	}

	public void printTemplateHeader(Group group) throws IOException {
		printTemplateHeader("Gruppe \"" + Util.escapeHTML(group.getName()) + "\"", "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowLecture?lecture=" + group.getLecture().getId()) + "\">Veranstaltung \"" + Util.escapeHTML(group.getLecture().getName()) + "\"</a> &gt; Gruppe \"" + Util.escapeHTML(group.getName()) + "\"");
	}

	public void addHead(String header) {
		headers.add(header);
	}

	protected void getHeads() throws IOException {
		for (String header : headers) {
			servletResponse.getWriter().println(header + "\n");
		}
	}

	public void addJQuery() {
		addHead("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + prefix + "/jquery/themes/base/jquery.ui.all.css\">");
		addHead("<script type=\"text/javascript\" language=\"JavaScript\" src=\"" + prefix + "/jquery/jquery-1.4.2.min.js\"></script>");
		addHead("<script type=\"text/javascript\" language=\"JavaScript\" src=\"" + prefix + "/jquery/jquery-ui-1.8.1.custom.min.js\"></script>");
	}

	public void addKeepAlive() {
		addHead("<script type=\"text/javascript\">keepAlive(\"" + servletResponse.encodeURL("Noop") + "\", 120);</script>");
	}

	public abstract void printStyleSheets(PrintWriter out);

	/**
	 * Prints the HTML page header with a title and breadcrums
	 * @param title
	 * @param breadCrum
	 * @throws IOException
	 */
	public abstract void printTemplateHeader(String title, String breadCrum) throws IOException;

	/**
	 * print a HTML page footer
	 * @throws IOException
	 */
	public abstract void printTemplateFooter() throws IOException;
}
