/*
 * Copyright 2009-2010, 2017, 2020-2022 Sven Strickroth <email@cs-ware.de>
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

import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.query.Query;

import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.persistence.datamodel.User_;

/**
 * Data Access Object implementation for the ParticipationDAOIf
 * @author Sven Strickroth
 */
public class ParticipationDAO extends AbstractDAO implements ParticipationDAOIf {
	public ParticipationDAO(Session session) {
		super(session);
	}

	@Override
	public boolean createParticipation(User user, Lecture lecture, ParticipationRole type) {
		Session session = getSession();
		Participation participation = null;
		session.lock(user, LockModeType.PESSIMISTIC_WRITE);
		// try to load an existing participation, this is thread-safe as the user is locked first
		participation = getParticipation(user, lecture);
		if (participation == null) {
			participation = new Participation();
			participation.setUser(user);
			participation.setLecture(lecture);
			participation.setRoleType(type);
			session.save(participation);
			return true;
		}
		return false;
	}

	@Override
	public void deleteParticipation(Participation participation) {
		Session session = getSession();
		session.update(participation);
		session.delete(participation);
	}

	private Participation getParticipation(User user, Lecture lecture, boolean locked) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Participation> criteria = builder.createQuery(Participation.class);
		Root<Participation> root = criteria.from(Participation.class);
		criteria.select(root);
		criteria.where(builder.and(builder.equal(root.get(Participation_.user), user), builder.equal(root.get(Participation_.lecture), lecture)));
		criteria.orderBy(builder.asc(root.get(Participation_.id)));
		Query<Participation> query = session.createQuery(criteria);
		if (locked) {
			query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		}
		return query.uniqueResult();
	}

	@Override
	public Participation getParticipation(User user, Lecture lecture) {
		return getParticipation(user, lecture, false);
	}

	@Override
	public Participation getParticipationLocked(User user, Lecture lecture) {
		return getParticipation(user, lecture, true);
	}

	@Override
	public void deleteParticipation(User user, Lecture lecture) {
		Session session = getSession();
		Participation participation = getParticipationLocked(user, lecture);
		if (participation != null) {
			session.delete(participation);
		}
	}

	@Override
	public List<Participation> getParticipationsWithoutGroup(Lecture lecture) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Participation> criteria = builder.createQuery(Participation.class);
		Root<Participation> root = criteria.from(Participation.class);
		criteria.select(root);
		@SuppressWarnings("unchecked")
		Join<Participation, User> userJoin = (Join<Participation, User>) root.fetch(Participation_.user);
		criteria.where(builder.and(builder.isNull(root.get(Participation_.group)), builder.equal(root.get(Participation_.lecture), lecture)));
		criteria.orderBy(builder.asc(userJoin.get(User_.lastName)), builder.asc(userJoin.get(User_.firstName)));
		return session.createQuery(criteria).list();
	}

	@Override
	public List<Participation> getParticipationsOfGroup(Group group) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Participation> criteria = builder.createQuery(Participation.class);
		Root<Participation> root = criteria.from(Participation.class);
		criteria.select(root);
		@SuppressWarnings("unchecked")
		Join<Participation, User> userJoin = (Join<Participation, User>) root.fetch(Participation_.user);
		criteria.where(builder.equal(root.get(Participation_.group), group));
		criteria.orderBy(builder.asc(userJoin.get(User_.lastName)), builder.asc(userJoin.get(User_.firstName)));
		return session.createQuery(criteria).list();
	}

	@Override
	public Participation getParticipation(int participationid) {
		return getSession().get(Participation.class, participationid);
	}

	@Override
	public Participation getParticipationLocked(int participationid) {
		return getSession().get(Participation.class, participationid, LockOptions.UPGRADE);
	}

	@Override
	public void saveParticipation(Participation participation) {
		Session session = getSession();
		session.save(participation);
	}

	@Override
	public List<Participation> getParticipationsWithNoSubmissionToTaskOrdered(Task task) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Participation> criteria = builder.createQuery(Participation.class);
		Root<Participation> root = criteria.from(Participation.class);
		criteria.select(root);
		@SuppressWarnings("unchecked")
		Join<Participation, User> userJoin = (Join<Participation, User>) root.fetch(Participation_.user);

		Subquery<Participation> subQuery = criteria.subquery(Participation.class);
		Root<Submission> lecturesTakingPartIn = subQuery.from(Submission.class);
		subQuery.select(lecturesTakingPartIn.join(Submission_.submitters));
		subQuery.where(builder.equal(lecturesTakingPartIn.get(Submission_.task), task));

		criteria.where(builder.and(builder.equal(root.get(Participation_.lecture), task.getTaskGroup().getLecture()), builder.not(root.get(Participation_.id).in(subQuery))));
		criteria.orderBy(builder.asc(userJoin.get(User_.lastName)), builder.asc(userJoin.get(User_.firstName)));
		return session.createQuery(criteria).list();
	}

	@Override
	public List<Participation> getMarkersAvailableParticipations(Group group) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Participation> criteria = builder.createQuery(Participation.class);
		Root<Participation> root = criteria.from(Participation.class);
		criteria.select(root);
		@SuppressWarnings("unchecked")
		Join<Participation, User> userJoin = (Join<Participation, User>) root.fetch(Participation_.user);
		Predicate where = builder.and(builder.equal(root.get(Participation_.lecture), group.getLecture()), root.get(Participation_.role).in(ParticipationRole.TUTOR.toString(), ParticipationRole.ADVISOR.toString()));
		if (!group.getTutors().isEmpty()) {
			where = builder.and(where, builder.not(root.in(group.getTutors())));
		}
		criteria.where(where);
		criteria.orderBy(builder.asc(userJoin.get(User_.lastName)), builder.asc(userJoin.get(User_.firstName)));
		return session.createQuery(criteria).list();
	}

	@Override
	public List<Participation> getLectureParticipationsOrderedByName(Lecture lecture) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Participation> criteria = builder.createQuery(Participation.class);
		Root<Participation> root = criteria.from(Participation.class);
		criteria.select(root);
		@SuppressWarnings("unchecked")
		Join<Participation, User> userJoin = (Join<Participation, User>) root.fetch(Participation_.user);
		root.fetch(Participation_.group, JoinType.LEFT);
		criteria.where(builder.equal(root.get(Participation_.lecture), lecture));
		criteria.orderBy(builder.asc(userJoin.get(User_.lastName)), builder.asc(userJoin.get(User_.firstName)));
		return session.createQuery(criteria).list();
	}
}
