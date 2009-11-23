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

import java.util.Date;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.TestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.CompileTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.RegExpTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Data Access Object implementation for the TestDAOIf
 * @author Sven Strickroth
 */
public class TestDAO extends AbstractDAO  implements TestDAOIf {
	public TestDAO(Session session) {
		super(session);
	}

	@Override
	public JUnitTest createJUnitTest(Task task) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		JUnitTest test = new JUnitTest();
		test.setTask(task);
		session.save(test);
		tx.commit();
		return test;
	}

	@Override
	public RegExpTest createRegExpTest(Task task) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		RegExpTest test = new RegExpTest();
		test.setTask(task);
		session.save(test);
		tx.commit();
		return test;
	}

	@Override
	public void saveTest(Test test) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		session.update(test);
		tx.commit();
	}

	@Override
	public void deleteTest(Test test) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		session.update(test);
		session.delete(test);
		tx.commit();
	}

	@Override
	public Test getTest(int testId) {
		return (Test) getSession().get(Test.class, testId);
	}

	@Override
	public CompileTest createCompileTest(Task task) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		CompileTest test = new CompileTest();
		test.setTask(task);
		session.save(test);
		tx.commit();
		return test;
	}

	@Override
	public Test takeTest() {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		Test test = (Test) session.createCriteria(Test.class).add(Restrictions.eq("needsToRun", true)).setLockMode(LockMode.UPGRADE).createCriteria("task").add(Restrictions.le("deadline", Util.correctTimezone(new Date()))).setMaxResults(1).uniqueResult();
		if (test != null) {
			test.setNeedsToRun(false);
			session.save(test);
		}
		tx.commit();
		return test;
	}

	@Override
	public List<Test> getStudentTests(Task task) {
		return (List<Test>) getSession().createCriteria(Test.class).add(Restrictions.eq("task", task)).add(Restrictions.gt("timesRunnableByStudents", 0)).list();
	}
}
