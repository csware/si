/*
 * Copyright 2009-2012, 2017, 2020 Sven Strickroth <email@cs-ware.de>
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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.LectureDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
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
		return getSession().createCriteria(Lecture.class).addOrder(Order.desc("semester")).addOrder(Order.asc("name")).list();
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
		session.save(lecture);
		return lecture;
	}

	@Override
	public Lecture getLecture(int lectureId) {
		return (Lecture) getSession().get(Lecture.class, lectureId);
	}

	@Override
	public List<Lecture> getCurrentLecturesWithoutUser(User user) {
		Session session = getSession();
		// Criteria a = session.createCriteria(Lecture.class).createCriteria("participants").add(Restrictions.isNull("lecture")).createCriteria("user", Criteria.FULL_JOIN);
		return session.createCriteria(Lecture.class).add(Restrictions.ge("semester", Util.getCurrentSemester())).addOrder(Order.asc("name")).add(Restrictions.sqlRestriction("{alias}.id not in (select lectureid from participations part where part.uid=" + user.getUid() + ")")).list();
	}

	@Override
	public void deleteLecture(Lecture lecture) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		session.update(lecture);
		session.delete(lecture);
		tx.commit();
	}

	@Override
	public int getSumOfPoints(Lecture lecture) {
		Session session = getSession();
		Query query = session.createQuery("select sum(submission.points.points) from Submission submission inner join submission.task as task inner join task.taskGroup as taskgroup inner join taskgroup.lecture as lecture where lecture.id=:LECTURE and submission.points.pointStatus=" + PointStatus.ABGENOMMEN.ordinal());
		query.setEntity("LECTURE", lecture);
		Object result = query.uniqueResult();
		if (result == null) {
			return 0;
		}
		return ((Long) result).intValue();
	}

	@Override
	public int getStudentsCount(Lecture lecture) {
		Session session = getSession();
		Query query = session.createQuery("select count(*) from Participation participation inner join participation.lecture as lecture where lecture.id=:LECTURE and participation.role='NORMAL'");
		query.setEntity("LECTURE", lecture);
		Object result = query.uniqueResult();
		if (result == null) {
			return 0;
		}
		return ((Long) result).intValue();
	}
}
