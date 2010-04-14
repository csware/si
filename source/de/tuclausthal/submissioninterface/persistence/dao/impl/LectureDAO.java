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

import java.util.LinkedList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.LectureDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Data Access Object implementation for the LectureDAOIf
 * @author Sven Strickroth
 */
public class LectureDAO extends AbstractDAO  implements LectureDAOIf {
	public LectureDAO(Session session) {
		super(session);
	}

	@Override
	public List<Lecture> getLectures() {
		return (List<Lecture>) getSession().createCriteria(Lecture.class).addOrder(Order.desc("semester")).addOrder(Order.asc("name")).list();
	}

	@Override
	public Lecture newLecture(String name) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		Lecture lecture = new Lecture();
		lecture.setName(name);
		lecture.setSemester(Util.getCurrentSemester());
		session.save(lecture);
		tx.commit();
		return lecture;
	}

	@Override
	public Lecture getLecture(int lectureId) {
		return (Lecture) getSession().get(Lecture.class, lectureId);
	}

	@Override
	public List<Lecture> getCurrentLecturesWithoutUser(User user) {
		// TODO: optimization possible here ;)
		Session session = getSession();
		List<Lecture> lectures = new LinkedList<Lecture>();
		for (Lecture lecture : (List<Lecture>) session.createCriteria(Lecture.class).add(Restrictions.eq("semester", Util.getCurrentSemester())).addOrder(Order.asc("name")).list()) {
			boolean found = false;
			for (Participation participation : lecture.getParticipants()) {
				if (participation.getUser().getUid() == user.getUid()) {
					found = true;
					break;
				}
			}
			if (found == false) {
				lectures.add(lecture);
			}
		}
		// Criteria a = session.createCriteria(Lecture.class).createCriteria("participants").add(Restrictions.isNull("lecture")).createCriteria("user", Criteria.FULL_JOIN);
		return lectures;
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
	public int getAveragePoints(Lecture lecture) {
		Session session = getSession();
		Query query = session.createQuery("select avg(submission.points.points) from Submission submission inner join submission.task as task inner join task.lecture as lecture where lecture.id=:LECTURE and submission.points.pointsOk=true");
		query.setEntity("LECTURE", lecture);
		Object result = query.uniqueResult();
		if (result == null) {
			return 0;
		} else {
			return ((Double) result).intValue();
		}
	}
}
