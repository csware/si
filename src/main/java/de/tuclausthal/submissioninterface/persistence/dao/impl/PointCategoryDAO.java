/*
 * Copyright 2010 Sven Strickroth <email@cs-ware.de>
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

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.PointCategoryDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
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
		Long ret = (Long) session.createCriteria(PointCategory.class).add(Restrictions.eq("task", task)).add(Restrictions.eq("optional", false)).setProjection(Projections.sum("points")).uniqueResult();
		if (ret == null) {
			return 0;
		}
		return ret.intValue();
	}
}
