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

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.TestCountDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestCount;

/**
 * Data Access Object implementation for the TestCountDAOIf
 * @author Sven Strickroth
 */
public class TestCountDAO extends AbstractDAO implements TestCountDAOIf {
	public TestCountDAO(Session session) {
		super(session);
	}

	@Override
	public boolean canSeeResultAndIncrementCounter(Test test, Submission submission) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		for (Participation participation : submission.getSubmitters()) {
			TestCount testCount = (TestCount) session.createCriteria(TestCount.class).add(Restrictions.eq("test", test)).add(Restrictions.eq("user", participation.getUser())).setLockMode(LockMode.UPGRADE).setMaxResults(1).uniqueResult();
			if (testCount == null) {
				testCount = new TestCount();
				testCount.setUser(participation.getUser());
				testCount.setTest(test);
			}
			if (testCount.getTimesExecuted() >= test.getTimesRunnableByStudents()) {
				tx.commit(); // rollback is evil in hibernate! ;)
				return false;
			}
			testCount.setTimesExecuted(testCount.getTimesExecuted() + 1);
			session.saveOrUpdate(testCount);
		}
		tx.commit();
		return true;
	}

	@Override
	public int canStillRunXTimes(Test test, Submission submission) {
		Session session = getSession();
		int maxExecuted = 0;
		for (Participation participation : submission.getSubmitters()) {
			TestCount testCount = (TestCount) session.createCriteria(TestCount.class).add(Restrictions.eq("test", test)).add(Restrictions.eq("user", participation.getUser())).setMaxResults(1).uniqueResult();
			if (testCount != null) {
				maxExecuted = Math.max(maxExecuted, testCount.getTimesExecuted());
			}
		}
		if (maxExecuted == 0) {
			return test.getTimesRunnableByStudents();
		}
		return Math.max(0, test.getTimesRunnableByStudents() - maxExecuted);
	}
}
