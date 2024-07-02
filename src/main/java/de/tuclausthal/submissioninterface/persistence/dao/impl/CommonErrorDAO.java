/*
 * Copyright 2021-2024 Sven Strickroth <email@cs-ware.de>
 * Copyright 2021 Florian Holzinger <f.holzinger@campus.lmu.de>
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.query.Query;

import de.tuclausthal.submissioninterface.persistence.dao.CommonErrorDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommonError;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommonError_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult_;

public class CommonErrorDAO extends AbstractDAO implements CommonErrorDAOIf {

	public CommonErrorDAO(Session session) {
		super(session);
	}

	@Override
	public void reset(Test test) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaDelete<CommonError> criteria = builder.createCriteriaDelete(CommonError.class);
		Root<CommonError> root = criteria.from(CommonError.class);
		criteria.where(builder.equal(root.get(CommonError_.test), test));
		session.createMutationQuery(criteria).executeUpdate();
	}

	@Override
	public void reset(Task task) {
		Session session = getSession();
		for (Test test : task.getTests()) {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaDelete<CommonError> criteria = builder.createCriteriaDelete(CommonError.class);
			Root<CommonError> root = criteria.from(CommonError.class);
			criteria.where(builder.equal(root.get(CommonError_.test), test));
			session.createMutationQuery(criteria).executeUpdate();
		}
	}

	@Override
	public CommonError newCommonError(String title, String commonErrorName, TestResult testResult, CommonError.Type errorType) {
		Session session = getSession();

		CommonError commonError = new CommonError(title, commonErrorName, testResult.getTest());
		if (errorType != null) {
			commonError.setType(errorType.ordinal());
		}
		commonError.getTestResults().add(testResult);
		session.persist(commonError);
		return commonError;
	}

	@Override
	public CommonError getCommonError(int id) {
		return getSession().get(CommonError.class, id);
	}

	@Override
	public CommonError getCommonError(String title, Test test) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<CommonError> criteria = builder.createQuery(CommonError.class);
		Root<CommonError> root = criteria.from(CommonError.class);
		criteria.select(root).where(builder.and(builder.equal(root.get(CommonError_.test), test), builder.equal(root.get(CommonError_.title), title)));
		return session.createQuery(criteria).uniqueResult();
	}

	@Override
	public List<CommonError> getCommonErrors(Test test) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<CommonError> criteria = builder.createQuery(CommonError.class);
		Root<CommonError> root = criteria.from(CommonError.class);
		criteria.select(root).where(builder.equal(root.get(CommonError_.test), test));
		return session.createQuery(criteria).getResultList();
	}

	@Override
	public Map<Submission, List<CommonError>> getErrorsForSubmissions(Test test, List<Submission> submissions) {
		Session session = getSession();

		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteria = builder.createTupleQuery();
		Root<CommonError> root = criteria.from(CommonError.class);
		Join<CommonError, TestResult> join = root.join(CommonError_.testResults);
		criteria.select(builder.tuple(join.get(TestResult_.submission), root));
		criteria.where(builder.and(builder.equal(root.get(CommonError_.test), test), join.get(TestResult_.submission).in(submissions)));

		Query<Tuple> query = session.createQuery(criteria);
		try (Stream<Tuple> stream = query.stream()) {
			return stream.collect(Collectors.groupingBy(tupel -> tupel.get(0, Submission.class), Collectors.mapping(tupel -> tupel.get(1, CommonError.class), Collectors.toList())));
		}
	}
}
