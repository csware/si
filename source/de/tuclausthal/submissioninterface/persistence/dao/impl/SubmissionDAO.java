/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;

/**
 * Data Access Object implementation for the SubmissionDAOIf
 * @author Sven Strickroth
 */
public class SubmissionDAO implements SubmissionDAOIf {
	@Override
	public Submission getSubmission(int submissionid) {
		return (Submission) HibernateSessionHelper.getSession().get(Submission.class, submissionid);
	}

	@Override
	public Submission getSubmission(Task task, User user) {
		return (Submission) HibernateSessionHelper.getSession().createCriteria(Submission.class).add(Restrictions.eq("task", task)).createCriteria("submitter").add(Restrictions.eq("user", user)).uniqueResult();
	}

	@Override
	public Submission createSubmission(Task task, Participation submitter) {
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
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
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
		session.saveOrUpdate(submission);
		tx.commit();
	}

	@Override
	public List<Submission> getSubmissionsForTaskOrdered(Task task) {
		return (List<Submission>) HibernateSessionHelper.getSession().createCriteria(Submission.class).add(Restrictions.eq("task", task)).createCriteria("submitter").addOrder(Order.asc("group")).createCriteria("user").addOrder(Order.asc("lastName")).addOrder(Order.asc("firstName")).list();
	}

	@Override
	public boolean deleteIfNoFiles(Submission submission, File submissionPath) {
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
		session.lock(submission, LockMode.UPGRADE);
		boolean result = false;
		if (submissionPath.listFiles().length == 0 && submissionPath.delete()) {
			session.delete(submission);
			result = true;
		}
		tx.commit();
		return result;
	}
}
