/*
 * Copyright 2009-2012, 2020-2023 Sven Strickroth <email@cs-ware.de>
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

import java.time.ZonedDateTime;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.TaskDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task_;

/**
 * Data Access Object implementation for the TaskDAOIf
 * @author Sven Strickroth
 */
public class TaskDAO extends AbstractDAO implements TaskDAOIf {
	public TaskDAO(Session session) {
		super(session);
	}

	@Override
	public Task getTask(int taskid) {
		return getSession().get(Task.class, taskid);
	}

	@Override
	public Task newTask(String title, int maxPoints, ZonedDateTime start, ZonedDateTime deadline, String description, TaskGroup taskGroup, ZonedDateTime showPoints, int maxSubmitters, boolean allowSubmittersAcrossGroups, String taskType, String dynamicTask, boolean allowPrematureSubmissionClosing) {
		Session session = getSession();
		Task task = new Task(title, maxPoints, start, deadline, description, taskGroup, showPoints, maxSubmitters, allowSubmittersAcrossGroups, taskType, dynamicTask, allowPrematureSubmissionClosing);
		session.persist(task);
		return task;
	}

	@Override
	public void deleteTask(Task task) {
		Session session = getSession();
		session.remove(task);
	}

	@Override
	public List<Task> getTasks(Lecture lecture, boolean onlyStudentVisible) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Task> criteria = builder.createQuery(Task.class);
		Root<Task> root = criteria.from(Task.class);
		root.fetch(Task_.taskGroup);
		criteria.select(root);
		Predicate where = builder.equal(root.get(Task_.taskGroup).get(TaskGroup_.lecture), lecture);
		if (onlyStudentVisible) {
			where = builder.and(where, builder.lessThanOrEqualTo(root.get(Task_.start), ZonedDateTime.now()));
		}
		criteria.where(where);
		criteria.orderBy(builder.asc(root.get(Task_.taskGroup)), builder.asc(root));
		return session.createQuery(criteria).list();
	}
}
