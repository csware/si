/*
 * Copyright 2020, 2022 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

@GATEDBTest
class UserDAOIfTest extends BasicTest {
	@Test
	void testGetUser() {
		assertNotNull(DAOFactory.UserDAOIf(session).getUser(1));
		assertNotNull(DAOFactory.UserDAOIf(session).getUser(2));
		assertNotNull(DAOFactory.UserDAOIf(session).getUser(3));
		assertNotNull(DAOFactory.UserDAOIf(session).getUser(6));
		assertNull(DAOFactory.UserDAOIf(session).getUser(0));
		assertNull(DAOFactory.UserDAOIf(session).getUser(-1));
		assertNull(DAOFactory.UserDAOIf(session).getUser(42));
	}

	@Test
	void testGetUserByUsername() {
		User admin = DAOFactory.UserDAOIf(session).getUserByUsername("admin");
		assertNotNull(admin);
		assertEquals(1, admin.getUid());
		assertTrue(admin.isSuperUser());

		User user1 = DAOFactory.UserDAOIf(session).getUserByUsername("user1");
		assertNotNull(user1);
		assertEquals(3, user1.getUid());
		assertFalse(user1.isSuperUser());
		assertTrue(user1 instanceof Student);
		assertEquals(1, ((Student) user1).getMatrikelno());

		User user4 = DAOFactory.UserDAOIf(session).getUserByUsername("user4");
		assertNotNull(user4);
		assertEquals(6, user4.getUid());
		assertFalse(user4.isSuperUser());
		assertFalse(user4 instanceof Student);

		assertNull(DAOFactory.UserDAOIf(session).getUserByUsername("noexist"));
	}

	@Test
	void testGetUsers() {
		assertEquals(16, DAOFactory.UserDAOIf(session).getUsers().size());
	}

	@Test
	void testGetSuperUsers() {
		assertEquals(1, DAOFactory.UserDAOIf(session).getSuperUsers().size());
	}

	@Test
	void testGetUserByEmail() {
		assertEquals(1, DAOFactory.UserDAOIf(session).getUserByEmail("admin@localhost").getUid());
		assertEquals(1, DAOFactory.UserDAOIf(session).getUserByEmail("admin@LocalHost").getUid());
		assertNull(DAOFactory.UserDAOIf(session).getUserByEmail("gibbed@Net"));
	}

	@Test
	void testMakeUserStudent() {
		Transaction tx = session.beginTransaction();
		User user = DAOFactory.UserDAOIf(session).getUserByUsername("user2");
		assertFalse(user instanceof Student);
		session.detach(user); // force user to be reloaded
		DAOFactory.UserDAOIf(session).makeUserStudent(user.getUid(), 4566);
		user = DAOFactory.UserDAOIf(session).getUserByUsername("user2");
		assertTrue(user instanceof Student);
		assertEquals(4566, ((Student)user).getMatrikelno());
		tx.rollback();
	}

	@Test
	void testCreateUser() {
		Transaction tx = session.beginTransaction();
		User user = DAOFactory.UserDAOIf(session).createUser("ausername", "a@mail", "First", "Last");
		assertFalse(user instanceof Student);
		assertTrue(user.getUid() > 0);
		tx.rollback();
	}

	@Test
	void testCreateStudent() {
		Transaction tx = session.beginTransaction();
		User user = DAOFactory.UserDAOIf(session).createUser("ausername", "a@mail", "First", "Last", 4711);
		assertTrue(user.getUid() > 0);
		assertTrue(user instanceof Student);
		assertEquals(4711, ((Student)user).getMatrikelno());
		tx.rollback();
	}
}
