/*
 * Copyright 2009-2012, 2017 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import java.io.File;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Data Access Object implementation for the SubmissionDAOIf
 * @author Sven Strickroth
 */
public class SubmissionDAO extends AbstractDAO implements SubmissionDAOIf {
	public SubmissionDAO(Session session) {
		super(session);
	}

	@Override
	public Submission getSubmission(int submissionid) {
		return (Submission) getSession().get(Submission.class, submissionid);
	}

	public Submission getSubmissionLocked(int submissionid) {
		return (Submission) getSession().get(Submission.class, submissionid, LockMode.UPGRADE);
	}

	@Override
	public Submission getSubmission(Task task, User user) {
		return (Submission) getSession().createCriteria(Submission.class).add(Restrictions.eq("task", task)).createCriteria("submitters").add(Restrictions.eq("user", user)).uniqueResult();
	}

	@Override
	public Submission getSubmissionLocked(Task task, User user) {
		return (Submission) getSession().createCriteria(Submission.class).add(Restrictions.eq("task", task)).createCriteria("submitters").add(Restrictions.eq("user", user)).setLockMode(LockMode.UPGRADE).uniqueResult();
	}

	@Override
	public Submission createSubmission(Task task, Participation submitter) {
		Session session = getSession();
		Submission submission = getSubmissionLocked(task, submitter.getUser());
		if (submission == null) {
			submission = new Submission(task, submitter);
			session.save(submission);
		}
		return submission;
	}

	@Override
	public void saveSubmission(Submission submission) {
		Session session = getSession();
		session.saveOrUpdate(submission);
	}

	@Override
	public List<Submission> getSubmissionsForTaskOrdered(Task task) {
		return getSession().createCriteria(Submission.class, "sub").add(Restrictions.eq("task", task)).createCriteria("submitters").addOrder(Order.asc("group")).addOrder(Order.asc("sub.submissionid")).list();
	}

	@Override
	public boolean deleteIfNoFiles(Submission submission, File submissionPath) {
		Session session = getSession();
		session.lock(submission, LockMode.UPGRADE);
		boolean result = false;
		Util.recursiveDeleteEmptySubDirectories(submissionPath);
		if (submissionPath.listFiles().length == 0 && submissionPath.delete()) {
			session.delete(submission);
			result = true;
		}
		return result;
	}

	@Override
	public List<Submission> getSubmissionsForTaskOfGroupOrdered(Task task, Group group) {
		if (group == null) {
			return getSession().createCriteria(Submission.class, "sub").add(Restrictions.eq("task", task)).createCriteria("submitters").add(Restrictions.isNull("group")).addOrder(Order.asc("sub.submissionid")).list();	
		}
		return getSession().createCriteria(Submission.class, "sub").add(Restrictions.eq("task", task)).createCriteria("submitters").add(Restrictions.eq("group", group)).addOrder(Order.asc("group")).addOrder(Order.asc("sub.submissionid")).list();
	}

	@Override
	public Submission getUngradedSubmission(Task task, int lastSubmissionID) {
		return (Submission) getSession().createCriteria(Submission.class, "sub").add(Restrictions.gt("submissionid", lastSubmissionID)).add(Restrictions.eq("task", task)).createCriteria("submitters").add(Restrictions.isNull("sub.points")).addOrder(Order.asc("sub.submissionid")).setMaxResults(1).uniqueResult();
	}

	@Override
	public Submission getUngradedSubmission(Task task, int lastSubmissionID, Group group) {
		if (group == null) {
			return (Submission) getSession().createCriteria(Submission.class, "sub").add(Restrictions.gt("submissionid", lastSubmissionID)).add(Restrictions.eq("task", task)).createCriteria("submitters").add(Restrictions.isNull("group")).add(Restrictions.isNull("sub.points")).addOrder(Order.asc("group")).addOrder(Order.asc("sub.submissionid")).setMaxResults(1).uniqueResult();
		}
		return (Submission) getSession().createCriteria(Submission.class, "sub").add(Restrictions.gt("submissionid", lastSubmissionID)).add(Restrictions.eq("task", task)).createCriteria("submitters").add(Restrictions.eq("group", group)).add(Restrictions.isNull("sub.points")).addOrder(Order.asc("group")).addOrder(Order.asc("sub.submissionid")).setMaxResults(1).uniqueResult();
	}
}
