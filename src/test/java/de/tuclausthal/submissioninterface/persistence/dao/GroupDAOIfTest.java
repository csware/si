/*
 * Copyright 2020-2023 Sven Strickroth <email@cs-ware.de>
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;

@GATEDBTest
public class GroupDAOIfTest extends BasicTest {
	@Test
	public void testCreateDeleteGroup() {
		Transaction tx = session.beginTransaction();
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(1);
		Group newGroup = DAOFactory.GroupDAOIf(session).createGroup(lecture, "New group", true, false, 5, false);

		Group g1 = DAOFactory.GroupDAOIf(session).getGroup(newGroup.getGid());
		assertEquals(newGroup, g1);

		session.refresh(g1);
		assertEquals(newGroup, g1);

		DAOFactory.GroupDAOIf(session).deleteGroup(newGroup);

		Group g2 = DAOFactory.GroupDAOIf(session).getGroup(newGroup.getGid());
		assertNull(g2);
		tx.rollback();
	}

	@Test
	public void testGetJoinAbleGroups() {
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(1);
		assertEquals(2, DAOFactory.GroupDAOIf(session).getJoinAbleGroups(lecture, null).size());

		Group g1 = DAOFactory.GroupDAOIf(session).getGroup(1);
		assertEquals(0, DAOFactory.GroupDAOIf(session).getJoinAbleGroups(lecture, g1).size());

		Group g2 = DAOFactory.GroupDAOIf(session).getGroup(2);
		assertEquals(2, DAOFactory.GroupDAOIf(session).getJoinAbleGroups(lecture, g2).size());

		Group g3 = DAOFactory.GroupDAOIf(session).getGroup(3);
		assertEquals(1, DAOFactory.GroupDAOIf(session).getJoinAbleGroups(lecture, g3).size());
	}

	@Test
	public void testGetGroupSizes() {
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(1);
		Map<Integer, Integer> groupSizes = DAOFactory.GroupDAOIf(session).getGroupSizes(List.copyOf(lecture.getGroups()), null);
		assertEquals(3, groupSizes.size());
		assertEquals(3, groupSizes.get(1));
		assertEquals(2, groupSizes.get(2));
		assertEquals(1, groupSizes.get(3));

		groupSizes = DAOFactory.GroupDAOIf(session).getGroupSizes(DAOFactory.GroupDAOIf(session).getJoinAbleGroups(lecture, null), null);
		assertEquals(1, groupSizes.size());
		assertEquals(1, groupSizes.get(3));

		Group g1 = DAOFactory.GroupDAOIf(session).getGroup(1);
		groupSizes = DAOFactory.GroupDAOIf(session).getGroupSizes(Collections.emptyList(), g1);
		assertEquals(1, groupSizes.size());
		assertEquals(3, groupSizes.get(1));

		groupSizes = DAOFactory.GroupDAOIf(session).getGroupSizes(DAOFactory.GroupDAOIf(session).getJoinAbleGroups(lecture, null), g1);
		assertEquals(2, groupSizes.size());
		assertEquals(3, groupSizes.get(1));
		assertEquals(1, groupSizes.get(3));

		assertEquals(0, DAOFactory.GroupDAOIf(session).getGroupSizes(Collections.emptyList(), null).size());
	}
}
