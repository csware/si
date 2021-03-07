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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

@GATEDBTest
class SimilarityDAOIfTest extends BasicTest {

	@Test
	void testNoMaxSimilarity() {
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(7);
		SimilarityTest simTest = DAOFactory.SimilarityTestDAOIf(session).getSimilarityTest(3);
		assertEquals(0, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission).size());
		assertEquals(0, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission).size());
	}

	@Test
	void testMaxSimilarityMulti() {
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(10);
		SimilarityTest simTest = DAOFactory.SimilarityTestDAOIf(session).getSimilarityTest(3);
		List<Similarity> simList = DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission);
		assertEquals(2, simList.size());
		assertEquals(100, simList.get(0).getPercentage());
		assertEquals(12, simList.get(0).getSubmissionTwo().getSubmissionid());
		assertEquals(91, simList.get(1).getPercentage());
		assertEquals(3, simList.get(1).getSubmissionTwo().getSubmissionid());
		List<Similarity> simListMax = DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission);
		assertEquals(1, simListMax.size());
		assertEquals(100, simListMax.get(0).getPercentage());
		assertEquals(12, simListMax.get(0).getSubmissionTwo().getSubmissionid());
	}

	@Test
	void testMaxSimilarityAsym() {
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(3);
		SimilarityTest simTest = DAOFactory.SimilarityTestDAOIf(session).getSimilarityTest(3);
		assertEquals(2, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission).size());
		assertEquals(2, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission).size());
	}

	@Test
	void testAddSimilarityResult() {
		Submission submission2 = DAOFactory.SubmissionDAOIf(session).getSubmission(2);
		Submission submission3 = DAOFactory.SubmissionDAOIf(session).getSubmission(3);
		Submission submission4 = DAOFactory.SubmissionDAOIf(session).getSubmission(4);
		Submission submission5 = DAOFactory.SubmissionDAOIf(session).getSubmission(5);
		session.getTransaction().begin();
		SimilarityTest simTest = DAOFactory.SimilarityTestDAOIf(session).addSimilarityTest(submission2.getTask(), "", "", false,"" , 0, "");
		session.getTransaction().commit();
		DAOFactory.SimilarityDAOIf(session).addSimilarityResult(simTest, submission2, submission3, 23);
		DAOFactory.SimilarityDAOIf(session).addSimilarityResult(simTest, submission4, submission5, 10);
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission2).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission2).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission3).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission3).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission4).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission4).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission5).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission5).size());
		DAOFactory.SimilarityDAOIf(session).addSimilarityResult(simTest, submission2, submission3, 42);
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission2).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission2).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission3).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission3).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission4).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission4).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission5).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission5).size());
		DAOFactory.SimilarityDAOIf(session).addSimilarityResult(simTest, submission2, submission4, 42);
		assertEquals(2, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission2).size());
		assertEquals(2, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission2).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission3).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission3).size());
		assertEquals(2, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission4).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission4).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission5).size());
		assertEquals(1, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission5).size());
		DAOFactory.SimilarityTestDAOIf(session).resetSimilarityTest(simTest);
		assertEquals(0, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission2).size());
		assertEquals(0, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission2).size());
		assertEquals(0, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission3).size());
		assertEquals(0, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission3).size());
		assertEquals(0, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission4).size());
		assertEquals(0, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission4).size());
		assertEquals(0, DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(simTest, submission5).size());
		assertEquals(0, DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(simTest, submission5).size());
		session.getTransaction().begin();
		DAOFactory.SimilarityTestDAOIf(session).deleteSimilarityTest(simTest);
		session.getTransaction().commit();
	}

	@Test
	void testGetMaxSimilarities() {
		assertTrue(DAOFactory.SimilarityDAOIf(session).getMaxSimilarities(DAOFactory.TaskDAOIf(session).getTask(1)).isEmpty());
		assertTrue(DAOFactory.SimilarityDAOIf(session).getMaxSimilarities(DAOFactory.TaskDAOIf(session).getTask(2)).isEmpty());

		Map<Integer, Map<Integer, List<Similarity>>> task3Similarities = DAOFactory.SimilarityDAOIf(session).getMaxSimilarities(DAOFactory.TaskDAOIf(session).getTask(3));
		assertEquals(4, task3Similarities.size());
		assertFalse(task3Similarities.containsKey(1));

		assertTrue(task3Similarities.containsKey(7));
		assertEquals(1, task3Similarities.get(7).size());
		assertEquals(1, task3Similarities.get(7).get(2).size());
		assertEquals(42, task3Similarities.get(7).get(2).get(0).getPercentage());
		assertEquals(2, task3Similarities.get(7).get(2).get(0).getSimilarityTest().getSimilarityTestId());
		assertEquals(7, task3Similarities.get(7).get(2).get(0).getSubmissionOne().getSubmissionid());
		assertEquals(10, task3Similarities.get(7).get(2).get(0).getSubmissionTwo().getSubmissionid());

		assertTrue(task3Similarities.containsKey(3));
		assertEquals(1, task3Similarities.get(3).size());
		assertEquals(2, task3Similarities.get(3).get(3).size());
		assertEquals(91, task3Similarities.get(3).get(3).get(0).getPercentage());
		assertEquals(91, task3Similarities.get(3).get(3).get(1).getPercentage());
		assertEquals(3, task3Similarities.get(3).get(3).get(0).getSubmissionOne().getSubmissionid());
		assertEquals(3, task3Similarities.get(3).get(3).get(1).getSubmissionOne().getSubmissionid());
		assertEquals(10, task3Similarities.get(3).get(3).get(0).getSubmissionTwo().getSubmissionid());
		assertEquals(12, task3Similarities.get(3).get(3).get(1).getSubmissionTwo().getSubmissionid());

		assertTrue(task3Similarities.containsKey(10));
		assertEquals(2, task3Similarities.get(10).size());
		assertEquals(1, task3Similarities.get(10).get(2).size());
		assertEquals(42, task3Similarities.get(10).get(2).get(0).getPercentage());
		assertEquals(10, task3Similarities.get(10).get(2).get(0).getSubmissionOne().getSubmissionid());
		assertEquals(7, task3Similarities.get(10).get(2).get(0).getSubmissionTwo().getSubmissionid());
		assertEquals(1, task3Similarities.get(10).get(3).size());
		assertEquals(100, task3Similarities.get(10).get(3).get(0).getPercentage());
		assertEquals(10, task3Similarities.get(10).get(3).get(0).getSubmissionOne().getSubmissionid());
		assertEquals(12, task3Similarities.get(10).get(3).get(0).getSubmissionTwo().getSubmissionid());

		assertTrue(task3Similarities.containsKey(12));
		assertEquals(1, task3Similarities.get(12).size());
		assertEquals(1, task3Similarities.get(12).get(3).size());
		assertEquals(100, task3Similarities.get(12).get(3).get(0).getPercentage());
		assertEquals(12, task3Similarities.get(12).get(3).get(0).getSubmissionOne().getSubmissionid());
		assertEquals(10, task3Similarities.get(12).get(3).get(0).getSubmissionTwo().getSubmissionid());
	}
}
