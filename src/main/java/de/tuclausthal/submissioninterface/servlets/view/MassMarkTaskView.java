/*
 * Copyright 2021-2022 Sven Strickroth <email@cs-ware.de>
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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.dto.SubmissionAssignPointsDTO;
import de.tuclausthal.submissioninterface.servlets.GATEView;
import de.tuclausthal.submissioninterface.servlets.controller.ShowTask;
import de.tuclausthal.submissioninterface.servlets.controller.ShowUser;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Mass mark task
 * @author Sven Strickroth
 */
@GATEView
public class MassMarkTaskView extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Task task = (Task) request.getAttribute("task");

		template.printTemplateHeader("Punkte über CSV-Dateiupload vergeben...", task);
		PrintWriter out = response.getWriter();

		out.println("<FORM class=mid ENCTYPE=\"multipart/form-data\" method=POST action=\"" + Util.generateHTMLLink("?taskid=" + task.getTaskid(), response) + "\">");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th colspan=2>Aufgabe \"" + Util.escapeHTML(task.getTitle()) + "\" bewerten</th>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>CSV-Datei mit Bewertungen:</th>");
		out.println("<td><INPUT TYPE=file NAME=file required></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Leere Abgaben anlegen:</th>");
		out.println("<td><input type=checkbox name=create" + (!(task.showTextArea() == false && "-".equals(task.getFilenameRegexp())) ? " disabled" : "") + "></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Existierende Bewertungen aktualisieren:</th>");
		out.println("<td><input type=checkbox name=override></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Vorschau/Dry-Run:</th>");
		out.println("<td><input type=checkbox name=dryrun checked></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td colspan=2 class=mid><INPUT TYPE=submit VALUE=upload> <a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid(), response) + "\">Abbrechen</a></td>");
		out.println("</table>");
		out.println("</FORM>");

		out.println("<h3>Aufbau der CSV-Datei:</h3>");
		out.print("<p>Spalten: E-Mail-Adresse;Interner Kommentar;Externer Kommentar;Abgenommen (0=nicht abgenommen|1=abgenommen|2=nicht bestanden)");
		if (!task.getPointCategories().isEmpty()) {
			for (PointCategory category : task.getPointCategories()) {
				out.print(";\"" + Util.escapeHTML(category.getDescription()) + "\"");
			}
		} else {
			out.print(";Punkte");
		}
		out.println("</p>");
		out.println("<p><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + task.getTaskid() + "&show=markingcsv", response) + "\">Download Template/aktuelle Bewertungen</a></p>");
		out.println("<p>Die Datei ist UTF-8 kodiert, enthält eine Überschriftenzeile, die Felder sind durch \";\" getrennt, Punkte haben \",\" oder \".\" als Dezimaltrennzeichen und Werte mit Zeilenumbruch, doppelte Anführungszeichen oder Semikolon sind in doppelte Anführungszeichen eingeschlossen (Escaping von Anführungszeichen mit \"\\\").</p>");

		template.printTemplateFooter();
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		Task task = (Task) request.getAttribute("task");
		@SuppressWarnings("unchecked")
		List<String> errors = (List<String>) request.getAttribute("errors");
		@SuppressWarnings("unchecked")
		List<SubmissionAssignPointsDTO> points = (List<SubmissionAssignPointsDTO>) request.getAttribute("points");

		template.printTemplateHeader("Punkte über CSV-Dateiupload vergeben... (dry run)", task);
		PrintWriter out = response.getWriter();

		if (!errors.isEmpty()) {
			out.println("<span class=red><b>Fehler, die behoben werden müssen:</b></span><br>");
			out.println("<ul>");
			for (String error : errors) {
				out.println("<li>" + error + "</li>");
			}
			out.println("</ul>");
			out.println("<hr>");
		}

		out.println("<table>");
		out.println("<tr>");
		out.println("<th>TeilnehmerIn</th>");
		out.println("<th>Interner Kommentar</th>");
		out.println("<th>Öffentlicher Kommentar</th>");
		out.println("<th>Abgenommen</th>");
		for (PointCategory category : task.getPointCategories()) {
			out.println("<th>" + Util.escapeHTML(category.getDescription()) + "</th>");
		}
		out.println("<th>Gesamt Punkte</th>");
		out.println("</tr>");
		for (SubmissionAssignPointsDTO submissionAssignPointsDTO : points) {
			out.println("<tr>");
			out.println("<td><a href=\"" + Util.generateHTMLLink(ShowUser.class.getSimpleName() + "?uid=" + submissionAssignPointsDTO.getParticipation().getUser().getUid(), response) + "\">" + Util.escapeHTML(submissionAssignPointsDTO.getParticipation().getUser().getFullName()) + "</a></td>");
			out.println("<td>" + Util.escapeHTML(submissionAssignPointsDTO.getPoints().getInternalComment()) + "</td>");
			out.println("<td>" + Util.escapeHTML(submissionAssignPointsDTO.getPoints().getPublicComment()) + "</td>");
			out.println("<td>" + Util.boolToHTML(submissionAssignPointsDTO.getPoints().getPointsOk()) + "</td>");
			if (submissionAssignPointsDTO.getPointCategories() != null) {
				for (Integer categoryPoints : submissionAssignPointsDTO.getPointCategories()) {
					out.println("<td class=\"points\">" + Util.showPoints(categoryPoints) + "</td>");
				}
			}
			out.println("<td class=\"points" + Util.getPointsCSSClass(submissionAssignPointsDTO.getPoints()) + "\">" + Util.showPoints(submissionAssignPointsDTO.getPoints().getPointsByStatus(task.getMinPointStep())) + "</td>");
			out.println("</tr>");
		}
		out.println("</table>");
		out.println("<p>Gültige Zeilen: " + points.size() + "</p>");

		template.printTemplateFooter();
	}
}
