/*
 * Copyright 2021 Sven Strickroth <email@cs-ware.de>
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

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

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
		return new de.tuclausthal.submissioninterface.testframework.tests.impl.DockerTest();
	}

	/**
	 * @return the preparationShellCode
	 */
	@Lob
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
}
