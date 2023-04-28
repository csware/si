/*
 * Copyright 2021-2023 Sven Strickroth <email@cs-ware.de>
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
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.TestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.ChecklistTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.servlets.view.PerformStudentTestChecklistPart2ResultView;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for storing user results of a checklisttest
 * @author Sven Strickroth
 */
@GATEController
public class ChecklistTestResponse extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);
		TestDAOIf testDAOIf = DAOFactory.TestDAOIf(session);
		Test tst = testDAOIf.getTest(Util.parseInteger(request.getParameter("testid"), 0));
		if (tst == null || !(tst instanceof ChecklistTest)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "Test nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}
		ChecklistTest test = (ChecklistTest) tst;

		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(RequestAdapter.getUser(request), test.getTask().getTaskGroup().getLecture());
		if (participation == null || participation.getRoleType() != ParticipationRole.NORMAL) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		Transaction tx = session.beginTransaction();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<LogEntry> criteria = builder.createQuery(LogEntry.class);
		Root<LogEntry> root = criteria.from(LogEntry.class);
		criteria.select(root);
		criteria.where(builder.and(builder.equal(root.get(LogEntry_.id), Util.parseInteger(request.getParameter("logid"), 0)), builder.equal(root.get(LogEntry_.test), test), builder.equal(root.get(LogEntry_.user), participation.getUser())));
		Query<LogEntry> query = session.createQuery(criteria);
		query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		LogEntry logEntry = query.uniqueResult();

		if (logEntry.getAdditionalData() != null) {
			request.setAttribute("title", "Testergebnis nicht (mehr) verfügbar");
			request.setAttribute("message", "<div class=mid>Das Testergebnis konnte nicht abgerufen werden. Entweder wurde es bereits abgerufen oder es wurde eine neue Sitzung gestartet.<p><a href=\"" + Util.generateHTMLLink(ShowTask.class.getSimpleName() + "?taskid=" + test.getTask().getTaskid(), response) + "\">zurück zur Aufgabe</a></div>");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			tx.rollback();
			return;
		}
		JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
		long now = Instant.now().getEpochSecond();
		jsonObjectBuilder.add("secondsneeded", now - logEntry.getTimeStamp().toInstant().getEpochSecond());
		jsonObjectBuilder.add("lastupdated", now);
		List<Integer> checkedByStudent = test.getCheckItems().stream().filter(checkItem -> request.getParameter("checkitem" + checkItem.getCheckitemid()) != null).map(checkitem -> checkitem.getCheckitemid()).collect(Collectors.toList());
		jsonObjectBuilder.add("checked", Json.createArrayBuilder(checkedByStudent));
		logEntry.setAdditionalData(jsonObjectBuilder.build().toString());
		logEntry.setResult(test.getCheckItems().stream().allMatch(checkItem -> request.getParameter("checkitem" + checkItem.getCheckitemid()) != null));
		tx.commit();

		request.setAttribute("test", test);
		request.setAttribute("checkedByStudent", checkedByStudent);
		getServletContext().getNamedDispatcher(PerformStudentTestChecklistPart2ResultView.class.getSimpleName()).forward(request, response);
	}
}
