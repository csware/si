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

import de.tuclausthal.submissioninterface.persistence.dao.TestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.CompileTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.RegExpTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;

/**
 * Data Access Object implementation for the TestDAOIf
 * @author Sven Strickroth
 */
public class TestDAO implements TestDAOIf {
	@Override
	public JUnitTest createJUnitTest(Task task) {
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
		session.lock(task, LockMode.UPGRADE);
		JUnitTest test = new JUnitTest();
		test.setTask(task);
		session.save(test);
		tx.commit();
		return test;
	}

	@Override
	public RegExpTest createRegExpTest(Task task) {
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
		session.lock(task, LockMode.UPGRADE);
		RegExpTest test = new RegExpTest();
		test.setTask(task);
		session.save(test);
		tx.commit();
		return test;
	}

	@Override
	public void saveTest(Test test) {
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
		session.lock(test.getTask(), LockMode.UPGRADE);
		session.update(test);
		tx.commit();
	}

	@Override
	public void deleteTest(Test test) {
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
		session.update(test);
		session.delete(test);
		tx.commit();
	}

	@Override
	public Test getTest(int testId) {
		return (Test) HibernateSessionHelper.getSession().get(Test.class, testId);
	}

	@Override
	public CompileTest createCompileTest(Task task) {
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
		session.lock(task, LockMode.UPGRADE);
		CompileTest test = new CompileTest();
		test.setTask(task);
		session.save(test);
		tx.commit();
		return test;
	}
}
