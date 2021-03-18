/*
 * Copyright 2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.LockModeType;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.PointsDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.persistence.dto.SubmissionAssignPointsDTO;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for mass marking a task
 * @author Sven Strickroth
 *
 */
@MultipartConfig(maxFileSize = Configuration.MAX_UPLOAD_SIZE)
public class MassMarkTask extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		Task task = DAOFactory.TaskDAOIf(session).getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.ADVISOR) < 0 || task.getMaxSubmitters() > 1) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (!task.getPointCategories().isEmpty()) {
			request.setAttribute("title", "Aufgabe nicht unterstützt");
			request.setAttribute("message", "Aktuell werden für den Upload nur Aufgaben ohne detaillierte Bewertungskriterien unterstützt.");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		request.setAttribute("task", task);
		getServletContext().getNamedDispatcher("MassMarkTaskView").forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		Task task = DAOFactory.TaskDAOIf(session).getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.ADVISOR) < 0 || task.getMaxSubmitters() > 1) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (request.getPart("file") == null) {
			request.setAttribute("title", "Keine Datei gefunden.");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		if (!task.getPointCategories().isEmpty()) {
			request.setAttribute("title", "Aufgabe nicht unterstützt");
			request.setAttribute("message", "Aktuell werden für den Upload nur Aufgaben ohne detaillierte Bewertungskriterien unterstützt.");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		UserDAOIf userDAO = DAOFactory.UserDAOIf(session);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		List<String[]> lines;
		final CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
		try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(request.getPart("file").getInputStream(), StandardCharsets.UTF_8)).withCSVParser(parser).build()) {
			try {
				lines = reader.readAll();
			} catch (CsvException e) {
				request.setAttribute("title", "Fehler beim Lesen der CSV-Datei.");
				getServletContext().getNamedDispatcher("MessageView").forward(request, response);
				return;
			}
		}

		List<String> errors = new ArrayList<>();

		if (lines.size() != lines.stream().map(line -> line[0]).collect(Collectors.toSet()).size()) {
			request.setAttribute("title", "Doppelte E-Mail-Adresse(n) gefunden.");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		List<SubmissionAssignPointsDTO> points = new ArrayList<>();

		boolean dryRun = request.getParameter("dryrun") != null;
		boolean onlyExistingSubmission = request.getParameter("create") == null || !(task.isShowTextArea() == false && "-".equals(task.getFilenameRegexp()));

		Transaction tx = session.beginTransaction();
		for (int i = 1; i < lines.size(); ++i) {
			String[] line = lines.get(i);
			if (line.length != 5) {
				request.setAttribute("title", "Zeile mit ungültigem Format gefunden.");
				getServletContext().getNamedDispatcher("MessageView").forward(request, response);
				return;
			}
			User user = userDAO.getUserByEmail(line[0]);
			if (user == null) {
				errors.add("Email-Adresse \"" + Util.escapeHTML(line[0]) + "\" nicht gefunden.");
				continue;
			}
			Participation studentParticipation = participationDAO.getParticipation(user, task.getTaskGroup().getLecture());
			if (studentParticipation == null) {
				errors.add("Email-Adresse \"" + Util.escapeHTML(line[0]) + "\" nicht in Vorlesung gefunden.");
				continue;
			}
			if (!dryRun) {
				session.lock(studentParticipation, LockModeType.PESSIMISTIC_WRITE);
			}
			Submission submission = submissionDAO.getSubmission(task, studentParticipation.getUser());
			if (onlyExistingSubmission && submission == null) {
				errors.add("Email-Adresse \"" + Util.escapeHTML(line[0]) + "\" hat keine Abgabe.");
				continue;
			}
			if (onlyExistingSubmission) {
				if (!dryRun) {
					session.refresh(submission, LockModeType.PESSIMISTIC_WRITE);
				}
				if (submission.getSubmitters().size() > 1) {
					errors.add("Die Abgabe zu Email-Adresse \"" + Util.escapeHTML(line[0]) + "\" weist mehrere assoziierte Studierende auf.");
					continue;
				}
				if (submission.getPoints() != null) {
					errors.add("Email-Adresse \"" + Util.escapeHTML(line[0]) + "\" wurde bereits bewertet.");
					continue;
				}
			}
			Points point = new Points();
			point.setInternalComment(line[1]);
			point.setPublicComment(line[2]);
			if ("0".equals(line[3])) {
				point.setPointStatus(PointStatus.NICHT_ABGENOMMEN);
			} else if ("1".equals(line[3])) {
				point.setPointStatus(PointStatus.ABGENOMMEN);
			} else if ("2".equals(line[3])) {
				point.setPointStatus(PointStatus.ABGENOMMEN_FAILED);
			} else {
				errors.add("Email-Adresse \"" + Util.escapeHTML(line[0]) + "\" enthält ungültigen \"Abgenommen\" Wert, nur 0/1 ist erlaubt.");
				continue;
			}
			int origPoints = Util.convertToPoints(line[4], task.getMinPointStep());
			int issuedPoints = origPoints;
			if (issuedPoints > task.getMaxPoints()) {
				issuedPoints = task.getMaxPoints();
			}
			if (origPoints != issuedPoints) {
				errors.add("Email-Adresse \"" + Util.escapeHTML(line[0]) + "\" enthielt eine ungültige Punktangabe.");
				continue;
			}
			point.setPoints(issuedPoints);
			point.setIssuedBy(participation);
			points.add(new SubmissionAssignPointsDTO(submission, studentParticipation, point));
		}

		if (dryRun || !errors.isEmpty()) {
			request.setAttribute("errors", errors);
			request.setAttribute("task", task);
			request.setAttribute("points", points);
			getServletContext().getNamedDispatcher("MassMarkTaskView").forward(request, response);
			tx.rollback();
			return;
		}

		PointsDAOIf pointsDAO = DAOFactory.PointsDAOIf(session);
		for (SubmissionAssignPointsDTO submissionAssignPointsDTO : points) {
			Submission submission = submissionAssignPointsDTO.getSubmission();
			if (submission == null) {
				submission = submissionDAO.createSubmission(task, submissionAssignPointsDTO.getParticipation());
			}
			pointsDAO.createPoints(submissionAssignPointsDTO.getPoints().getPoints().intValue(), submissionAssignPointsDTO.getSubmission(), submissionAssignPointsDTO.getPoints().getIssuedBy(), submissionAssignPointsDTO.getPoints().getPublicComment(), submissionAssignPointsDTO.getPoints().getInternalComment(), submissionAssignPointsDTO.getPoints().getTypedPointStatus(), null);
		}
		tx.commit();
		response.sendRedirect(Util.generateRedirectURL("ShowTask?taskid=" + task.getTaskid(), response));
	}
}
