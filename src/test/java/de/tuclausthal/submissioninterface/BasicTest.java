/*
 * Copyright 2020, 2023 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;

public class BasicTest {
	protected Session session;

	@BeforeEach
	public void openSession() {
		session = HibernateSessionHelper.getSessionFactory().openSession();
	}

	@AfterEach
	public void closeSession() {
		try {
			assertFalse(session.getTransaction().isActive(), "Transaction still open");
		} finally {
			session.close();
		}
	}
}
