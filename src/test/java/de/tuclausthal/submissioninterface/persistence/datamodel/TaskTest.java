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
class TaskTest extends BasicTest {
	@Test
	void testGettersA() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(3);
		assertEquals(2, task.getTaskGroup().getTaskGroupId());
		assertEquals(2, task.getPointCategories().size());
		assertEquals(2, task.getSimularityTests().size());
		assertEquals(4, task.getSubmissions().size());
		assertEquals(4, task.getTests().size());
	}

	@Test
	void testGettersB() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(4);
		assertEquals(2, task.getTaskGroup().getTaskGroupId());
		assertEquals(0, task.getPointCategories().size());
		assertEquals(0, task.getSimularityTests().size());
		assertEquals(0, task.getSubmissions().size());
		assertEquals(0, task.getTests().size());
	}


	@Test
	void testGettersC() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(1);
		assertEquals(1, task.getTaskGroup().getTaskGroupId());
		assertEquals(2, task.getPointCategories().size());
		assertEquals(1, task.getSimularityTests().size());
		assertEquals(4, task.getSubmissions().size());
		assertEquals(0, task.getTests().size());
	}
}
