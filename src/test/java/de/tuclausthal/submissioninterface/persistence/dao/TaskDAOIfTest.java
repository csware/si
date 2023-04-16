/*
 * Copyright 2022-2023 Sven Strickroth <email@cs-ware.de>
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

@GATEDBTest
class TaskDAOIfTest extends BasicTest {
	@Test
	void testCreateDeleteTask() {
		Transaction tx = session.beginTransaction();
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(1);
		List<Task> tasks = DAOFactory.TaskDAOIf(session).getTasks(lecture, false);
		int maxId = tasks.stream().mapToInt(t -> t.getTaskid()).max().orElse(0);

		ZonedDateTime now = ZonedDateTime.of(2021, 10, 21, 12, 0, 0, 0, ZoneId.systemDefault());

		Task task = DAOFactory.TaskDAOIf(session).newTask("New task", 2, now, ZonedDateTime.now().plusHours(2), "Aufgabenstellung", lecture.getTaskGroups().get(0), null, 1, false, "", null, false);
		assertTrue(task.getTaskid() > maxId);
		assertFalse(tasks.contains(task));
		assertEquals(DAOFactory.TaskDAOIf(session).getTasks(lecture, false).size(), tasks.size() + 1);
		assertTrue(DAOFactory.TaskDAOIf(session).getTasks(lecture, false).contains(task));

		session.refresh(task);
		/* it is important that the conversion to the "right" timezome is done as early as possible, i.e. before it is accessed by getStart() etc.
		 * let's assume we're in CET and task.getStart() is 2021-10-21T12:00:00+02:00 (change to wintertime is on 2021-10-31), then plusDays() etc. will work as expected.
		 * if task.getStart() is in UTC (2021-10-21T10:00Z) time, plusDays(14) is called, and then converted the local timeZone, the time is wrong
		 * System.out.println(task.getStart().plusDays(14).withZoneSameInstant(ZoneId.systemDefault())); --> 2021-11-04T11:00+01:00[Europe/Berlin], should be 2021-11-04T12:00+01:00[Europe/Berlin]
		 * In Hibernate 6 the default changed (UTC is stored in the DB and read from it); hibernate.timezone.default_storage needs to be set to NORMALIZE to fix that
		 */
		assertEquals(now, task.getStart(), "Make sure we read the very same timezone as we wrote to the DB");

		DAOFactory.TaskDAOIf(session).deleteTask(task);
		assertEquals(DAOFactory.TaskDAOIf(session).getTasks(lecture, false).size(), tasks.size());
		assertFalse(DAOFactory.TaskDAOIf(session).getTasks(lecture, false).contains(task));

		tx.rollback();
	}

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
