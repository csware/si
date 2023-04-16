/*
 * Copyright 2020, 2022-2023 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.datamodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

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
		assertEquals(2, task.getSimilarityTests().size());
		assertEquals(4, task.getSubmissions().size());
		assertEquals(4, task.getTests().size());
		assertFalse(task.showTextArea());
	}

	@Test
	void testGettersB() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(4);
		assertEquals(2, task.getTaskGroup().getTaskGroupId());
		assertEquals(0, task.getPointCategories().size());
		assertEquals(0, task.getSimilarityTests().size());
		assertEquals(0, task.getSubmissions().size());
		assertEquals(0, task.getTests().size());
		assertFalse(task.showTextArea());
	}

	@Test
	void testGettersC() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(1);
		assertEquals(LocalDateTime.of(2020, 12, 21, 10, 26, 50, 0), task.getStart().toLocalDateTime()); // HACK: for portability we don't test the timezone here
		assertEquals(1, task.getTaskGroup().getTaskGroupId());
		assertEquals(2, task.getPointCategories().size());
		assertEquals(1, task.getSimilarityTests().size());
		assertEquals(4, task.getSubmissions().size());
		assertEquals(0, task.getTests().size());
		assertTrue(task.showTextArea());
	}
}
