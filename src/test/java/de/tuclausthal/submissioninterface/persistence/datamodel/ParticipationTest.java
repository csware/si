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

package de.tuclausthal.submissioninterface.persistence.datamodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;

@GATEDBTest
class ParticipationTest extends BasicTest {
	@Test
	void testGetSubmissionsFew() {
		Participation participation = DAOFactory.ParticipationDAOIf(session).getParticipation(14);
		assertEquals(1, participation.getLecture().getId());
		assertNull(participation.getGroup());
		assertEquals(3, participation.getSubmissions().size());
	}

	@Test
	void testGetSubmissionsNone() {
		Participation participation = DAOFactory.ParticipationDAOIf(session).getParticipation(15);
		assertEquals(2, participation.getLecture().getId());
		assertNull(participation.getGroup());
		assertEquals(0, participation.getSubmissions().size());
	}
}
