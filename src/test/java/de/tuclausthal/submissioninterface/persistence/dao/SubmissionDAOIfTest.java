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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

@GATEDBTest
class SubmissionDAOIfTest extends BasicTest {
	@Test
	void testGetSubmissionsForTaskOfGroupOrderedNoGroupNoSubmissions() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(4);
		assertEquals(0, DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOfGroupOrdered(task, null).size());
	}

	@Test
	void testGetSubmissionsForTaskOfGroupOrderedNoSubmissions() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(1);
		Group group = DAOFactory.GroupDAOIf(session).getGroup(3);
		assertEquals(0, DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOfGroupOrdered(task, group).size());
	}

	@Test
	void testGetSubmissionsForTaskOfGroupOrderedNoGroup() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(1);
		List<Submission> submissions = DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOfGroupOrdered(task, null);
		assertEquals(2, submissions.size());
		assertEquals(1, submissions.get(0).getSubmissionid());
		assertEquals(4, submissions.get(1).getSubmissionid());
	}

	@Test
	void testGetSubmissionsForTaskOfGroupOrdered() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(2);
		Group group = DAOFactory.GroupDAOIf(session).getGroup(1);
		List<Submission> submissions = DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOfGroupOrdered(task, group);
		assertEquals(3, submissions.size());
		assertEquals(9, submissions.get(0).getSubmissionid());
		assertEquals(9, submissions.get(1).getSubmissionid()); // TODO: duplicates possible
		assertEquals(13, submissions.get(2).getSubmissionid());
	}

	@Test
	void testGetUngradedSubmissionNone() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(2);
		assertNull(DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, -1));
	}

	@Test
	void testGetUngradedSubmissionNoneNoSub() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(4);
		assertNull(DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, 1));
	}

	@Test
	void testGetUngradedSubmission() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(3);
		assertEquals(12, DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, -1).getSubmissionid());
		assertEquals(12, DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, 3).getSubmissionid());
		assertNull(DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, 12));
	}

	@Test
	void testGetUngradedSubmissionNoGroupNone() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(3);
		assertNull(DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, -1, null));
	}

	@Test
	void testGetUngradedSubmissionNoGroupOne() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(1);
		assertEquals(4, DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, -1, null).getSubmissionid());
		assertNull(DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, 4, null));
	}

	@Test
	void testGetUngradedSubmissionGroupNone() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(3);
		Group group = DAOFactory.GroupDAOIf(session).getGroup(1);
		assertNull(DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, -1, group));
	}

	@Test
	void testGetUngradedSubmissionGroupOne() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(3);
		Group group = DAOFactory.GroupDAOIf(session).getGroup(2);
		assertEquals(12, DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, -1, group).getSubmissionid());
		assertNull(DAOFactory.SubmissionDAOIf(session).getUngradedSubmission(task, 12, group));
	}

	@Test
	void testGetSubmissionsForTaskOrderedA() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(1);
		List<Submission> submissions = DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOrdered(task);
		assertEquals(6, submissions.size());
		assertEquals(1, submissions.get(0).getSubmissionid());
		assertEquals(4, submissions.get(1).getSubmissionid());
		assertEquals(8, submissions.get(2).getSubmissionid());
		assertEquals(8, submissions.get(3).getSubmissionid()); // TODO: duplicate
		assertEquals(15, submissions.get(4).getSubmissionid());
		assertEquals(15, submissions.get(5).getSubmissionid()); // TODO: duplicate
	}

	@Test
	void testGetSubmissionsForTaskOrderedB() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(2);
		List<Submission> submissions = DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOrdered(task);
		assertEquals(6, submissions.size());
		assertEquals(2, submissions.get(0).getSubmissionid());
		assertEquals(5, submissions.get(1).getSubmissionid());
		assertEquals(9, submissions.get(2).getSubmissionid());
		assertEquals(9, submissions.get(3).getSubmissionid()); // TODO: duplicate
		assertEquals(13, submissions.get(4).getSubmissionid());
		assertEquals(14, submissions.get(5).getSubmissionid());
	}

	@Test
	void testGetSubmission() {
		assertNull(DAOFactory.SubmissionDAOIf(session).getSubmission(DAOFactory.TaskDAOIf(session).getTask(1), DAOFactory.UserDAOIf(session).getUser(2)));
		assertNotNull(DAOFactory.SubmissionDAOIf(session).getSubmission(DAOFactory.TaskDAOIf(session).getTask(1), DAOFactory.UserDAOIf(session).getUser(3)));
	}

	@Test
	void testGetSubmissionsForSearch() {
		Task task1 = DAOFactory.TaskDAOIf(session).getTask(1);
		Task task3 = DAOFactory.TaskDAOIf(session).getTask(3);
		assertEquals(0, DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task1, "60", false, false, false).size());
		assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task1, "really good", true, false, false).size());
		assertEquals(0, DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task1, "really good", false, true, false).size());
		assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task1, "internal comment", false, true, false).size());
		assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task1, "really good", true, true, true).size());
		assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task1, "ok", true, true, true).size());
		assertEquals(0, DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task1, "ist plagiat", true, true, true).size());
		assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task3, "ist plagiat", true, true, true).size());
		assertEquals(2, DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task3, "nicht", true, true, true).size());
		assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task3, "GaussscheSummenFormel", false, false, true).size());
		assertEquals(4, DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task3, "Hello World", false, false, true).size());
		assertEquals(1, DAOFactory.SubmissionDAOIf(session).getSubmissionsForSearch(task3, "HelloWorld", false, false, true).size());
	}
}
