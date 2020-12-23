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
class SubmissionTest extends BasicTest {
	@Test
	void testGetSimilarSubmissions() {
		assertEquals(0, DAOFactory.SubmissionDAOIf(session).getSubmission(1).getSimilarSubmissions().size());
		assertEquals(2, DAOFactory.SubmissionDAOIf(session).getSubmission(3).getSimilarSubmissions().size());
		assertEquals(3, DAOFactory.SubmissionDAOIf(session).getSubmission(10).getSimilarSubmissions().size());
	}

	@Test
	void testGetSubmitters() {
		assertEquals("Lastname6, Firstname6", DAOFactory.SubmissionDAOIf(session).getSubmission(1).getSubmitterNames());
		assertEquals("Lastname6, Firstname6", DAOFactory.SubmissionDAOIf(session).getSubmission(3).getSubmitterNames());
		assertEquals("Lastname1, Firstname1; Lastname2, Firstname2", DAOFactory.SubmissionDAOIf(session).getSubmission(10).getSubmitterNames());
	}

	@Test
	void testGetTestResults() {
		assertEquals(2, DAOFactory.SubmissionDAOIf(session).getSubmission(3).getTestResults().size());
		assertEquals(0, DAOFactory.SubmissionDAOIf(session).getSubmission(5).getTestResults().size());
	}

}
