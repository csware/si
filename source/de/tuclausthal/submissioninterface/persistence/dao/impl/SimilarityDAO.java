/*
 * Copyright 2009-2010, 2017 Sven Strickroth <email@cs-ware.de>
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

import java.util.LinkedList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.SimilarityDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

public class SimilarityDAO extends AbstractDAO implements SimilarityDAOIf {

	public SimilarityDAO(Session session) {
		super(session);
	}

	@Override
	public void addSimilarityResult(SimilarityTest similarityTest, Submission submissionOne, Submission submissionTwo, int percentage) {
		// TODO: check in plaggie that only submissiondirectories are considered
		if (submissionOne != null && submissionTwo != null) {
			Session session = getSession();
			Transaction tx = session.beginTransaction();
			for (Similarity similarity : (List<Similarity>) session.createCriteria(Similarity.class).add(Restrictions.eq("similarityTest", similarityTest)).add(Restrictions.or(Restrictions.and(Restrictions.eq("submissionOne", submissionOne), Restrictions.eq("submissionTwo", submissionTwo)), Restrictions.and(Restrictions.eq("submissionOne", submissionTwo), Restrictions.eq("submissionTwo", submissionOne)))).list()) {
				session.delete(similarity);
			}
			Similarity simularity;
			simularity = new Similarity(similarityTest, submissionOne, submissionTwo, percentage);
			session.save(simularity);
			simularity = new Similarity(similarityTest, submissionTwo, submissionOne, percentage);
			session.save(simularity);
			tx.commit();
		}
	}

	@Override
	public int getMaxSimilarity(SimilarityTest similarityTest, Submission submission) {
		Session session = getSession();
		Query query = session.createQuery("select max(similarity.percentage) from Similarity similarity inner join similarity.similarityTest as similaritytest where similarity.submissionOne=:SUBMISSION and similaritytest.similarityTestId=:SIMID group by similarity.submissionOne");
		query.setEntity("SIMID", similarityTest);
		query.setEntity("SUBMISSION", submission);
		Object result = query.uniqueResult();
		if (result == null) {
			return 0;
		} else {
			return (Integer) result;
		}
	}

	@Override
	public List<Similarity> getUsersWithMaxSimilarity(SimilarityTest similarityTest, Submission submission) {
		int maxSimilarity = getMaxSimilarity(similarityTest, submission);
		if (maxSimilarity == 0) {
			return new LinkedList<>();
		} else {
			return getSession().createCriteria(Similarity.class).add(Restrictions.eq("submissionOne", submission)).add(Restrictions.eq("similarityTest", similarityTest)).add(Restrictions.eq("percentage", maxSimilarity)).list();
		}
	}

	@Override
	public List<Similarity> getUsersWithSimilarity(SimilarityTest similarityTest, Submission submission) {
		return getSession().createCriteria(Similarity.class).add(Restrictions.eq("submissionOne", submission)).add(Restrictions.eq("similarityTest", similarityTest)).addOrder(Order.desc("percentage")).list();
	}
}
