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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;

@GATEDBTest
class TestDAOIfTest extends BasicTest {
	@Test
	void testgetStudentTestsNone() {
		assertEquals(0, DAOFactory.TestDAOIf(session).getStudentTests(DAOFactory.TaskDAOIf(session).getTask(4)).size());
	}

	@Test
	void testGetStudentTests() {
		List<de.tuclausthal.submissioninterface.persistence.datamodel.Test> tests = DAOFactory.TestDAOIf(session).getStudentTests(DAOFactory.TaskDAOIf(session).getTask(3));
		assertEquals(3, tests.size());
		for (de.tuclausthal.submissioninterface.persistence.datamodel.Test test : tests) {
			assertTrue(test.getTimesRunnableByStudents() > 0);
		}
		assertEquals(1, tests.get(0).getId());
		assertEquals(2, tests.get(1).getId());
		assertEquals(4, tests.get(2).getId());
	}

	@Test
	void testGetTutorTestsNone() {
		assertEquals(0, DAOFactory.TestDAOIf(session).getStudentTests(DAOFactory.TaskDAOIf(session).getTask(4)).size());
	}

	@Test
	void testGetTutorTests() {
		List<de.tuclausthal.submissioninterface.persistence.datamodel.Test> tests = DAOFactory.TestDAOIf(session).getTutorTests(DAOFactory.TaskDAOIf(session).getTask(3));
		assertEquals(3, tests.size());
		for (de.tuclausthal.submissioninterface.persistence.datamodel.Test test : tests) {
			assertTrue(test.isForTutors());
		}
		assertEquals(1, tests.get(0).getId());
		assertEquals(2, tests.get(1).getId());
		assertEquals(3, tests.get(2).getId());
	}
}
