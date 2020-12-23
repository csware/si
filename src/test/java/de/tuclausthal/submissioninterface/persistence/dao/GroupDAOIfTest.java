/*
 * Copyright 2020 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;

@GATEDBTest
public class GroupDAOIfTest extends BasicTest {
	@Test
	public void testGetJoinAbleGroups() {
		Lecture lecture = DAOFactory.LectureDAOIf(session).getLecture(1);
		assertEquals(2, DAOFactory.GroupDAOIf(session).getJoinAbleGroups(lecture, null).size());

		Group g1 = DAOFactory.GroupDAOIf(session).getGroup(1);
		assertEquals(2, DAOFactory.GroupDAOIf(session).getJoinAbleGroups(lecture, g1).size());

		Group g2 = DAOFactory.GroupDAOIf(session).getGroup(2);
		assertEquals(2, DAOFactory.GroupDAOIf(session).getJoinAbleGroups(lecture, g2).size());

		Group g3 = DAOFactory.GroupDAOIf(session).getGroup(3);
		assertEquals(1, DAOFactory.GroupDAOIf(session).getJoinAbleGroups(lecture, g3).size());
	}
}
