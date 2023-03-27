/*
 * Copyright 2022-2023 Sven Strickroth <email@cs-ware.de>
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

@GATEDBTest
class MCOptionDAOIfTest extends BasicTest {
	@Test
	void testCreateDeleteMCOption() {
		Transaction tx = session.beginTransaction();
		Task task = DAOFactory.TaskDAOIf(session).getTask(2);
		List<MCOption> mcOptions = DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(task);

		MCOption newMCOption = DAOFactory.MCOptionDAOIf(session).createMCOption(task, "New option", false);
		assertFalse(mcOptions.contains(newMCOption));
		assertEquals(mcOptions.size() + 1, DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(task).size());
		assertTrue(DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(task).contains(newMCOption));

		DAOFactory.MCOptionDAOIf(session).deleteMCOption(newMCOption);
		assertEquals(mcOptions.size(), DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(task).size());
		assertFalse(DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(task).contains(newMCOption));

		tx.rollback();
	}

	@Test
	void testMCOptionsEmpty() {
		assertEquals(0, DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(DAOFactory.TaskDAOIf(session).getTask(1)).size());
	}

	@Test
	void testMCOptionsMCTask() {
		var mcOptions = DAOFactory.MCOptionDAOIf(session).getMCOptionsForTask(DAOFactory.TaskDAOIf(session).getTask(2));
		assertEquals(4, mcOptions.size());
		assertEquals(1, mcOptions.stream().filter(option -> option.isCorrect()).count());
	}
}
