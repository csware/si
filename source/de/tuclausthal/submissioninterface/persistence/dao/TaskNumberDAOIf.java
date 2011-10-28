/*
 * Copyright 2011 Giselle Rodriguez
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

import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

/**
 * Data Access Object Interface for the Results-class
 * @author Giselle Rodriguez
 */
public interface TaskNumberDAOIf {
	/**
	 * Fetch the TaskNumber with the id tasknumberid
	 * @param tasknumberid the tasknumberid to fetch
	 * @return the TaskNumber or null
	 */
	public TaskNumber getTaskNumber(int tasknumberid);

	/**
	 * Fetch the TaskNumber with the id tasknumberid
	 * @param submissionid the id to fetch
	 * @param number the number to fetch
	 * @param type the type of the number to fetch
	 * @return the TaskNumber or null
	 */
	public List<TaskNumber> getTaskNumber(int taskid, int userid, int submissionid, String number, char type);

	/**
	 * Return the TaskNumbers of a submission
	 * @param submissionid the id to fetch
	 * @return the list of numbers of a Task
	 */
	public List<TaskNumber> getTaskNumbersforTask(int submissionid);

	/**
	 * Return the TaskNumbers of a submission
	 * @param submissionid the id to fetch
	 * @return the list of numbers of a Task
	 */
	public List<TaskNumber> getTaskNumbersforTask(int taskid, int user);

	/**
	 * Return the TaskNumbers of a submission
	 * @param submissionid the id to fetch
	 * @return the list of numbers of a Task
	 */
	public List<TaskNumber> getTaskNumbersforTask(int taskid, int user, int submissionid);

	/**
	 * Return the TaskNumbers of a submission
	 * @param submissionid the id to fetch
	 * @param type the type of number to fetch
	 * @return the list of numbers of a Task
	 */
	public List<TaskNumber> getTaskNumbersforTask(int taskid, int userid, int submissionid, char type);

	/**
	 * Create and store a new TaskNumber in the DB
	 * @param number the number to set
	 * @param type the type of the number to set
	 * @return the TaskNumber or null
	 */
	public TaskNumber createTaskNumber(int taskid, int userid, String number, char type);

	/**
	 * Create and store a list of new TaskNumbers in the DB
	 * @param submissionid the id of a submission
	 * @param taskNumberList the list of TaskNumbers
	 * @return the list of TaskNumbers or null
	 */
	public List<TaskNumber> createTaskNumbers(int taskid, int userid, int submissionid, List<TaskNumber> taskNumberList);

	/**
	 * Updates/saves TaskNumbers in the DB
	 * @param submissionsid the id of submission to update
	 */
	public void saveTaskNumbers(int taskid, int userid, int submissionid);

	/**
	 * Updates/saves a TaskNumber in the DB
	 * @param tasknumber the TaskNumber to update
	 */
	public void saveTaskNumber(TaskNumber tasknumber);

	/**
	 * Updates/saves a list of TaskNumbers in the DB
	 * @param tasknumberList the list of TaskNumbers to update
	 */
	public void saveTaskNumbers(List<TaskNumber> taskNumberList);

	/**
	 * Updates/saves a list of TaskNumbers in the DB
	 * @param tasknumberList the list of TaskNumbers to update
	 */
	public boolean updateSubmissionToNull(int submissionid);

	/**
	 * Deletes the tasknumber if it hat no id
	 * @return success of the removal
	 */
	public boolean deleteIfNoId();

	/**
	 * Deletes the list of TaskNumbers
	 * @param submissionid the id of a submission
	 * @return success of the removal
	 */
	public boolean deleteTaskNumbers(int taskid, int userid);
}
