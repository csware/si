/*
 * Copyright 2009, 2020-2023 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import jakarta.json.Json;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry.LogAction;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

/**
 * Data Access Object implementation for the TestDAOIf
 * @author Sven Strickroth
 */
public class LogDAO extends AbstractDAO {
	public LogDAO(Session session) {
		super(session);
	}

	public LogEntry createLogEntryForStudentTest(User user, Test test, Task task, Boolean result, String testOutput) {
		return createLogEntry(user, test, task, LogAction.PERFORMED_TEST, result, testOutput, null);
	}

	public void createLogDeleteEntry(User user, Task task, String filename) {
		Session session = getSession();
		LogEntry logEntry = new LogEntry(user, null, task, LogAction.DELETE_FILE, null, null, Json.createObjectBuilder().add("filename", filename).build().toString());
		session.persist(logEntry);
	}

	public LogEntry createLogUploadEntry(User user, Task task, LogAction logaction, String additionalData) {
		Session session = getSession();
		LogEntry logEntry = new LogEntry(user, null, task, logaction, null, null, additionalData);
		session.persist(logEntry);
		return logEntry;
	}

	private LogEntry createLogEntry(User user, Test test, Task task, LogAction logAction, Boolean result, String testOutput, String additionalData) {
		Session session = getSession();
		LogEntry logEntry = new LogEntry(user, test, task, logAction, result, testOutput, additionalData);
		session.persist(logEntry);
		return logEntry;
	}
}
