/*
 * Copyright 2010, 2022-2023 Sven Strickroth <email@cs-ware.de>
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

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.TaskGroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;

/**
 * Data Access Object implementation for the TaskDAOIf
 * @author Sven Strickroth
 */
public class TaskGroupDAO extends AbstractDAO implements TaskGroupDAOIf {
	public TaskGroupDAO(Session session) {
		super(session);
	}

	@Override
	public void deleteTaskGroup(TaskGroup taskGroup) {
		Session session = getSession();
		session.remove(taskGroup);
	}

	@Override
	public TaskGroup getTaskGroup(int taskGroupId) {
		return getSession().get(TaskGroup.class, taskGroupId);
	}

	@Override
	public TaskGroup newTaskGroup(String title, Lecture lecture) {
		Session session = getSession();
		TaskGroup taskGroup = new TaskGroup(title, lecture);
		session.persist(taskGroup);
		return taskGroup;
	}
}
