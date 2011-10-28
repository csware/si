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

import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Result;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

/**
 * Data Access Object Interface for the Results-class
 * @author Giselle Rodriguez
 */
public interface ResultDAOIf {
	/**
	 * Fetch the result with the id resultid
	 * @param resultid the resultid to fetch
	 * @return the result or null
	 */
	public Result getResult(int resultid);

	/**
	 * Fetch a result based on the task-user-association
	 * @param task the task to which the submission is associated to
	 * @param user the user who submitted the submission
	 * @return the result of the submission or null
	 */
	public Result getResult(Task task, User user);

	/**
	 * Create and store a new result in the DB
	 * @param result the result of a task
	 */
	public Result createResult(Task task, Participation submitter, String result);

	/**
	 * Returns the results
	 * @param result
	 * @return the list of results
	 */
	public List<Result> getResultsForResult(String result);

	/**
	 * Updates/saves a result in the DB
	 * @param result the result to update
	 */
	public void saveResult(Result result);
}
