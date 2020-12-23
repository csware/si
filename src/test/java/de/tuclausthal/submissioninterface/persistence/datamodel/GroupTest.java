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

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;

@GATEDBTest
class GroupTest extends BasicTest {
	@Test
	void testGetTutorsOne() {
		Group group = DAOFactory.GroupDAOIf(session).getGroup(1);
		assertEquals(1, group.getTutors().size());
	}

	@Test
	void testGetTutorsTwo() {
		Group group = DAOFactory.GroupDAOIf(session).getGroup(2);
		assertEquals(2, group.getTutors().size());
	}

	@Test
	void testGetTutorsNone() {
		Group group = DAOFactory.GroupDAOIf(session).getGroup(5);
		assertEquals(0, group.getTutors().size());
	}
}
