/*
 * Copyright 2009, 2020-2024 Sven Strickroth <email@cs-ware.de>
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

/**
 * Data Access Object Interface for the Submission-class
 * @author Sven Strickroth
 */
public interface SubmissionDAOIf {
	/**
	 * Fetch the submission with the id submissionid
	 * @param submissionid the submissionid to fetch
	 * @return the submission or null
	 */
	Submission getSubmission(int submissionid);

	Submission getSubmissionLocked(int submissionid);

	/**
	 * Fetch a submission based on the task-user-association
	 * @param task the task to which the submission is associated to
	 * @param user the user who submitted the submission
	 * @return the submission or null
	 */
	Submission getSubmission(Task task, User user);

	/**
	 * Create and store a new submission in the DB
	 * @param task the task to associate the submission to
	 * @param submitter the participation of the submitter
	 * @return the (new or existing) submission
	 */
	Submission createSubmission(Task task, Participation submitter);

	/**
	 * Returns the submission of a task orderd by name
	 * @param task
	 * @param honourGroups
	 * @return the list of submissions
	 */
	List<Submission> getSubmissionsForTaskOrdered(Task task, boolean honourGroups);

	/**
	 * Returns the submission of a group for a specific task orderd by name
	 * @param task 
	 * @param group
	 * @return the list of submissions
	 */
	List<Submission> getSubmissionsForTaskOfGroupOrdered(Task task, Group group);

	/**
	 * Deletes a submission if it contains no files
	 * @param submission
	 * @param submissionPath
	 * @return success of the removal
	 * @throws IOException 
	 */
	boolean deleteIfNoFiles(Submission submission, Path submissionPath) throws IOException;

	Submission getUngradedSubmission(Task task, int lastSubmissionID, boolean reverse);

	Submission getUngradedSubmission(Task task, int lastSubmissionID, Group group, boolean reverse);

	List<Submission> getSubmissionsForSearch(Task task, String searchString, boolean publicComment, boolean privateComment, boolean testResults);

	List<Submission> getAllSubmissions(Participation submitter);
}
