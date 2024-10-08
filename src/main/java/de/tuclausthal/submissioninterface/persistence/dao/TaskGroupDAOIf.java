/*
 * Copyright 2010, 2022, 2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;

/**
 * Data Access Object Interface for the Task-class
 * @author Sven Strickroth
 */
public interface TaskGroupDAOIf {
	/**
	 * Creates and stores a new TaskGroup on the DB with given attribute-values
	 * @param title the title of the task
	 * @param lecture the lecture to which the taskGroup should be associated
	 * @return a new TaskGroup
	 */
	TaskGroup newTaskGroup(String title, Lecture lecture);

	/**
	 * Fetch a taskGroup based on it's id
	 * @param taskGroupId the taskgroup id
	 * @return the TaskGroup or null
	 */
	TaskGroup getTaskGroup(int taskGroupId);

	/**
	 * Deletes a specific taskGroup from the DB
	 * @param taskGroup the taskGroup to remove
	 */
	void deleteTaskGroup(TaskGroup taskGroup);
}
