/*
 * Copyright 2009-2010, 2020-2024 Sven Strickroth <email@cs-ware.de>
 *
 * This file is part of the GATE.
 *
 * GATE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * GATE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GATE. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import java.util.List;

import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;

import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student_;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.persistence.datamodel.User_;

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
		return getSession().get(User.class, uid);
	}

	private User getUserByUsername(String username, boolean locked) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<User> criteria = builder.createQuery(User.class);
		Root<User> root = criteria.from(User.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get(User_.username), username));
		Query<User> query = session.createQuery(criteria);
		if (locked) {
			query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		}
		return query.uniqueResult();
	}

	@Override
	public User getUserByEmail(String email) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<User> criteria = builder.createQuery(User.class);
		Root<User> root = criteria.from(User.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get(User_.email), email));
		Query<User> query = session.createQuery(criteria);
		return query.uniqueResult();
	}

	@Override
	public User getUserByUsername(String username) {
		return getUserByUsername(username, false);
	}

	@Override
	public User createUser(String username, String email, String firstName, String lastName) {
		Session session = getSession();
		User user = getUserByUsername(username, true);
		if (user == null) {
			user = new User();
			user.setUsername(username);
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setEmail(email);
			session.persist(user);
			session.refresh(user); // make sure all fields are populated from DB
		}
		return user;
	}

	@Override
	public User createUser(String username, String email, String firstName, String lastName, int matrikelno) {
		Session session = getSession();
		User user = getUserByUsername(username, true);
		if (user == null) {
			Student student = new Student();
			student.setUsername(username);
			student.setEmail(email);
			student.setFirstName(firstName);
			student.setLastName(lastName);
			student.setMatrikelno(matrikelno);
			session.persist(student);
			session.refresh(student); // make sure all fields are populated from DB
			user = student;
		}
		return user;
	}

	@Override
	public void makeUserStudent(int uid, int matrikelno) {
		getSession().createNativeMutationQuery("update users set " + Student_.MATRIKELNO + " = :matrikelno where " + User_.UID + " = :uid").setParameter("matrikelno", matrikelno, StandardBasicTypes.INTEGER).setParameter("uid", uid, StandardBasicTypes.INTEGER).executeUpdate();
	}

	@Override
	public List<User> getUsers() {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<User> criteria = builder.createQuery(User.class);
		Root<User> root = criteria.from(User.class);
		criteria.select(root);
		criteria.orderBy(builder.asc(root.get(User_.lastName)), builder.asc(root.get(User_.firstName)));
		return session.createQuery(criteria).list();
	}

	@Override
	public List<User> getSuperUsers() {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<User> criteria = builder.createQuery(User.class);
		Root<User> root = criteria.from(User.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get(User_.superUser), true));
		criteria.orderBy(builder.asc(root.get(User_.lastName)), builder.asc(root.get(User_.firstName)));
		return session.createQuery(criteria).list();
	}
}
