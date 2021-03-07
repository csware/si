/*
 * Copyright 2011, 2017, 2020 Sven Strickroth <email@cs-ware.de>
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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.LockOptions;
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
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Result> criteria = builder.createQuery(Result.class);
		Root<Result> root = criteria.from(Result.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get(Result_.submission), submission));
		criteria.orderBy(builder.asc(root.get(Result_.resultid)));

		List<String> results = new ArrayList<>();
		for (Result result : session.createQuery(criteria).list()) {
			results.add(result.getResult());
		}
		return results;
	}

	@Override
	public void createResults(Submission submission, List<String> results) {
		Session session = getSession();
		session.buildLockRequest(LockOptions.UPGRADE).lock(submission);

		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaDelete<Result> criteria = builder.createCriteriaDelete(Result.class);
		Root<Result> root = criteria.from(Result.class);
		criteria.where(builder.equal(root.get(Result_.submission), submission));
		session.createQuery(criteria).executeUpdate();

		for (String stringResult : results) {
			session.save(new Result(submission, stringResult));
		}
	}
}
