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
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestCount;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;

/**
 * Data Access Object implementation for the TestCountDAOIf
 * @author Sven Strickroth
 */
public class TestCountDAO implements TestCountDAOIf {
	@Override
	public boolean canSeeResultAndIncrementCounter(Test test, User user) {
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
		TestCount testCount = (TestCount) session.createCriteria(TestCount.class).add(Restrictions.eq("test", test)).add(Restrictions.eq("user", user)).setLockMode(LockMode.UPGRADE).setMaxResults(1).uniqueResult();
		if (testCount == null) {
			testCount = new TestCount();
			testCount.setUser(user);
			testCount.setTest(test);
		}
		if (testCount.getTimesExecuted() >= test.getTimesRunnableByStudents()) {
			tx.commit(); // rollback is evil in hibernate! ;)
			return false;
		}
		testCount.setTimesExecuted(testCount.getTimesExecuted() + 1);
		session.saveOrUpdate(testCount);
		tx.commit();
		return true;
	}

	@Override
	public int canStillRunXTimes(Test test, User user) {
		Session session = HibernateSessionHelper.getSession();
		TestCount testCount = (TestCount) session.createCriteria(TestCount.class).add(Restrictions.eq("test", test)).add(Restrictions.eq("user", user)).setMaxResults(1).uniqueResult();
		if (testCount == null) {
			return test.getTimesRunnableByStudents();
		}
		return Math.max(0, testCount.getTimesExecuted() - test.getTimesRunnableByStudents());
	}
}
