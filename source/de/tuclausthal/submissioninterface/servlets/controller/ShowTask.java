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

package de.tuclausthal.submissioninterface.servlets.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for displaying a task
 * loads a task and differs between the student and tutor view 
 * @author Sven Strickroth
 */
public class ShowTask extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = HibernateSessionHelper.getSession();
		TaskDAOIf taskDAO = DAOFactory.TaskDAOIf(session);
		Task task = taskDAO.getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(session), task.getLecture());
		if (participation == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if (task.getStart().after(Util.correctTimezone(new Date())) && participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		request.setAttribute("participation", participation);
		request.setAttribute("task", task);
		if (participation.getRoleType().compareTo(ParticipationRole.TUTOR) >= 0) {
			if ("grouplist".equals(request.getParameter("action"))) {
				Group group = DAOFactory.GroupDAOIf(session).getGroup(Util.parseInteger(request.getParameter("groupid"), 0));
				request.setAttribute("group", group);
				request.getRequestDispatcher("ShowTaskTutorPrintView").forward(request, response);
			} else {
				request.getRequestDispatcher("ShowTaskTutorView").forward(request, response);
			}
		} else {
			SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
			Submission submission = submissionDAO.getSubmission(task, new SessionAdapter(request).getUser(session));
			if (submission != null) {
				File path = new File(new ContextAdapter(getServletContext()).getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
				request.setAttribute("submittedFiles", Util.listFilesAsRelativeStringList(path));
			}
			request.setAttribute("submission", submission);
			request.getRequestDispatcher("ShowTaskStudentView").forward(request, response);
		}
	}
}
