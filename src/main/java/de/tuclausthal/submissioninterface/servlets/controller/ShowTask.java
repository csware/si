/*
 * Copyright 2009-2011, 2020-2025 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.ModelSolutionProvisionType;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.servlets.view.ShowTaskDescriptionView;
import de.tuclausthal.submissioninterface.servlets.view.ShowTaskStudentView;
import de.tuclausthal.submissioninterface.servlets.view.ShowTaskTutorCSVView;
import de.tuclausthal.submissioninterface.servlets.view.ShowTaskTutorPrintView;
import de.tuclausthal.submissioninterface.servlets.view.ShowTaskTutorView;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.TaskPath;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for displaying a task
 * loads a task and differs between the student and tutor view 
 * @author Sven Strickroth
 */
@GATEController
public class ShowTask extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), task.getTaskGroup().getLecture());
		if (participation == null) {
			if (task.getTaskGroup().getLecture().getSemester() == Util.getCurrentSemester()) {
				request.setAttribute("title", "Zugriff verweigert (403)");
				request.setAttribute("message", "<p>Sie versuchen auf eine Aufgabe einer Vorlesung zuzugreifen, für die Sie nicht angemeldet sind.</p><p>Sie können Sie <a href=\"" + Util.generateHTMLLink(SubscribeToLecture.class.getSimpleName(), response) + "\" target=\"_blank\">hier</a> für die Vorlesung anmelden.</p>");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.getStart().isAfter(ZonedDateTime.now()) && participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		request.setAttribute("participation", participation);

		final Path taskPath = Util.constructPath(Configuration.getInstance().getDataPath(), task);
		request.setAttribute("advisorFiles", Util.listFilesAsRelativeStringListSorted(taskPath.resolve(TaskPath.ADVISORFILES.getPathComponent())));
		request.setAttribute("task", task);
		if (request.getParameter("onlydescription") != null) {
			SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
			Submission submission = submissionDAO.getSubmission(task, RequestAdapter.getUser(request));
			if (submission != null) {
				response.addHeader("SID", String.valueOf(submission.getSubmissionid()));
			}
			getServletContext().getNamedDispatcher(ShowTaskDescriptionView.class.getSimpleName()).forward(request, response);
		} else if (participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
			if (participation.getRoleType().compareTo(ParticipationRole.ADVISOR) >= 0 && "markingcsv".equals(request.getParameter("show"))) {
				getServletContext().getNamedDispatcher(ShowTaskTutorCSVView.class.getSimpleName()).forward(request, response);
				return;
			}
			if ("grouplist".equals(request.getParameter("action"))) {
				Group group = DAOFactory.GroupDAOIf(session).getGroup(Util.parseInteger(request.getParameter("groupid"), 0));
				if (group != null && group.getLecture().getId() == participation.getLecture().getId()) {
					request.setAttribute("group", group);
				}
				getServletContext().getNamedDispatcher(ShowTaskTutorPrintView.class.getSimpleName()).forward(request, response);
			} else {
				request.setAttribute("modelSolutionFiles", Util.listFilesAsRelativeStringListSorted(taskPath.resolve(TaskPath.MODELSOLUTIONFILES.getPathComponent())));
				getServletContext().getNamedDispatcher(ShowTaskTutorView.class.getSimpleName()).forward(request, response);
			}
		} else {
			SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
			Submission submission = submissionDAO.getSubmission(task, RequestAdapter.getUser(request));
			if (submission != null) {
				final Path path = taskPath.resolve(String.valueOf(submission.getSubmissionid()));
				request.setAttribute("submittedFiles", Util.listFilesAsRelativeStringListSorted(path));
				if (task.isADynamicTask()) {
					task.setDescription(task.getDynamicTaskStrategie(session).getTranslatedDescription(submission));
				}
			} else {
				if (task.isADynamicTask()) {
					task.setDescription(task.getDynamicTaskStrategie(session).getTranslatedDescription(participation));
				}
			}
			if (ModelSolutionProvisionType.canStudentAccessModelSolution(task, submission, null, null)) {
				request.setAttribute("modelSolutionFiles", Util.listFilesAsRelativeStringListSorted(taskPath.resolve(String.valueOf(TaskPath.MODELSOLUTIONFILES.getPathComponent()))));
			}

			request.setAttribute("submission", submission);
			getServletContext().getNamedDispatcher(ShowTaskStudentView.class.getSimpleName()).forward(request, response);
		}
	}
}
