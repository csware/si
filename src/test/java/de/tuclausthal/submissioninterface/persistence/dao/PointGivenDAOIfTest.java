/*
 * Copyright 2020 Sven Strickroth <email@cs-ware.de>
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

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

@GATEDBTest
class PointGivenDAOIfTest extends BasicTest {
	@Test
	void testNothingGivenYet() {
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(4);
		assertEquals(0, DAOFactory.PointGivenDAOIf(session).getPointsGivenOfSubmission(submission).size());
	}

	@Test
	void testNothingGivenOne() {
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(3);
		assertEquals(2, DAOFactory.PointGivenDAOIf(session).getPointsGivenOfSubmission(submission).size());
	}

	@Test
	void testNothingGivenMultiple() {
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(10);
		assertEquals(2, DAOFactory.PointGivenDAOIf(session).getPointsGivenOfSubmission(submission).size());
	}

	@Test
	void testNothingGivenAuto() {
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(2);
		assertEquals(0, DAOFactory.PointGivenDAOIf(session).getPointsGivenOfSubmission(submission).size());
	}

}
