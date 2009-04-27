package de.tuclausthal.abgabesystem.persistence.dao.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

/**
 * Data Access Object implementation for the SubmissionDAOIf
 * @author Sven Strickroth
 */
public class SubmissionDAO implements SubmissionDAOIf {
	@Override
	public Submission getSubmission(int submissionid) {
		return (Submission) MainBetterNameHereRequired.getSession().get(Submission.class, submissionid);
	}

	@Override
	public Submission getSubmission(Task task, User user) {
		return (Submission) MainBetterNameHereRequired.getSession().createCriteria(Submission.class).add(Restrictions.eq("task", task)).createCriteria("submitter").add(Restrictions.eq("user", user)).uniqueResult();
	}

	@Override
	public Submission createSubmission(Task task, Participation submitter) {
		// TODO check for race condition with Task and/or User
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		//session.lock(task, LockMode.UPGRADE);
		//session.lock(submitter, LockMode.UPGRADE);
		//session.lock(submitter.getUser(), LockMode.UPGRADE);
		//participation = (Participation) session.createCriteria(Participation.class).add(Restrictions.eq("lecture", lecture)).add(Restrictions.eq("user", user)).setLockMode(LockMode.UPGRADE).uniqueResult();
		Submission submission = getSubmission(task, submitter.getUser());
		if (submission == null) {
			submission = new Submission(task, submitter);
			session.save(submission);
		}
		tx.commit();
		return submission;
	}

	@Override
	public void saveSubmission(Submission submission) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		session.update(submission);
		tx.commit();
	}
}
