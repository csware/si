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

import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

/**
 * Data Access Object implementation for the UserDAOIf
 * @author Sven Strickroth
 */
public class UserDAO extends AbstractDAO implements UserDAOIf {

	public UserDAO(Session session) {
		super(session);
	}

	@Override
	public User getUser(int uid) {
		return (User) getSession().get(User.class, uid);
	}

	@Override
	public User getUser(String email) {
		return (User) getSession().createCriteria(User.class).add(Restrictions.eq("email", email)).setMaxResults(1).uniqueResult();
	}

	@Override
	public User createUser(String email, String firstName, String lastName) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		User user = (User) session.createCriteria(User.class).add(Restrictions.eq("email", email)).setLockMode(LockMode.UPGRADE).setMaxResults(1).uniqueResult();
		if (user == null) {
			user = new User();
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setEmail(email);
			session.save(user);
		}
		tx.commit();
		return user;
	}

	@Override
	public User createUser(String email, String firstName, String lastName, int matrikelno) {
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		User user = (User) session.createCriteria(User.class).add(Restrictions.eq("email", email)).setLockMode(LockMode.UPGRADE).setMaxResults(1).uniqueResult();
		if (user == null) {
			Student student = new Student();
			student.setEmail(email);
			student.setFirstName(firstName);
			student.setLastName(lastName);
			student.setMatrikelno(matrikelno);
			session.save(student);
			user = student;
		}
		tx.commit();
		return user;
	}

	@Override
	public List<User> getUsers() {
		return (List<User>) getSession().createCriteria(User.class).addOrder(Order.asc("lastName")).addOrder(Order.asc("firstName")).list();
	}

	@Override
	public List<User> getSuperUsers() {
		return (List<User>) getSession().createCriteria(User.class).add(Restrictions.eq("superUser", true)).addOrder(Order.asc("lastName")).addOrder(Order.asc("firstName")).list();
	}

	@Override
	public void saveUser(User user) {
		Session session = getSession();
		session.save(user);
	}
}
