/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;

/**
 * Data Access Object implementation for the TaskDAOIf
 * @author Sven Strickroth
 */
public class TaskDAO  extends AbstractDAO implements TaskDAOIf {
	public TaskDAO(Session session) {
		super(session);
	}

	@Override
	public Task getTask(int taskid) {
		return (Task) getSession().get(Task.class, taskid);
	}

	@Override
	public Task newTask(String title, int maxPoints, Date start, Date deadline, String description, Lecture lecture, Date showPoints, String filenameregexp, boolean showTextArea) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		Task task = new Task(title, maxPoints, start, deadline, description, lecture, showPoints, filenameregexp, showTextArea);
		session.save(task);
		tx.commit();
		return task;
	}

	@Override
	public void saveTask(Task task) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		session.save(task);
		tx.commit();
	}

	@Override
	public void deleteTask(Task task) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		session.update(task);
		session.delete(task);
		tx.commit();
	}
}
