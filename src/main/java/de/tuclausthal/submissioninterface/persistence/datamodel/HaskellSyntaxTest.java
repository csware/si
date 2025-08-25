/*
 * Copyright 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2025 Esat Avci <e.avci@campus.lmu.de>
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

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;

@Entity
public class HaskellSyntaxTest extends DockerTest {
	@Override
	@Transient
	public AbstractTest<DockerTest> getTestImpl() {
		return new de.tuclausthal.submissioninterface.testframework.tests.impl.HaskellSyntaxTest(this);
	}
}
