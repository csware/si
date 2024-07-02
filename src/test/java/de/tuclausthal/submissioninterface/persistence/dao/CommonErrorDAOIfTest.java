/*
 * Copyright 2023-2024 Sven Strickroth <email@cs-ware.de>
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

import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommonError;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.testanalyzer.CommonErrorAnalyzer;

@GATEDBTest
class CommonErrorDAOIfTest extends BasicTest {
	@Test
	void testAnalyzeAndDelete() {
		final Transaction tx = session.beginTransaction();
		final de.tuclausthal.submissioninterface.persistence.datamodel.Test test = DAOFactory.TestDAOIf(session).getTest(2);
		assertEquals(0, DAOFactory.CommonErrorDAOIf(session).getCommonErrors(test).size());

		final CommonErrorAnalyzer analyzer = new CommonErrorAnalyzer(session);
		for (final TestResult testResult : DAOFactory.TestResultDAOIf(session).getResults(test)) {
			analyzer.runAnalysis(testResult);
		}

		final List<CommonError> commonErrors = DAOFactory.CommonErrorDAOIf(session).getCommonErrors(test);
		assertEquals(1, commonErrors.size());
		assertEquals("\"Hello World\" failed + Output null ", commonErrors.get(0).getTitle());
		assertEquals(1, commonErrors.get(0).getTestResults().size());
		assertEquals(5, commonErrors.get(0).getTestResults().iterator().next().getId());

		DAOFactory.CommonErrorDAOIf(session).reset(test);
		assertEquals(0, DAOFactory.CommonErrorDAOIf(session).getCommonErrors(test).size());

		tx.rollback();
	}
}
