package de.tuclausthal.abgabesystem.persistence.dao;

import java.util.Date;

import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;

/**
 * Data Access Object Interface for the Task-class
 * @author Sven Strickroth
 */
public interface TaskDAOIf {
	/**
	 * Creates and stores a new task on the DB with given attribute-values
	 * @param title the title of the task
	 * @param maxPoints the maximum number of points
	 * @param start the start date of the task/when it get's visible to students
	 * @param deadline the date after which no submission is possible any more
	 * @param description the description of the task (HTML possible)
	 * @param lecture the lecture to which the task should be associated
	 * @param showPoints the daten when the issued points will be shown
	 * @return
	 */
	public Task newTask(String title, int maxPoints, Date start, Date deadline, String description, Lecture lecture,Date showPoints);

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
}
