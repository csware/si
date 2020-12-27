/*
 * Copyright 2009-2010, 2017, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.SimilarityDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

public class SimilarityDAO extends AbstractDAO implements SimilarityDAOIf {

	public SimilarityDAO(Session session) {
		super(session);
	}

	@Override
	public void addSimilarityResult(SimilarityTest similarityTest, Submission submissionOne, Submission submissionTwo, int percentage) {
		if (submissionOne != null && submissionTwo != null) {
			Session session = getSession();
			Transaction tx = session.beginTransaction();
			session.buildLockRequest(LockOptions.UPGRADE).lock(similarityTest);

			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaDelete<Similarity> criteria = builder.createCriteriaDelete(Similarity.class);
			Root<Similarity> root = criteria.from(Similarity.class);
			criteria.where(builder.and(builder.equal(root.get(Similarity_.similarityTest), similarityTest), builder.or(builder.and(builder.equal(root.get(Similarity_.submissionOne), submissionOne), builder.equal(root.get(Similarity_.submissionTwo), submissionTwo)), builder.and(builder.equal(root.get(Similarity_.submissionOne), submissionTwo), builder.equal(root.get(Similarity_.submissionTwo), submissionOne)))));
			session.createQuery(criteria).executeUpdate();

			Similarity simularity;
			simularity = new Similarity(similarityTest, submissionOne, submissionTwo, percentage);
			session.save(simularity);
			simularity = new Similarity(similarityTest, submissionTwo, submissionOne, percentage);
			session.save(simularity);
			tx.commit();
		}
	}

	@Override
	public List<Similarity> getUsersWithMaxSimilarity(SimilarityTest similarityTest, Submission submission) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Similarity> criteria = builder.createQuery(Similarity.class);
		Root<Similarity> root = criteria.from(Similarity.class);
		criteria.select(root);

		Subquery<Integer> subQuery = criteria.subquery(Integer.class);
		Root<Similarity> groupMembersCount = subQuery.from(Similarity.class);
		subQuery.select(builder.max(groupMembersCount.get(Similarity_.percentage)));
		subQuery.where(builder.and(builder.equal(groupMembersCount.get(Similarity_.similarityTest), root.get(Similarity_.similarityTest)), builder.equal(groupMembersCount.get(Similarity_.submissionOne), root.get(Similarity_.submissionOne))));

		criteria.where(builder.and(builder.equal(root.get(Similarity_.submissionOne), submission), builder.equal(root.get(Similarity_.similarityTest), similarityTest), builder.equal(root.get(Similarity_.percentage), subQuery)));
		return session.createQuery(criteria).list();
	}

	@Override
	public List<Similarity> getUsersWithSimilarity(SimilarityTest similarityTest, Submission submission) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Similarity> criteria = builder.createQuery(Similarity.class);
		Root<Similarity> root = criteria.from(Similarity.class);
		criteria.select(root);
		criteria.where(builder.and(builder.equal(root.get(Similarity_.submissionOne), submission), builder.equal(root.get(Similarity_.similarityTest), similarityTest)));
		criteria.orderBy(builder.desc(root.get(Similarity_.percentage)));
		return session.createQuery(criteria).list();
	}
}
