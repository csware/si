package de.tuclausthal.abgabesystem.persistence.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.persistence.dao.UserDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

/**
 * Data Access Object implementation for the UserDAOIf
 * @author Sven Strickroth
 */
public class UserDAO implements UserDAOIf {
	@Override
	public User getUser(int uid) {
		return (User) MainBetterNameHereRequired.getSession().get(User.class, uid);
	}

	@Override
	public User getUser(String email) {
		return (User) MainBetterNameHereRequired.getSession().createCriteria(User.class).add(Restrictions.eq("email", email)).setMaxResults(1).uniqueResult();
	}

	@Override
	public User createUser(String email) {
		// Hibernate exception abfangen
		Session session = MainBetterNameHereRequired.getSession();
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
		return (List<User>) MainBetterNameHereRequired.getSession().createCriteria(User.class).addOrder(Order.asc("lastName")).addOrder(Order.asc("firstName")).list();
	}
}
