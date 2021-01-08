/*
 * Copyright 2020-2021 Sven Strickroth <email@cs-ware.de>
 * Copyright 2019 Dustin Reineke <dustin.reineke@tu-clausthal.de>
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
import java.util.HashSet;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jakarta.mail.internet.MimeUtility;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * controller servlet to download all submissions of a group as a zip file
 */
public class DownloadSubmissionsByGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		RequestAdapter requestAdapter = new RequestAdapter(request);
		Session session = requestAdapter.getSession();

		Task task = DAOFactory.TaskDAOIf(session).getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			request.setAttribute("title", "Aufgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		// check rights to download all submissions of a group
		ParticipationDAOIf participationDAOIf = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAOIf.getParticipation(requestAdapter.getUser(), task.getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		Group group = null;
		if (request.getParameter("groupid") != null) {
			group = DAOFactory.GroupDAOIf(session).getGroup(Util.parseInteger(request.getParameter("groupid"), 0));
			if (group == null || group.getLecture().getId() != participation.getLecture().getId()) {
				request.setAttribute("title", "Gruppe nicht gefunden");
				request.getRequestDispatcher("MessageView").forward(request, response);
				return;
			}
		}

		// get unique submission ids
		HashSet<Integer> submissionIds = new HashSet<>();
		for (Submission submission : DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOfGroupOrdered(task, group)) {
			submissionIds.add(submission.getSubmissionid());
		};

		// set header
		response.setContentType("application/zip");
		if (group == null) {
			response.setHeader("Content-Disposition", "attachment; filename=\"Abgaben Aufgabe " + task.getTaskid() + " ohne Gruppe.zip\"");
		} else {
			response.setHeader("Content-Disposition", "attachment; filename=\"" + MimeUtility.encodeWord("Abgaben Aufgabe-" + task.getTaskid() + " Gruppe " + group.getName() + ".zip") + "\"");
		}

		ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());

		// add submitted files to zip archive
		final File taskPath = new File(new ContextAdapter(getServletContext()).getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid());
		for (final int submissionId : submissionIds) {
			File submissionDir = new File(taskPath, submissionId + System.getProperty("file.separator"));
			if (!submissionDir.exists()) {
				continue;
			}

			String submitters = submissionId + " " + DAOFactory.SubmissionDAOIf(session).getSubmission(submissionId).getSubmitterNames();
			Util.recursivelyZip(zipOutputStream, submissionDir, submitters + System.getProperty("file.separator"));
		}

		zipOutputStream.close();
	}
}
