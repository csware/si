/*
 * Copyright 2009-2010, 2020-2022 Sven Strickroth <email@cs-ware.de>
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

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.TestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.ChecklistTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommentsMetricTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.CompileTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.RegExpTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test_;
import de.tuclausthal.submissioninterface.persistence.datamodel.UMLConstraintTest;

/**
 * Data Access Object implementation for the TestDAOIf
 * @author Sven Strickroth
 */
public class TestDAO extends AbstractDAO implements TestDAOIf {
	public TestDAO(Session session) {
		super(session);
	}

	@Override
	public JUnitTest createJUnitTest(Task task) {
		Session session = getSession();
		JUnitTest test = new JUnitTest();
		test.setTask(task);
		session.save(test);
		return test;
	}

	@Override
	public UMLConstraintTest createUMLConstraintTest(Task task) {
		Session session = getSession();
		UMLConstraintTest test = new UMLConstraintTest();
		test.setTask(task);
		session.save(test);
		return test;
	}

	@Override
	public RegExpTest createRegExpTest(Task task) {
		Session session = getSession();
		RegExpTest test = new RegExpTest();
		test.setTask(task);
		session.save(test);
		return test;
	}

	@Override
	public void deleteTest(Test test) {
		Session session = getSession();
		session.delete(test);
	}

	@Override
	public Test getTest(int testId) {
		return getSession().get(Test.class, testId);
	}

	@Override
	public Test getTestLocked(int testId) {
		return getSession().get(Test.class, testId, LockOptions.UPGRADE);
	}

	@Override
	public CompileTest createCompileTest(Task task) {
		Session session = getSession();
		CompileTest test = new CompileTest();
		test.setTask(task);
		session.save(test);
		return test;
	}

	@Override
	public Test takeTest() {
		Session session = getSession();
		Transaction tx = session.beginTransaction();

		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Test> criteria = builder.createQuery(Test.class);
		Root<Test> root = criteria.from(Test.class);
		criteria.select(root);
		criteria.where(builder.and(builder.equal(root.get(Test_.forTutors), true), builder.equal(root.get(Test_.needsToRun), true), builder.lessThan(root.join(Test_.task).get(Task_.deadline), ZonedDateTime.now().minusMinutes(2))));
		Test test = session.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).setMaxResults(1).uniqueResult();
		if (test != null) {
			test.setNeedsToRun(false);
		}
		tx.commit();
		return test;
	}

	@Override
	public List<Test> getStudentTests(Task task) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Test> criteria = builder.createQuery(Test.class);
		Root<Test> root = criteria.from(Test.class);
		criteria.select(root);
		criteria.where(builder.and(builder.equal(root.get(Test_.task), task), builder.gt(root.get(Test_.timesRunnableByStudents), 0)));
		return session.createQuery(criteria).list();
	}

	@Override
	public List<Test> getTutorTests(Task task) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Test> criteria = builder.createQuery(Test.class);
		Root<Test> root = criteria.from(Test.class);
		criteria.select(root);
		criteria.where(builder.and(builder.equal(root.get(Test_.task), task), builder.equal(root.get(Test_.forTutors), true)));
		return session.createQuery(criteria).list();
	}

	@Override
	public CommentsMetricTest createCommentsMetricTest(Task task) {
		Session session = getSession();
		CommentsMetricTest test = new CommentsMetricTest();
		test.setTask(task);
		session.save(test);
		return test;
	}

	@Override
	public JavaAdvancedIOTest createJavaAdvancedIOTest(Task task) {
		Session session = getSession();
		JavaAdvancedIOTest test = new JavaAdvancedIOTest();
		test.setTask(task);
		session.save(test);
		return test;
	}

	@Override
	public DockerTest createDockerTest(Task task) {
		Session session = getSession();
		DockerTest test = new DockerTest();
		test.setTask(task);
		session.save(test);
		return test;
	}

	@Override
	public ChecklistTest createChecklistTest(Task task) {
		Session session = getSession();
		ChecklistTest test = new ChecklistTest();
		test.setTask(task);
		session.save(test);
		return test;
	}
}
