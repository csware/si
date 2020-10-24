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

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.PointGivenDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointGiven;
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

	@SuppressWarnings("unchecked")
	@Override
	public List<PointGiven> getPointsGivenOfSubmission(Submission submission) {
		return getSession().createCriteria(PointGiven.class).add(Restrictions.eq("submission", submission)).addOrder(Order.asc("category")).list();
	}

	@Override
	public void revokePointGiven(PointGiven pointGiven) {
		getSession().delete(pointGiven);
	}
}
