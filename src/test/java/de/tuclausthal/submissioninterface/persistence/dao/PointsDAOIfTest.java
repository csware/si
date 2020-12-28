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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;

@GATEDBTest
class PointsDAOIfTest extends BasicTest {
	@Test
	void testGetPointHistoryForSubmission() {
		assertEquals(6, DAOFactory.PointsDAOIf(session).getPointHistoryForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(1)).size());
		assertEquals(0, DAOFactory.PointsDAOIf(session).getPointHistoryForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(2)).size());
		assertEquals(7, DAOFactory.PointsDAOIf(session).getPointHistoryForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(3)).size());
		assertEquals(4, DAOFactory.PointsDAOIf(session).getPointHistoryForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(8)).size());
		assertEquals(8, DAOFactory.PointsDAOIf(session).getPointHistoryForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(10)).size());
	}

	@Test
	void testGetAllPointsForLecture() {
		assertTrue(DAOFactory.PointsDAOIf(session).getAllPointsForLecture(DAOFactory.LectureDAOIf(session).getLecture(2)).isEmpty());

		Map<Integer, Integer> lecture1Points = DAOFactory.PointsDAOIf(session).getAllPointsForLecture(DAOFactory.LectureDAOIf(session).getLecture(1));
		assertEquals(4, lecture1Points.size());
		assertEquals(150, lecture1Points.get(14));
		assertEquals(250, lecture1Points.get(4));
		assertEquals(250, lecture1Points.get(6));
		assertEquals(100, lecture1Points.get(12));
	}
}
