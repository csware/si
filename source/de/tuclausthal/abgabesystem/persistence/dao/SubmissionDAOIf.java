package de.tuclausthal.abgabesystem.persistence.dao;

import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public interface SubmissionDAOIf {
	public Submission getSubmission(int submissionid);

	public Submission getSubmission(Task taskid, User user);

	public Submission createSubmission(Task task, Participation submitter);

	public void saveSubmission(Submission submission);
}
