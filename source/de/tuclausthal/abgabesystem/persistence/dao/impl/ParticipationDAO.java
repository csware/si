package de.tuclausthal.abgabesystem.persistence.dao.impl;

import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.ParticipationRole;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public class ParticipationDAO implements ParticipationDAOIf {

	@Override
	public Participation createParticipation(User user, Lecture lecture, ParticipationRole type) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		Participation participation = null;
		// TODO check for race condition with Lecture and/or User

		// try to load an existing participation and lock it (or lock it in advance, so that nobody can create it in another thread)
		participation = (Participation) session.createCriteria(Participation.class).add(Restrictions.eq("lecture", lecture)).add(Restrictions.eq("user", user)).setLockMode(LockMode.UPGRADE).uniqueResult();
		if (participation == null) {
			participation = new Participation();
			participation.setUser(user);
			participation.setLecture(lecture);
		}
		participation.setRoleType(type);
		session.saveOrUpdate(participation);
		tx.commit();

		return participation;
	}

	@Override
	public void deleteParticipation(Participation participation) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		session.update(participation);
		session.delete(participation);
		tx.commit();
	}

	@Override
	public Participation getParticipation(User user, Lecture lecture) {
		return (Participation) MainBetterNameHereRequired.getSession().createCriteria(Participation.class).add(Restrictions.eq("lecture", lecture)).add(Restrictions.eq("user", user)).uniqueResult();
	}

	@Override
	public void deleteParticipation(User user, Lecture lecture) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		Participation participation = (Participation) session.createCriteria(Participation.class).add(Restrictions.eq("lecture", lecture)).add(Restrictions.eq("user", user)).setLockMode(LockMode.UPGRADE).uniqueResult();
		if (participation != null) {
			session.delete(participation);
		}
		tx.commit();
	}

	@Override
	public List<Participation> getParticipationsWithoutGroup(Lecture lecture) {
		return (List<Participation>) MainBetterNameHereRequired.getSession().createCriteria(Participation.class).add(Restrictions.eq("lecture", lecture)).add(Restrictions.isNull("group")).list();
	}

	@Override
	public Participation getParticipation(int participationid) {
		return (Participation) MainBetterNameHereRequired.getSession().get(Participation.class, participationid);
	}

	@Override
	public void saveParticipation(Participation participation) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		session.save(participation);
		tx.commit();
	}
}
