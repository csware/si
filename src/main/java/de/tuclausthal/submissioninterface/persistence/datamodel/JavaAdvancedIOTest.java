/*
 * Copyright 2020-2023 Sven Strickroth <email@cs-ware.de>
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
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;

/**
 * Java template test
 * @author Sven Strickroth
 */
@Entity
public class JavaAdvancedIOTest extends Test {
	private static final long serialVersionUID = 1L;

	private List<JavaAdvancedIOTestStep> testSteps = new ArrayList<>();

	@Override
	@Transient
	public AbstractTest getTestImpl() {
		return new de.tuclausthal.submissioninterface.testframework.tests.impl.JavaAdvancedIOTest(this);
	}

	/**
	 * @return the testSteps
	 */
	@OneToMany(mappedBy = "test")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy("teststepid asc")
	public List<JavaAdvancedIOTestStep> getTestSteps() {
		return testSteps;
	}

	/**
	 * @param testSteps the testSteps to set
	 */
	public void setTestSteps(List<JavaAdvancedIOTestStep> testSteps) {
		this.testSteps = testSteps;
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
