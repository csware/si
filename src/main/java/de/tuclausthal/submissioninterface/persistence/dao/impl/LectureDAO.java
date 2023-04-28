/*
 * Copyright 2009-2012, 2017, 2020-2023 Sven Strickroth <email@cs-ware.de>
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

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.LectureDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation_;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Data Access Object implementation for the LectureDAOIf
 * @author Sven Strickroth
 */
public class LectureDAO extends AbstractDAO implements LectureDAOIf {
	public LectureDAO(Session session) {
		super(session);
	}

	@Override
	public List<Lecture> getLectures() {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Lecture> criteria = builder.createQuery(Lecture.class);
		Root<Lecture> root = criteria.from(Lecture.class);
		criteria.select(root);
		criteria.orderBy(builder.desc(root.get(Lecture_.semester)), builder.asc(root.get(Lecture_.name)));
		return session.createQuery(criteria).list();
	}

	@Override
	public Lecture newLecture(String name, boolean requiresAbhnahme, boolean groupWiseGrading) {
		Session session = getSession();
		Lecture lecture = new Lecture();
		lecture.setName(name);
		lecture.setSemester(Util.getCurrentSemester());
		lecture.setRequiresAbhnahme(requiresAbhnahme);
		if (groupWiseGrading) {
			lecture.setGradingMethod("groupWise");
		} else {
			lecture.setGradingMethod("taskWise");
		}
		session.persist(lecture);
		return lecture;
	}

	@Override
	public Lecture getLecture(int lectureId) {
		return getSession().get(Lecture.class, lectureId);
	}

	@Override
	public List<Lecture> getCurrentLecturesWithoutUser(User user) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Lecture> criteria = builder.createQuery(Lecture.class);
		Root<Lecture> root = criteria.from(Lecture.class);
		criteria.select(root);
		criteria.orderBy(builder.asc(root.get(Lecture_.name)));

		Subquery<Lecture> countSubQuery = criteria.subquery(Lecture.class);
		Root<Participation> lecturesTakingPartIn = countSubQuery.from(Participation.class);
		countSubQuery.select(lecturesTakingPartIn.get(Participation_.lecture));
		countSubQuery.where(builder.equal(lecturesTakingPartIn.get(Participation_.user), user));

		criteria.where(builder.and(builder.equal(root.get(Lecture_.semester), Util.getCurrentSemester()), builder.equal(root.get(Lecture_.allowSelfSubscribe), true), builder.not(root.in(countSubQuery))));

		return session.createQuery(criteria).list();
	}

	@Override
	public void deleteLecture(Lecture lecture) {
		Session session = getSession();
		session.remove(lecture);
	}
}
