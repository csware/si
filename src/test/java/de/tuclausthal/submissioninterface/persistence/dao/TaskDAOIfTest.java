/*
 * Copyright 2022 Sven Strickroth <email@cs-ware.de>
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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

@GATEDBTest
class TaskDAOIfTest extends BasicTest {

	@Test
	void testGetTasks() {
		assertEquals(4, DAOFactory.TaskDAOIf(session).getTasks(DAOFactory.LectureDAOIf(session).getLecture(1), false).size());
		assertEquals(4, DAOFactory.TaskDAOIf(session).getTasks(DAOFactory.LectureDAOIf(session).getLecture(1), true).size());

		session.beginTransaction();
		Task task = DAOFactory.TaskDAOIf(session).getTask(3);
		task.setStart(ZonedDateTime.now().plusHours(5));
		assertEquals(4, DAOFactory.TaskDAOIf(session).getTasks(DAOFactory.LectureDAOIf(session).getLecture(1), false).size());
		assertEquals(3, DAOFactory.TaskDAOIf(session).getTasks(DAOFactory.LectureDAOIf(session).getLecture(1), true).size());
		session.getTransaction().rollback();

		assertEquals(0, DAOFactory.TaskDAOIf(session).getTasks(DAOFactory.LectureDAOIf(session).getLecture(2), true).size());
	}

	@Test
	void testGetTask() {
		assertNull(DAOFactory.TaskDAOIf(session).getTask(-1));
		assertNull(DAOFactory.TaskDAOIf(session).getTask(10));
	}
}
