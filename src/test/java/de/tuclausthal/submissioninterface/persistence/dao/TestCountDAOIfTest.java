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

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

@GATEDBTest
class TestCountDAOIfTest extends BasicTest {

	@Test
	void testUsedOnce() {
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(10);
		de.tuclausthal.submissioninterface.persistence.datamodel.Test test = DAOFactory.TestDAOIf(session).getTest(1);
		assertEquals(1, DAOFactory.TestCountDAOIf(session).canStillRunXTimes(test, submission));
	}

	@Test
	void testOneStudentUsedAllTests() {
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(12);
		de.tuclausthal.submissioninterface.persistence.datamodel.Test test = DAOFactory.TestDAOIf(session).getTest(1);
		assertEquals(0, DAOFactory.TestCountDAOIf(session).canStillRunXTimes(test, submission));
		assertEquals(false, DAOFactory.TestCountDAOIf(session).canSeeResultAndIncrementCounter(test, submission));
	}

	@Test
	void testAllTestsUnused() {
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(12);
		de.tuclausthal.submissioninterface.persistence.datamodel.Test test = DAOFactory.TestDAOIf(session).getTest(2);
		assertEquals(2, DAOFactory.TestCountDAOIf(session).canStillRunXTimes(test, submission));
	}

	@Test
	void testTakeATest() {
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmission(12);
		session.getTransaction().begin();
		de.tuclausthal.submissioninterface.persistence.datamodel.Test test = DAOFactory.TestDAOIf(session).createCompileTest(submission.getTask());
		test.setTimesRunnableByStudents(0);
		DAOFactory.TestDAOIf(session).saveTest(test);
		session.getTransaction().commit();
		assertEquals(0, DAOFactory.TestCountDAOIf(session).canStillRunXTimes(test, submission));
		session.getTransaction().begin();
		test.setTimesRunnableByStudents(2);
		DAOFactory.TestDAOIf(session).saveTest(test);
		session.getTransaction().commit();
		assertEquals(2, DAOFactory.TestCountDAOIf(session).canStillRunXTimes(test, submission));
		assertEquals(true, DAOFactory.TestCountDAOIf(session).canSeeResultAndIncrementCounter(test, submission));
		assertEquals(1, DAOFactory.TestCountDAOIf(session).canStillRunXTimes(test, submission));
		assertEquals(true, DAOFactory.TestCountDAOIf(session).canSeeResultAndIncrementCounter(test, submission));
		assertEquals(0, DAOFactory.TestCountDAOIf(session).canStillRunXTimes(test, submission));
		assertEquals(false, DAOFactory.TestCountDAOIf(session).canSeeResultAndIncrementCounter(test, submission));
		session.getTransaction().begin();
		DAOFactory.TestDAOIf(session).deleteTest(test);
		session.getTransaction().commit();
	}
}
