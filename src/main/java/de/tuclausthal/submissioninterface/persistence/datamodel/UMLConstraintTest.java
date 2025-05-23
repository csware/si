/*
 * Copyright 2020-2024 Sven Strickroth <email@cs-ware.de>
 * Copyright 2011 Joachim Schramm
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

import java.lang.invoke.MethodHandles;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.JavaUMLConstraintTest;

@Entity
public class UMLConstraintTest extends Test {
	private static final long serialVersionUID = 1L;

	@Override
	@Transient
	public AbstractTest<? extends Test> getTestImpl() {
		return new JavaUMLConstraintTest(this);
	}

	@Override
	@Transient
	public boolean TutorsCanRun() {
		return true;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): id:" + getId() + "; testtitle:" + getTestTitle() + "; taskid:" + (getTask() == null ? "null" : getTask().getTaskid());
	}
}
