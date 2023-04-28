/*
 * Copyright 2021-2023 Sven Strickroth <email@cs-ware.de>
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
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.OrderBy;

import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;

/**
 * Docker test
 * @author Sven Strickroth
 */
@Entity
public class DockerTest extends Test {
	private static final long serialVersionUID = 1L;

	private String preparationShellCode;
	private List<DockerTestStep> testSteps;

	@Override
	@Transient
	public AbstractTest getTestImpl() {
		return new de.tuclausthal.submissioninterface.testframework.tests.impl.DockerTest(this);
	}

	/**
	 * @return the preparationShellCode
	 */
	@Column(length = 65536)
	public String getPreparationShellCode() {
		return preparationShellCode;
	}

	/**
	 * @param preparationShellCode the preparationShellCode to set
	 */
	public void setPreparationShellCode(String preparationShellCode) {
		this.preparationShellCode = preparationShellCode;
	}

	/**
	 * @return the testSteps
	 */
	@OneToMany(mappedBy = "test")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy(clause = "teststepid asc")
	public List<DockerTestStep> getTestSteps() {
		return testSteps;
	}

	/**
	 * @param testSteps the testSteps to set
	 */
	public void setTestSteps(List<DockerTestStep> testSteps) {
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
