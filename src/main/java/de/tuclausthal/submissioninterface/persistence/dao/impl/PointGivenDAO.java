/*
 * Copyright 2009-2010, 2020 Sven Strickroth <email@cs-ware.de>
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
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.PointGivenDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointGiven;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointGiven_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

/**
 * Data Access Object implementation for the PointsDAOIf
 * @author Sven Strickroth
 */
public class PointGivenDAO extends AbstractDAO implements PointGivenDAOIf {
	public PointGivenDAO(Session session) {
		super(session);
	}

	@Override
	public PointGiven givePoint(int issuedPoints, Submission submission, PointCategory category) {
		Session session = getSession();
		PointGiven pointGiven = new PointGiven(issuedPoints, submission, category);
		session.save(pointGiven);
		return pointGiven;
	}

	@Override
	public List<PointGiven> getPointsGivenOfSubmission(Submission submission) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<PointGiven> criteria = builder.createQuery(PointGiven.class);
		Root<PointGiven> root = criteria.from(PointGiven.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get(PointGiven_.submission), submission));
		criteria.orderBy(builder.asc(root.get(PointGiven_.category)));
		return session.createQuery(criteria).list();
	}

	@Override
	public void revokePointGiven(PointGiven pointGiven) {
		getSession().delete(pointGiven);
	}
}
