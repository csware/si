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

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.OrderBy;

import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;

/**
 * Java template test
 * @author Sven Strickroth
 */
@Entity
public class JavaAdvancedIOTest extends Test {
	private static final long serialVersionUID = 1L;

	private List<JavaAdvancedIOTestStep> testSteps;

	@Override
	@Transient
	public AbstractTest getTestImpl() {
		return new de.tuclausthal.submissioninterface.testframework.tests.impl.JavaAdvancedIOTest();
	}

	/**
	 * @return the testSteps
	 */
	@OneToMany(mappedBy = "test")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy(clause = "teststepid asc")
	public List<JavaAdvancedIOTestStep> getTestSteps() {
		return testSteps;
	}

	/**
	 * @param testSteps the testSteps to set
	 */
	public void setTestSteps(List<JavaAdvancedIOTestStep> testSteps) {
		this.testSteps = testSteps;
	}
}
