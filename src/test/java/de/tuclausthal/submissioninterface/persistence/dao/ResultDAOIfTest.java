/*
 * Copyright 2020, 2022 Sven Strickroth <email@cs-ware.de>
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

import java.util.List;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

@GATEDBTest
class ResultDAOIfTest extends BasicTest {
	@Test
	void testGetResultsForSubmissionNone() {
		assertEquals(0, DAOFactory.ResultDAOIf(session).getResultsForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(3)).size());
	}

	@Test
	void testGetResultsForSubmissionOne() {
		List<String> results = DAOFactory.ResultDAOIf(session).getResultsForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(1));
		assertEquals(1, results.size());
		assertEquals("60", results.get(0));
	}

	@Test
	void testCreateResults() {
		session.beginTransaction();
		Submission submissionDrei = DAOFactory.SubmissionDAOIf(session).getSubmission(3);
		assertEquals(0, DAOFactory.ResultDAOIf(session).getResultsForSubmission(submissionDrei).size());
		DAOFactory.ResultDAOIf(session).createResults(submissionDrei, List.of("a", "v", "fddsf", "dsf"));
		assertEquals(List.of("a", "v", "fddsf", "dsf"), DAOFactory.ResultDAOIf(session).getResultsForSubmission(submissionDrei));
		DAOFactory.ResultDAOIf(session).createResults(submissionDrei, List.of("asf"));
		assertEquals(List.of("asf"), DAOFactory.ResultDAOIf(session).getResultsForSubmission(submissionDrei));
		DAOFactory.ResultDAOIf(session).createResults(submissionDrei, List.of());
		assertEquals(0, DAOFactory.ResultDAOIf(session).getResultsForSubmission(submissionDrei).size());
		session.getTransaction().rollback();
	}
}
