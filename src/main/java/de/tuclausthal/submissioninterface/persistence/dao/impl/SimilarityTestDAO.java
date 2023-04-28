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

import java.time.ZonedDateTime;

import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.SimilarityTestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task_;

public class SimilarityTestDAO extends AbstractDAO implements SimilarityTestDAOIf {

	public SimilarityTestDAO(Session session) {
		super(session);
	}

	@Override
	public SimilarityTest addSimilarityTest(Task task, String type, String basis, boolean normalizeCapitalization, String tabsSpacesNewlinesNormalization, int minimumDifferenceInPercent, String excludeFiles) {
		Session session = getSession();
		SimilarityTest similarityTest = new SimilarityTest(task, type, basis, normalizeCapitalization, tabsSpacesNewlinesNormalization, minimumDifferenceInPercent, excludeFiles);
		session.persist(similarityTest);
		return similarityTest;
	}

	@Override
	public void deleteSimilarityTest(SimilarityTest similarityTest) {
		Session session = getSession();
		session.remove(similarityTest);
	}

	@Override
	public void resetSimilarityTest(SimilarityTest similarityTest) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaDelete<Similarity> criteria = builder.createCriteriaDelete(Similarity.class);
		Root<Similarity> root = criteria.from(Similarity.class);
		criteria.where(builder.equal(root.get(Similarity_.similarityTest), similarityTest));
		session.createMutationQuery(criteria).executeUpdate();
	}

	@Override
	public SimilarityTest getSimilarityTest(int similarityTestId) {
		return getSession().get(SimilarityTest.class, similarityTestId);
	}

	@Override
	public SimilarityTest getSimilarityTestLocked(int similarityTestId) {
		return getSession().get(SimilarityTest.class, similarityTestId, LockOptions.UPGRADE);
	}

	@Override
	public SimilarityTest takeSimilarityTestTransacted() {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<SimilarityTest> criteria = builder.createQuery(SimilarityTest.class);
		Root<SimilarityTest> root = criteria.from(SimilarityTest.class);
		criteria.select(root);
		criteria.where(builder.and(builder.equal(root.get(SimilarityTest_.status), 1), builder.lessThan(root.join(SimilarityTest_.task).get(Task_.deadline), ZonedDateTime.now().minusMinutes(2))));
		SimilarityTest similarityTest = session.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).setMaxResults(1).uniqueResult();
		if (similarityTest != null) {
			similarityTest.setStatus(2);
		}
		tx.commit();
		return similarityTest;
	}

	@Override
	public void finish(SimilarityTest similarityTest) {
		SimilarityTest TheSimilarityTest = getSimilarityTestLocked(similarityTest.getSimilarityTestId());
		if (TheSimilarityTest != null) {
			TheSimilarityTest.setStatus(0);
		}
	}
}
