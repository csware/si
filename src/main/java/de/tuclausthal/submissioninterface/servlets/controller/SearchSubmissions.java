/*
 * Copyright 2011-2012, 2017, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieIf;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for searching a task
 * @author Sven Strickroth
 */
@GATEController
public class SearchSubmissions extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		request.setAttribute("task", task);
		getServletContext().getNamedDispatcher("SearchSubmissionsView").forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (request.getParameterValues("search") == null || request.getParameterValues("search").length == 0 || request.getParameter("q") == null || request.getParameter("search").trim().isEmpty()) {
			request.setAttribute("title", "Nicht gesucht");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		Set<Submission> foundSubmissions = new LinkedHashSet<>();

		if (task.isADynamicTask()) {
			DynamicTaskStrategieIf dynamicTask = task.getDynamicTaskStrategie(session);
			if (arrayContains(request.getParameterValues("search"), "dyntaskdescription")) {
				for (Submission submission : task.getSubmissions()) {
					String result = "";
					for (TaskNumber taskNumber : dynamicTask.getVariables(submission)) {
						if (result.isEmpty()) {
							result = taskNumber.getNumber();
						} else {
							result += " " + taskNumber.getNumber();
						}
					}
					if (result.contains(request.getParameter("q"))) {
						foundSubmissions.add(submission);
						break;
					}
				}
			}
			if (arrayContains(request.getParameterValues("search"), "dyntasksolution")) {
				for (Submission submission : task.getSubmissions()) {
					for (String result : dynamicTask.getUserResults(submission)) {
						if (result.contains(request.getParameter("q"))) {
							foundSubmissions.add(submission);
							break;
						}
					}
				}
			}

		}
		if (arrayContains(request.getParameterValues("search"), "files")) {
			File taskPath = new File(Configuration.getInstance().getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid());
			for (Submission submission : task.getSubmissions()) {
				File submissionPath = new File(taskPath, String.valueOf(submission.getSubmissionid()));
				List<String> files = Util.listFilesAsRelativeStringList(submissionPath);
				for (String file : files) {
					StringBuffer fileContent = Util.loadFile(new File(submissionPath, file));
					if (fileContent.toString().contains(request.getParameter("q"))) {
						foundSubmissions.add(submission);
						break;
					}
				}
			}
		}

		foundSubmissions.addAll(DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task, request.getParameter("q"), arrayContains(request.getParameterValues("search"), "publiccomments"), arrayContains(request.getParameterValues("search"), "privatecomments"), arrayContains(request.getParameterValues("search"), "testresults")));

		request.setAttribute("task", task);
		request.setAttribute("results", foundSubmissions);
		getServletContext().getNamedDispatcher("SearchSubmissionsResultView").forward(request, response);
	}

	static private boolean arrayContains(String array[], String searchFor) {
		assert (searchFor != null);
		for (String string : array) {
			if (searchFor.equals(string)) {
				return true;
			}
		}
		return false;
	}
}
