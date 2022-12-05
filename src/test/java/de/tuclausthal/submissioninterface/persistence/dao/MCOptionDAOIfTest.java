/*
 * Copyright 2022 Sven Strickroth <email@cs-ware.de>
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

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;

@GATEDBTest
class MCOptionDAOIfTest extends BasicTest {
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
