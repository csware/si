/*
 * Copyright 2009-2010, 2017, 2020 Sven Strickroth <email@cs-ware.de>
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

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

/**
 * Data Access Object implementation for the ParticipationDAOIf
 * @author Sven Strickroth
 */
public class ParticipationDAO extends AbstractDAO implements ParticipationDAOIf {
	public ParticipationDAO(Session session) {
		super(session);
	}

	@Override
	public Participation createParticipation(User user, Lecture lecture, ParticipationRole type) {
		Session session = getSession();
		Participation participation = null;

		// try to load an existing participation and lock it (or lock it in advance, so that nobody can create it in another thread)
		participation = getParticipationLocked(user, lecture);
		if (participation == null) {
			participation = new Participation();
			participation.setUser(user);
			participation.setLecture(lecture);
		}
		participation.setRoleType(type);
		session.saveOrUpdate(participation);

		return participation;
	}

	@Override
	public void deleteParticipation(Participation participation) {
		Session session = getSession();
		session.update(participation);
		session.delete(participation);
	}

	@Override
	public Participation getParticipation(User user, Lecture lecture) {
		return (Participation) getSession().createCriteria(Participation.class).add(Restrictions.eq("lecture", lecture)).add(Restrictions.eq("user", user)).uniqueResult();
	}

	@Override
	public Participation getParticipationLocked(User user, Lecture lecture) {
		return (Participation) getSession().createCriteria(Participation.class).add(Restrictions.eq("lecture", lecture)).add(Restrictions.eq("user", user)).setLockMode(LockMode.UPGRADE).uniqueResult();
	}

	@Override
	public void deleteParticipation(User user, Lecture lecture) {
		Session session = getSession();
		Participation participation = (Participation) session.createCriteria(Participation.class).add(Restrictions.eq("lecture", lecture)).add(Restrictions.eq("user", user)).setLockMode(LockMode.UPGRADE).uniqueResult();
		if (participation != null) {
			session.delete(participation);
		}
	}

	@Override
	public List<Participation> getParticipationsWithoutGroup(Lecture lecture) {
		return getSession().createCriteria(Participation.class).add(Restrictions.eq("lecture", lecture)).add(Restrictions.isNull("group")).createCriteria("user").addOrder(Order.asc("lastName")).addOrder(Order.asc("firstName")).list();
	}

	@Override
	public List<Participation> getParticipationsOfGroup(Group group) {
		return getSession().createCriteria(Participation.class).add(Restrictions.eq("group", group)).createCriteria("user").addOrder(Order.asc("lastName")).addOrder(Order.asc("firstName")).list();
	}

	@Override
	public Participation getParticipation(int participationid) {
		return (Participation) getSession().get(Participation.class, participationid);
	}

	@Override
	public Participation getParticipationLocked(int participationid) {
		return (Participation) getSession().get(Participation.class, participationid, LockMode.UPGRADE);
	}

	@Override
	public void saveParticipation(Participation participation) {
		Session session = getSession();
		session.save(participation);
	}

	@Override
	public List<Participation> getParticipationsWithNoSubmissionToTaskOrdered(Task task) {
		return getSession().createCriteria(Participation.class).add(Restrictions.eq("lecture", task.getTaskGroup().getLecture())).add(Restrictions.sqlRestriction("{alias}.id not in (SELECT submitters_id FROM submissions, submissions_participations where submissions.submissionid=submissions_participations.submissions_submissionid and taskid=" + task.getTaskid() + ")")).createCriteria("user").addOrder(Order.asc("lastName")).addOrder(Order.asc("firstName")).list();
	}

	@Override
	public List<Participation> getMarkersAvailableParticipations(Group group) {
		if (group.getTutors().size() > 0) {
			Integer[] ids = new Integer[group.getTutors().size()];
			int i = 0;
			for (Participation participation : group.getTutors()) {
				ids[i++] = participation.getId();
			}
			return getSession().createCriteria(Participation.class).add(Restrictions.eq("lecture", group.getLecture())).add(Restrictions.or(Restrictions.eq("role", ParticipationRole.TUTOR.toString()), Restrictions.eq("role", ParticipationRole.ADVISOR.toString()))).add(Restrictions.not(Restrictions.in("id", ids))).createCriteria("user").addOrder(Order.asc("lastName")).addOrder(Order.asc("firstName")).list();
		}
		return getSession().createCriteria(Participation.class).add(Restrictions.eq("lecture", group.getLecture())).add(Restrictions.or(Restrictions.eq("role", ParticipationRole.TUTOR.toString()), Restrictions.eq("role", ParticipationRole.ADVISOR.toString()))).createCriteria("user").addOrder(Order.asc("lastName")).addOrder(Order.asc("firstName")).list();
	}

	@Override
	public List<Participation> getLectureParticipations(Lecture lecture) {
		return (List<Participation>) getSession().createCriteria(Participation.class).add(Restrictions.eq("lecture", lecture)).createAlias("user", "user").addOrder(Order.asc("user.lastName")).addOrder(Order.asc("user.firstName")).list();
	}
}
