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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.Util;

@GATEDBTest
class LectureDAOIfTest extends BasicTest {

	@Test
	void testGetLectures() {
		assertEquals(2, DAOFactory.LectureDAOIf(session).getLectures().size());
	}

	@Test
	void testGetCurrentLecturesWithoutUser() {
		Util.CLOCK = Clock.fixed(Instant.parse("2020-12-21T12:00:00Z"), ZoneOffset.UTC);

		User user1 = DAOFactory.UserDAOIf(session).getUser(1);
		assertEquals(0, DAOFactory.LectureDAOIf(session).getCurrentLecturesWithoutUser(user1).size());

		User user2 = DAOFactory.UserDAOIf(session).getUser(2);
		assertEquals(2, DAOFactory.LectureDAOIf(session).getCurrentLecturesWithoutUser(user2).size());

		User user9 = DAOFactory.UserDAOIf(session).getUser(8);
		assertEquals(0, DAOFactory.LectureDAOIf(session).getCurrentLecturesWithoutUser(user9).size());

		Util.CLOCK = Clock.systemDefaultZone();
	}
}
