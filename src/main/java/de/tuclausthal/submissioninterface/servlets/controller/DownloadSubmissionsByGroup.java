/*
 * Copyright 2020-2023 Sven Strickroth <email@cs-ware.de>
 * Copyright 2019 Dustin Reineke <dustin.reineke@tu-clausthal.de>
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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * controller servlet to download all submissions of a group as a zip file
 */
@GATEController
public class DownloadSubmissionsByGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		RequestAdapter requestAdapter = new RequestAdapter(request);
		Session session = requestAdapter.getSession();

		Task task = DAOFactory.TaskDAOIf(session).getTask(Util.parseInteger(request.getParameter("taskid"), 0));
		if (task == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Aufgabe nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
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
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				request.setAttribute("title", "Gruppe nicht gefunden");
				getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
				return;
			}
		}

		// get unique submission ids
		HashSet<Integer> submissionIds = new HashSet<>();
		for (Submission submission : DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOfGroupOrdered(task, group)) {
			submissionIds.add(submission.getSubmissionid());
		}

		// set header
		if (group == null) {
			ShowFile.setContentTypeBasedonFilenameExtension(response, "Abgaben Aufgabe " + task.getTaskid() + " ohne Gruppe.zip", true);
		} else {
			ShowFile.setContentTypeBasedonFilenameExtension(response, "Abgaben Aufgabe-" + task.getTaskid() + " Gruppe " + group.getName() + ".zip", true);
		}

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
			// add submitted files to zip archive
			final File taskPath = Util.constructPath(Configuration.getInstance().getDataPath(), task);
			for (final int submissionId : submissionIds) {
				File submissionDir = new File(taskPath, submissionId + System.getProperty("file.separator"));
				if (!submissionDir.exists()) {
					continue;
				}

				String submitters = submissionId + " " + DAOFactory.SubmissionDAOIf(session).getSubmission(submissionId).getSubmitterNames();
				Util.recursivelyZip(zipOutputStream, submissionDir, submitters + System.getProperty("file.separator"));
			}
		}
	}
}
