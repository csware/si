/*
 * Copyright 2011, 2017, 2020-2024 Sven Strickroth <email@cs-ware.de>
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

import java.util.Collections;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.LockMode;
import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.ResultDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Result;
import de.tuclausthal.submissioninterface.persistence.datamodel.Result_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

/**
 * Data Access Object implementation for the ResultDAOIf
 * @author Sven Strickroth
 */
public class ResultDAO extends AbstractDAO implements ResultDAOIf {
	public ResultDAO(Session session) {
		super(session);
	}

	@Override
	public List<String> getResultsForSubmission(Submission submission) {
		if (submission == null) {
			return Collections.emptyList();
		}
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<String> criteria = builder.createQuery(String.class);
		Root<Result> root = criteria.from(Result.class);
		criteria.select(root.get(Result_.result));
		criteria.where(builder.equal(root.get(Result_.submission), submission));
		criteria.orderBy(builder.asc(root.get(Result_.resultid)));
		return session.createQuery(criteria).list();
	}

	@Override
	public void createResults(Submission submission, List<String> results) {
		Session session = getSession();
		session.lock(submission, LockMode.PESSIMISTIC_WRITE);

		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Result> criteria = builder.createQuery(Result.class);
		Root<Result> root = criteria.from(Result.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get(Result_.submission), submission));
		// not nice, but issuing a delete query with a where clause might cause deadlocks and items per submission is expected to be low
		for (Result result : session.createQuery(criteria).list()) {
			session.remove(result);
		}

		for (String stringResult : results) {
			session.persist(new Result(submission, stringResult));
		}
	}
}
