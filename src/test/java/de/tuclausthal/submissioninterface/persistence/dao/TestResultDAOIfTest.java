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

package de.tuclausthal.submissioninterface.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;

@GATEDBTest
class TestResultDAOIfTest extends BasicTest {
	@Test
	void testGetResultNone() {
		assertNull(DAOFactory.TestResultDAOIf(session).getResult(DAOFactory.TestDAOIf(session).getTest(5), DAOFactory.SubmissionDAOIf(session).getSubmission(7)));
	}

	@Test
	void testGetResultNegative() {
		TestResult result = DAOFactory.TestResultDAOIf(session).getResult(DAOFactory.TestDAOIf(session).getTest(1), DAOFactory.SubmissionDAOIf(session).getSubmission(7));
		assertNotNull(result);
		assertFalse(result.getPassedTest());
	}

	@Test
	void testGetResultPositive() {
		TestResult result = DAOFactory.TestResultDAOIf(session).getResult(DAOFactory.TestDAOIf(session).getTest(1), DAOFactory.SubmissionDAOIf(session).getSubmission(3));
		assertNotNull(result);
		assertTrue(result.getPassedTest());
	}

	@Test
	void testGetResults() {
		assertTrue(DAOFactory.TestResultDAOIf(session).getResults(DAOFactory.TaskDAOIf(session).getTask(1)).isEmpty());
		assertTrue(DAOFactory.TestResultDAOIf(session).getResults(DAOFactory.TaskDAOIf(session).getTask(2)).isEmpty());

		Map<Integer, Map<Integer, Boolean>> task3TestResults = DAOFactory.TestResultDAOIf(session).getResults(DAOFactory.TaskDAOIf(session).getTask(3));
		assertEquals(4, task3TestResults.size());

		assertTrue(task3TestResults.containsKey(3));
		assertEquals(2, task3TestResults.get(3).size());
		assertTrue(task3TestResults.get(3).get(1));
		assertTrue(task3TestResults.get(3).get(2));
		assertTrue(task3TestResults.containsKey(7));
		assertEquals(2, task3TestResults.get(7).size());
		assertFalse(task3TestResults.get(7).get(1));
		assertFalse(task3TestResults.get(7).get(2));
		assertTrue(task3TestResults.containsKey(10));
		assertEquals(2, task3TestResults.get(10).size());
		assertTrue(task3TestResults.get(10).get(1));
		assertTrue(task3TestResults.get(10).get(2));
		assertTrue(task3TestResults.containsKey(12));
		assertEquals(2, task3TestResults.get(12).size());
		assertTrue(task3TestResults.get(12).get(1));
		assertTrue(task3TestResults.get(12).get(2));
	}
}
