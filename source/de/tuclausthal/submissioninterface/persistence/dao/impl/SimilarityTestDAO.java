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

import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.SimilarityTestDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

public class SimilarityTestDAO  extends AbstractDAO implements SimilarityTestDAOIf {

	public SimilarityTestDAO(Session session) {
		super(session);
	}

	@Override
	public SimilarityTest addSimilarityTest(Task task, String type, String basis, boolean normalizeCapitalization, String tabsSpacesNewlinesNormalization, int minimumDifferenceInPercent, String excludeFiles) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		SimilarityTest similarityTest = new SimilarityTest(task, type, basis, normalizeCapitalization, tabsSpacesNewlinesNormalization, minimumDifferenceInPercent, excludeFiles);
		session.save(similarityTest);
		tx.commit();
		return similarityTest;
	}

	@Override
	public void deleteSimilarityTest(SimilarityTest similarityTest) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		session.update(similarityTest);
		session.delete(similarityTest);
		tx.commit();
	}

	@Override
	public void resetSimilarityTest(SimilarityTest similarityTest) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		Query query = session.createQuery("delete from Similarity similarity where similarity.similarityTest=:SIMTEST");
		query.setEntity("SIMTEST", similarityTest);
		query.executeUpdate();
		tx.commit();
	}

	@Override
	public SimilarityTest getSimilarityTest(int similarityTestId) {
		return (SimilarityTest) getSession().get(SimilarityTest.class, similarityTestId);
	}

	@Override
	public SimilarityTest takeSimilarityTest() {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		SimilarityTest similarityTest = (SimilarityTest) session.createCriteria(SimilarityTest.class).add(Restrictions.eq("needsToRun", true)).setLockMode(LockMode.UPGRADE).createCriteria("task").add(Restrictions.le("deadline", Util.correctTimezone(new Date()))).setMaxResults(1).uniqueResult();
		if (similarityTest != null) {
			similarityTest.setNeedsToRun(false);
			session.save(similarityTest);
		}
		tx.commit();
		return similarityTest;
	}
}
