package de.tuclausthal.abgabesystem.persistence.dao.impl;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.persistence.dao.LectureDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;
import de.tuclausthal.abgabesystem.util.Util;

/**
 * Data Access Object implementation for the LectureDAOIf
 * @author Sven Strickroth
 */
public class LectureDAO implements LectureDAOIf {
	@Override
	public List<Lecture> getLectures() {
		return (List<Lecture>) MainBetterNameHereRequired.getSession().createCriteria(Lecture.class).addOrder(Order.desc("semester")).addOrder(Order.asc("name")).list();
	}

	@Override
	public Lecture newLecture(String name) {
		Session session = MainBetterNameHereRequired.getSession();
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
		return (Lecture) MainBetterNameHereRequired.getSession().get(Lecture.class, lectureId);
	}

	@Override
	public List<Lecture> getCurrentLucturesWithoutUser(User user) {
		// TODO: optimization possible here ;)
		Session session = MainBetterNameHereRequired.getSession();
		List<Lecture> lectures = new LinkedList<Lecture>();
		for (Lecture lecture : (List<Lecture>) session.createCriteria(Lecture.class).addOrder(Order.desc("semester")).addOrder(Order.asc("name")).list()) {
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
}
