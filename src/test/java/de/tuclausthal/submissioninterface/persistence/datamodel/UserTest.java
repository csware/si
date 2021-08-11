/*
 * Copyright 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.datamodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;

@GATEDBTest
class UserTest extends BasicTest {
	@Test
	void getParticipationsAdmin() {
		User user = DAOFactory.UserDAOIf(session).getUser(1);
		assertFalse(user instanceof Student);
		Set<Participation> lectureParcicipations = user.getLectureParticipant();
		assertEquals(3, lectureParcicipations.size());
		for (Participation participation : lectureParcicipations) {
			assertEquals(participation.getRoleType(), ParticipationRole.ADVISOR);
		}
	}

	@Test
	void getParticipationsNoParticipating() {
		User user = DAOFactory.UserDAOIf(session).getUser(2);
		Set<Participation> lectureParcicipations = user.getLectureParticipant();
		assertEquals(0, lectureParcicipations.size());
	}

	@Test
	void getParticipationsUser1() {
		User user = DAOFactory.UserDAOIf(session).getUser(3);
		assertTrue(user instanceof Student);
		Set<Participation> lectureParcicipations = user.getLectureParticipant();
		assertEquals(2, lectureParcicipations.size());
		boolean found1 = false;
		boolean found2 = false;
		for (Participation participation : lectureParcicipations) {
			assertEquals(participation.getRoleType(), ParticipationRole.NORMAL);
			if (participation.getLecture().getId() == 1) {
				found1 = true;
			}
			if (participation.getLecture().getId() == 2) {
				found2 = true;
			}
		}
		assertTrue(found1 && found2);
	}

	@Test
	void getParticipationsUser2() {
		User user = DAOFactory.UserDAOIf(session).getUser(4);
		assertFalse(user instanceof Student);
		Set<Participation> lectureParcicipations = user.getLectureParticipant();
		assertEquals(3, lectureParcicipations.size());
		boolean found1 = false;
		boolean found2 = false;
		boolean found3 = false;
		for (Participation participation : lectureParcicipations) {
			assertEquals(participation.getRoleType(), ParticipationRole.NORMAL);
			if (participation.getLecture().getId() == 1) {
				found1 = true;
			}
			if (participation.getLecture().getId() == 2) {
				found2 = true;
			}
			if (participation.getLecture().getId() == 3) {
				found3 = true;
			}
		}
		assertTrue(found1 && found2 && found3);
	}
}
