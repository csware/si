package de.tuclausthal.abgabesystem.persistence.dao;

import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

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
	public Submission getSubmission(int submissionid);

	/**
	 * Fetch a submission based on the task-user-association
	 * @param task the task to which the submission is associated to
	 * @param user the user who submitted the submission
	 * @return the submission or null
	 */
	public Submission getSubmission(Task task, User user);

	/**
	 * Create and store a new submission in the DB
	 * @param task the task to associate the submission to
	 * @param submitter the participation of the submitter
	 * @return the (new or updated) submission
	 */
	public Submission createSubmission(Task task, Participation submitter);

	/**
	 * Updates/saves a submission in the DB
	 * @param submission the submission to update
	 */
	public void saveSubmission(Submission submission);
}
