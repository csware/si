/*
 * Copyright 2009, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.TestCountDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestCount;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestCount_;

/**
 * Data Access Object implementation for the TestCountDAOIf
 * @author Sven Strickroth
 */
public class TestCountDAO extends AbstractDAO implements TestCountDAOIf {
	public TestCountDAO(Session session) {
		super(session);
	}

	@Override
	public boolean canSeeResultAndIncrementCounterTransaction(Test test, Submission submission) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		for (Participation participation : submission.getSubmitters()) {
			CriteriaQuery<TestCount> criteria = builder.createQuery(TestCount.class);
			Root<TestCount> root = criteria.from(TestCount.class);
			criteria.select(root);
			criteria.where(builder.and(builder.equal(root.get(TestCount_.test), test), builder.equal(root.get(TestCount_.user), participation.getUser())));
			TestCount testCount = session.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).uniqueResult();
			if (testCount == null) {
				testCount = new TestCount();
				testCount.setUser(participation.getUser());
				testCount.setTest(test);
			}
			if (testCount.getTimesExecuted() >= test.getTimesRunnableByStudents()) {
				return false;
			}
			testCount.setTimesExecuted(testCount.getTimesExecuted() + 1);
			session.saveOrUpdate(testCount);
		}
		return true;
	}

	@Override
	public int canStillRunXTimes(Test test, Submission submission) {
		Session session = getSession();
		int maxExecuted = 0;
		CriteriaBuilder builder = session.getCriteriaBuilder();
		for (Participation participation : submission.getSubmitters()) {
			CriteriaQuery<TestCount> criteria = builder.createQuery(TestCount.class);
			Root<TestCount> root = criteria.from(TestCount.class);
			criteria.select(root);
			criteria.where(builder.and(builder.equal(root.get(TestCount_.test), test), builder.equal(root.get(TestCount_.user), participation.getUser())));
			TestCount testCount = session.createQuery(criteria).uniqueResult();
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
