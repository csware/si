/*
 * Copyright 2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.tests.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;

public class JUnitTestTest {
	private final JUnitTest test = new JUnitTest();

	@ParameterizedTest
	@ValueSource(strings = { "AllTests", "de.lmu.ifi.tel.AllTests", "de.tu-clausthal.ifi.AllTests", "alltests" })
	void testMainClassNameOk(final String mainClassName) {
		test.setMainClass(mainClassName);
		assertEquals(mainClassName, test.getMainClass());
	}

	@ParameterizedTest
	@ValueSource(strings = { "", ".", "/Something", "-Something", ";Something", "&&Something", "\"Something", "Something ", "Something\"", "Something &&", "Something&&", "Something;", "Something -" })
	void testMainClassNameIllegal(final String mainClassName) {
		assertThrows(IllegalArgumentException.class, () -> test.setMainClass(mainClassName));
	}
}
