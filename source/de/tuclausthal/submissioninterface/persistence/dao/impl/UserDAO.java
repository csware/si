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

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;

/**
 * Data Access Object implementation for the UserDAOIf
 * @author Sven Strickroth
 */
public class UserDAO implements UserDAOIf {
	@Override
	public User getUser(int uid) {
		return (User) HibernateSessionHelper.getSession().get(User.class, uid);
	}

	@Override
	public User getUser(String email) {
		return (User) HibernateSessionHelper.getSession().createCriteria(User.class).add(Restrictions.eq("email", email)).setMaxResults(1).uniqueResult();
	}

	@Override
	public User createUser(String email) {
		// Hibernate exception abfangen
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
		//MainBetterNameHereRequired.getSession().get(User.class, uid, LockMode.UPGRADE);
		User user = new User();
		user.setEmail(email);
		//user.setMatrikelno(matrikelno);
		session.save(user);
		tx.commit();
		return user;
	}

	@Override
	public List<User> getUsers() {
		return (List<User>) HibernateSessionHelper.getSession().createCriteria(User.class).addOrder(Order.asc("lastName")).addOrder(Order.asc("firstName")).list();
	}

	@Override
	public List<User> getSuperUsers() {
		return (List<User>) HibernateSessionHelper.getSession().createCriteria(User.class).add(Restrictions.eq("superUser", true)).addOrder(Order.asc("lastName")).addOrder(Order.asc("firstName")).list();
	}

	@Override
	public void saveUser(User user) {
		Session session = HibernateSessionHelper.getSession();
		Transaction tx = session.beginTransaction();
		session.save(user);
		tx.commit();
	}
}
