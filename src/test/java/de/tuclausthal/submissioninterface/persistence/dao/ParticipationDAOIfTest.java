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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

@GATEDBTest
class ParticipationDAOIfTest extends BasicTest {
	@Test
	void testGetParticipationsWithoutGroup4() {
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(1);
		assertEquals(4, DAOFactory.ParticipationDAOIf(session).getParticipationsWithoutGroup(lecture).size());
	}

	@Test
	void testGetParticipationsOfGroupEmpty() {
		Group group = DAOFactory.GroupDAOIf(session).getGroup(5);
		assertEquals(0, DAOFactory.ParticipationDAOIf(session).getParticipationsOfGroup(group).size());
	}

	@Test
	void testGetParticipationsOfGroupOne() {
		Group group = DAOFactory.GroupDAOIf(session).getGroup(3);
		assertEquals(1, DAOFactory.ParticipationDAOIf(session).getParticipationsOfGroup(group).size());
	}

	@Test
	void testGetParticipationsOfGroup() {
		Group group = DAOFactory.GroupDAOIf(session).getGroup(1);
		assertEquals(3, DAOFactory.ParticipationDAOIf(session).getParticipationsOfGroup(group).size());
	}

	@Test
	void testGetParticipationsWithNoSubmissionToTaskOrderedNooneSubmitted() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(4);
		List<Participation> list = DAOFactory.ParticipationDAOIf(session).getParticipationsWithNoSubmissionToTaskOrdered(task);
		assertEquals(10, list.size());
		assertEquals("Lastname1", list.get(0).getUser().getLastName());
		assertEquals("Lastname2", list.get(1).getUser().getLastName());
		assertEquals("Lastname3", list.get(2).getUser().getLastName());
		assertEquals("User", list.get(9).getUser().getLastName());
	}

	@Test
	void testGetParticipationsWithNoSubmissionToTaskOrdered() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(1);
		assertEquals(4, DAOFactory.ParticipationDAOIf(session).getParticipationsWithNoSubmissionToTaskOrdered(task).size());
	}

	@Test
	void testGetMarkersAvailableParticipationsÓne() {
		Group group = DAOFactory.GroupDAOIf(session).getGroup(1);
		assertEquals(1, DAOFactory.ParticipationDAOIf(session).getMarkersAvailableParticipations(group).size());
	}

	@Test
	void testGetMarkersAvailableParticipationsNone() {
		Group group = DAOFactory.GroupDAOIf(session).getGroup(2);
		assertEquals(0, DAOFactory.ParticipationDAOIf(session).getMarkersAvailableParticipations(group).size());
	}

	@Test
	void testGetMarkersAvailableParticipationsTwo() {
		Group group = DAOFactory.GroupDAOIf(session).getGroup(5);
		assertEquals(2, DAOFactory.ParticipationDAOIf(session).getMarkersAvailableParticipations(group).size());
	}

	@Test
	void testGetLectureParticipations() {
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(1);
		List<Participation> list = DAOFactory.ParticipationDAOIf(session).getLectureParticipationsOrderedByName(lecture);
		assertEquals(10, list.size());
		assertEquals("Lastname1", list.get(0).getUser().getLastName());
		assertEquals("Lastname2", list.get(1).getUser().getLastName());
		assertEquals("Lastname3", list.get(2).getUser().getLastName());
		assertEquals("User", list.get(9).getUser().getLastName());
	}

	@Test
	void testGetAndCreateAndDeleteParticipation() {
		Transaction tx = session.beginTransaction();
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(1);
		User user = DAOFactory.UserDAOIf(session).getUser(14);
		assertNull(DAOFactory.ParticipationDAOIf(session).getParticipation(user, lecture));
		assertEquals(true, DAOFactory.ParticipationDAOIf(session).createParticipation(user, lecture, ParticipationRole.NORMAL));
		assertNotNull(DAOFactory.ParticipationDAOIf(session).getParticipation(user, lecture));
		assertEquals(false, DAOFactory.ParticipationDAOIf(session).createParticipation(user, lecture, ParticipationRole.NORMAL));
		DAOFactory.ParticipationDAOIf(session).deleteParticipation(user, lecture);
		assertNull(DAOFactory.ParticipationDAOIf(session).getParticipation(user, lecture));

		assertEquals(true, DAOFactory.ParticipationDAOIf(session).createParticipation(user, lecture, ParticipationRole.NORMAL));
		assertNotNull(DAOFactory.ParticipationDAOIf(session).getParticipation(user, lecture));
		DAOFactory.ParticipationDAOIf(session).deleteParticipation(DAOFactory.ParticipationDAOIf(session).getParticipation(user, lecture));
		assertNull(DAOFactory.ParticipationDAOIf(session).getParticipation(user, lecture));
		tx.rollback();
	}
}
