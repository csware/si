/*
 * Copyright 2009-2012, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import java.time.ZonedDateTime;
import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskGroup;

/**
 * Data Access Object Interface for the Task-class
 * @author Sven Strickroth
 */
public interface TaskDAOIf {
	/**
	 * Creates and stores a new task on the DB with given attribute-values
	 * @param title the title of the task
	 * @param maxPoints the maximum number of points
	 * @param start the start date of the task/when it get's visible to students; if != null show points automatically after that date, otherwise manuel intervention is required
	 * @param deadline the date after which no submission is possible any more
	 * @param description the description of the task (HTML possible)
	 * @param taskGroup the lecture to which the task should be associated
	 * @param showPoints the daten when the issued points will be shown
	 * @param maxSubmitters 
	 * @param allowSubmittersAcrossGroups
	 * @param taskType 
	 * @param dynamicTask task with dynamic nubmers and a special result field for calculated solution
	 * @param allowPrematureSubmissionClosing set the flag to enable the feature that students can close the submission before the deadline ends to allow tutors grading the task
	 * @return a new Task
	 */
	public Task newTask(String title, int maxPoints, ZonedDateTime start, ZonedDateTime deadline, String description, TaskGroup taskGroup, ZonedDateTime showPoints, int maxSubmitters, boolean allowSubmittersAcrossGroups, String taskType, String dynamicTask, boolean allowPrematureSubmissionClosing);

	/**
	 * Fetch a task based on it's id
	 * @param taskid the task id
	 * @return the task or null
	 */
	public Task getTask(int taskid);

	/**
	 * Update/save the task <i>task</i>
	 * @param task the task to update
	 */
	public void saveTask(Task task);

	/**
	 * Deletes a specific task from the DB
	 * @param task the task to remove
	 */
	public void deleteTask(Task task);

	public List<Task> getTasks(Lecture lecture, boolean onlyVisible);
}
