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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

@GATEDBTest
class TaskNumberDAOIfTest extends BasicTest {

	@Test
	void testGetTaskNumbersForSubmissionNone() {
		assertEquals(0, DAOFactory.TaskNumberDAOIf(session).getTaskNumbersForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(3)).size());
	}

	@Test
	void testGetTaskNumbersForSubmissionOne() {
		List<TaskNumber> taskNubmers = DAOFactory.TaskNumberDAOIf(session).getTaskNumbersForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(1));
		assertEquals(1, taskNubmers.size());
		assertEquals("111100", taskNubmers.get(0).getNumber());
		assertEquals("60", taskNubmers.get(0).getOrigNumber());
		assertEquals(14, taskNubmers.get(0).getParticipation().getId());
	}

	@Test
	void testGetTaskNumbersForSubmissionOneGroupSubmission() {
		List<TaskNumber> taskNubmers = DAOFactory.TaskNumberDAOIf(session).getTaskNumbersForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(8));
		assertEquals(1, taskNubmers.size());
		assertEquals("101111", taskNubmers.get(0).getNumber());
		assertEquals("47", taskNubmers.get(0).getOrigNumber());
		assertEquals(4, taskNubmers.get(0).getParticipation().getId());
	}

	@Test
	void testGetTaskNumbersForTaskLockedNone() {
		session.getTransaction().begin();
		assertEquals(0, DAOFactory.TaskNumberDAOIf(session).getTaskNumbersForTaskLocked(DAOFactory.TaskDAOIf(session).getTask(1), DAOFactory.ParticipationDAOIf(session).getParticipation(19)).size());
		session.getTransaction().rollback();
	}

	@Test
	void testGetTaskNumbersForTaskLockedOneSubmitted() {
		session.getTransaction().begin();
		List<TaskNumber> taskNubmers = DAOFactory.TaskNumberDAOIf(session).getTaskNumbersForTaskLocked(DAOFactory.TaskDAOIf(session).getTask(1), DAOFactory.ParticipationDAOIf(session).getParticipation(14));
		assertEquals(1, taskNubmers.size());
		assertEquals("111100", taskNubmers.get(0).getNumber());
		assertEquals("60", taskNubmers.get(0).getOrigNumber());
		assertEquals(1, taskNubmers.get(0).getSubmission().getSubmissionid());
		session.getTransaction().rollback();
	}

	@Test
	void testGetTaskNumbersForTaskLockedOneUnsubmitted() {
		session.getTransaction().begin();
		List<TaskNumber> taskNubmers = DAOFactory.TaskNumberDAOIf(session).getTaskNumbersForTaskLocked(DAOFactory.TaskDAOIf(session).getTask(1), DAOFactory.ParticipationDAOIf(session).getParticipation(12));
		assertEquals(1, taskNubmers.size());
		assertEquals("110", taskNubmers.get(0).getNumber());
		assertEquals("6", taskNubmers.get(0).getOrigNumber());
		assertNull(taskNubmers.get(0).getSubmission());
		session.getTransaction().rollback();
	}

	@Test
	void testTwoDifferentParticipationsOneSubmitted() {
		session.getTransaction().begin();
		List<TaskNumber> taskNubmersSubmission = DAOFactory.TaskNumberDAOIf(session).getTaskNumbersForSubmission(DAOFactory.SubmissionDAOIf(session).getSubmission(15));
		assertEquals(1, taskNubmersSubmission.size());
		assertEquals("100010", taskNubmersSubmission.get(0).getNumber());
		assertEquals("34", taskNubmersSubmission.get(0).getOrigNumber());
		assertEquals(15, taskNubmersSubmission.get(0).getSubmission().getSubmissionid());
		assertEquals(8, taskNubmersSubmission.get(0).getParticipation().getId());

		List<TaskNumber> taskNubmersSubmittingUser = DAOFactory.TaskNumberDAOIf(session).getTaskNumbersForTaskLocked(DAOFactory.TaskDAOIf(session).getTask(1), DAOFactory.ParticipationDAOIf(session).getParticipation(8));
		assertEquals(1, taskNubmersSubmittingUser.size());
		assertEquals("100010", taskNubmersSubmittingUser.get(0).getNumber());
		assertEquals("34", taskNubmersSubmittingUser.get(0).getOrigNumber());
		assertEquals(15, taskNubmersSubmittingUser.get(0).getSubmission().getSubmissionid());

		List<TaskNumber> taskNubmersNotSubmittingUser = DAOFactory.TaskNumberDAOIf(session).getTaskNumbersForTaskLocked(DAOFactory.TaskDAOIf(session).getTask(1), DAOFactory.ParticipationDAOIf(session).getParticipation(10));
		assertEquals(1, taskNubmersNotSubmittingUser.size());
		assertEquals("1100001", taskNubmersNotSubmittingUser.get(0).getNumber());
		assertEquals("97", taskNubmersNotSubmittingUser.get(0).getOrigNumber());
		assertNull(taskNubmersNotSubmittingUser.get(0).getSubmission());
		session.getTransaction().rollback();
	}

}
