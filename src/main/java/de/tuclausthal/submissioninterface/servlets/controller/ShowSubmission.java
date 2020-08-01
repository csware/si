/*
 * Copyright 2009-2012, 2020 Sven Strickroth <email@cs-ware.de>
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
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.PointsDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for loading and displaying a submission to tutors
 * @author Sven Strickroth
 */
public class ShowSubmission extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(Util.parseInteger(request.getParameter("sid"), 0));
		if (submission == null) {
			request.setAttribute("title", "Abgabe nicht gefunden");
			request.getRequestDispatcher("MessageView").forward(request, response);
			return;
		}

		Task task = submission.getTask();

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), submission.getTask().getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		if ((task.getDeadline().before(Util.correctTimezone(new Date())) || (task.isShowTextArea() == false && "-".equals(task.getFilenameRegexp()))) && request.getParameter("points") != null) {
			PointsDAOIf pointsDAO = DAOFactory.PointsDAOIf(session);
			String publicComment = "";
			if (request.getParameter("publiccomment") != null) {
				publicComment = request.getParameter("publiccomment").trim();
			}
			String internalComment = "";
			if (request.getParameter("internalcomment") != null) {
				internalComment = request.getParameter("internalcomment").trim();
			}
			PointStatus pointStatus = PointStatus.NICHT_ABGENOMMEN;
			if ("failed".equals(request.getParameter("pointsstatus"))) {
				pointStatus = PointStatus.ABGENOMMEN_FAILED;
			} else if ("ok".equals(request.getParameter("pointsstatus"))) {
				pointStatus = PointStatus.ABGENOMMEN;
			} else if ("nbewertet".equals(request.getParameter("pointsstatus"))) {
				pointStatus = PointStatus.NICHT_BEWERTET;
			}
			Transaction tx = session.beginTransaction();
			Integer duplicate = null;
			if (request.getParameter("isdupe") != null && Util.parseInteger(request.getParameter("duplicate"), -1) >= 0) {
				duplicate = Util.parseInteger(request.getParameter("duplicate"), -1);
			}
			// attention: quite similar code in MarkEmptyTask
			if (task.getPointCategories().size() > 0) {
				pointsDAO.createPoints(request.getParameterMap(), submission, participation, publicComment, internalComment, pointStatus, duplicate, new ContextAdapter(request.getServletContext()));
			} else {
				pointsDAO.createPoints(Util.convertToPoints(request.getParameter("points")), submission, participation, publicComment, internalComment, pointStatus, duplicate, new ContextAdapter(request.getServletContext()));
			}
			tx.commit();
			String groupAdding = "";
			if (request.getParameter("group") != null && Util.parseInteger(request.getParameter("group"), 0) > 0) {
				groupAdding = "&amp;groupid=" + Util.parseInteger(request.getParameter("group"), 0);
			}
			response.sendRedirect(response.encodeRedirectURL("ShowSubmission?sid=" + submission.getSubmissionid() + groupAdding));
			return;
		}

		if (Util.parseInteger(request.getParameter("partnerid"), 0) > 0) {
			Transaction tx = session.beginTransaction();
			Participation partnerParticipation = participationDAO.getParticipationLocked(Util.parseInteger(request.getParameter("partnerid"), 0));
			session.lock(submission, LockMode.UPGRADE);
			if (submission.getSubmitters().size() < task.getMaxSubmitters() && partnerParticipation != null && partnerParticipation.getRoleType().equals(ParticipationRole.NORMAL) && partnerParticipation.getLecture().getId() == task.getTaskGroup().getLecture().getId() && ((task.isAllowSubmittersAcrossGroups() && (partnerParticipation.getGroup() == null || !partnerParticipation.getGroup().isSubmissionGroup())) || (!task.isAllowSubmittersAcrossGroups() && partnerParticipation.getGroup() != null && submission.getSubmitters().iterator().next().getGroup() != null && partnerParticipation.getGroup().getGid() == submission.getSubmitters().iterator().next().getGroup().getGid())) && submissionDAO.getSubmission(task, partnerParticipation.getUser()) == null) {
				submission.getSubmitters().add(partnerParticipation);
				session.update(submission);
				tx.commit();
				response.sendRedirect(response.encodeRedirectURL("ShowSubmission?sid=" + submission.getSubmissionid()));
				return;
			}
			tx.rollback();
			PrintWriter out = response.getWriter();
			Template template = TemplateFactory.getTemplate(request, response);
			template.printTemplateHeader("Ungültige Anfrage");
			out.println("<div class=mid>Der ausgewählte Studierende hat bereits eine eigene Abgabe initiiert, es wurde die maximale Anzahl von Studierenden überschritten oder es wurde eine nicht verfügbarer Studierender ausgewählt.</div>");
			template.printTemplateFooter();
			return;
		}

		request.setAttribute("submission", submission);
		request.setAttribute("submittedFiles", Util.listFilesAsRelativeStringList(new File(new ContextAdapter(getServletContext()).getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"))));
		request.getRequestDispatcher("ShowSubmissionView").forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// don't want to have any special post-handling
		doGet(request, response);
	}
}
