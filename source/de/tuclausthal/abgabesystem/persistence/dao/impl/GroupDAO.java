package de.tuclausthal.abgabesystem.persistence.dao.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.persistence.dao.GroupDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Group;
import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;

/**
 * Data Access Object implementation for the GroupDAOIf
 * @author Sven Strickroth
 */
public class GroupDAO implements GroupDAOIf {
	@Override
	public Group createGroup(Lecture lecture, String name) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		Group group = new Group();
		group.setName(name);
		group.setLecture(lecture);
		session.save(group);
		tx.commit();
		return group;
	}

	@Override
	public void deleteGroup(Group group) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		session.update(group);
		session.delete(group);
		tx.commit();
	}

	@Override
	public Group getGroup(int groupid) {
		return (Group) MainBetterNameHereRequired.getSession().get(Group.class, groupid);
	}

	@Override
	public void saveGroup(Group group) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		session.update(group);
		tx.commit();
	}
}
