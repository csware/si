/*
 * Copyright 2009, 2020 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.persistence.datamodel.LogEntry.LogAction;

/**
 * Data Access Object implementation for the TestDAOIf
 * @author Sven Strickroth
 */
public class LogDAO extends AbstractDAO {
	public LogDAO(Session session) {
		super(session);
	}

	public void createLogEntry(User user, Test test, Task task, LogAction logAction, Boolean result, String testOutput) {
		createLogEntry(user, test, task, logAction, result, testOutput, null, null);
	}

	public void createLogEntry(User user, Test test, Task task, LogAction logAction, Boolean result, String testOutput, String filename, byte[] upload) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		LogEntry logEntry = new LogEntry(user, test, task, logAction, result, testOutput, filename, upload);
		session.save(logEntry);
		tx.commit();
	}
}
