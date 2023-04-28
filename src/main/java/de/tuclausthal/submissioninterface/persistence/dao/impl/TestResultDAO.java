/*
 * Copyright 2009-2010, 2020-2023 Sven Strickroth <email@cs-ware.de>
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

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.query.Query;

import de.tuclausthal.submissioninterface.persistence.dao.TestResultDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test_;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

/**
 * Data Access Object implementation for the TestResultDAOIf
 * @author Sven Strickroth
 */
public class TestResultDAO extends AbstractDAO implements TestResultDAOIf {
	public TestResultDAO(Session session) {
		super(session);
	}

	@Override
	public void storeTestResult(Test test, Submission submission, TestExecutorTestResult testExecutorTestResult) {
		Session session = getSession();
		session.lock(test, LockMode.PESSIMISTIC_WRITE);
		TestResult testResult = getResultLocked(test, submission);
		if (testExecutorTestResult == null && testResult != null) {
			session.remove(testResult);
			return;
		}
		if (testResult == null) {
			testResult = new TestResult();
			testResult.setSubmission(submission);
			testResult.setTest(test);
		}
		if (testExecutorTestResult != null) {
			testResult.setPassedTest(testExecutorTestResult.isTestPassed());
			testResult.setTestOutput(testExecutorTestResult.getTestOutput());
			session.persist(testResult);
		}
	}

	private TestResult getResult(Test test, Submission submission, boolean locked) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<TestResult> criteria = builder.createQuery(TestResult.class);
		Root<TestResult> root = criteria.from(TestResult.class);
		criteria.select(root);
		criteria.where(builder.and(builder.equal(root.get(TestResult_.test), test), builder.equal(root.get(TestResult_.submission), submission)));
		Query<TestResult> query = session.createQuery(criteria);
		if (locked) {
			query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		}
		return query.uniqueResult();
	}

	@Override
	public TestResult getResult(Test test, Submission submission) {
		return getResult(test, submission, false);
	}

	@Override
	public TestResult getResultLocked(Test test, Submission submission) {
		return getResult(test, submission, true);
	}

	@Override
	public Map<Integer, Map<Integer, Boolean>> getResults(Task task) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<TestResult> criteria = builder.createQuery(TestResult.class);
		Root<TestResult> root = criteria.from(TestResult.class);
		criteria.select(root);
		criteria.where(builder.and(builder.equal(root.get(TestResult_.test).get(Test_.task), task)));
		criteria.orderBy(builder.asc(root.get(TestResult_.submission)), builder.asc(root.get(TestResult_.test)));
		Query<TestResult> query = session.createQuery(criteria);

		try (Stream<TestResult> stream = query.stream()) {
			return stream.collect(Collectors.groupingBy(tr -> tr.getSubmission().getSubmissionid(), Collectors.toMap(tr -> tr.getTest().getId(), tr -> tr.getPassedTest())));
		}
	}
}
