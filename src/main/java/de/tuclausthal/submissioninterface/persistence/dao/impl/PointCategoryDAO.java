/*
 * Copyright 2010, 2020 Sven Strickroth <email@cs-ware.de>
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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.PointCategoryDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

/**
 * Data Access Object implementation for the PointCategoryDAOIf
 * @author Sven Strickroth
 */
public class PointCategoryDAO extends AbstractDAO implements PointCategoryDAOIf {
	public PointCategoryDAO(Session session) {
		super(session);
	}

	@Override
	public void deletePointCategory(PointCategory pointCategory) {
		Session session = getSession();
		session.update(pointCategory);
		session.delete(pointCategory);
	}

	@Override
	public PointCategory newPointCategory(Task task, int points, String description, boolean optional) {
		Session session = getSession();
		PointCategory pointCategory = new PointCategory(task, points, description, optional);
		session.save(pointCategory);
		return pointCategory;
	}

	@Override
	public PointCategory getPointCategory(int id) {
		return getSession().get(PointCategory.class, id);
	}

	@Override
	public int countPoints(Task task) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Integer> criteria = builder.createQuery(Integer.class);
		Root<PointCategory> root = criteria.from(PointCategory.class);
		criteria.select(builder.sum(root.get(PointCategory_.points)));
		criteria.where(builder.and(builder.equal(root.get(PointCategory_.task), task), builder.equal(root.get(PointCategory_.optional), false)));
		Integer result = session.createQuery(criteria).uniqueResult();
		if (result == null) {
			return 0;
		}
		return result.intValue();
	}
}
