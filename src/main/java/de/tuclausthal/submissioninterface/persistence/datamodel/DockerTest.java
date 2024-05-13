/*
 * Copyright 2021-2024 Sven Strickroth <email@cs-ware.de>
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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;

/**
 * Docker test
 * @author Sven Strickroth
 */
@Entity
public class DockerTest extends Test {
	private static final long serialVersionUID = 1L;

	@Column(length = 65536)
	private String preparationShellCode;
	@OneToMany(mappedBy = "test", cascade = CascadeType.PERSIST)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy("teststepid asc")
	@JacksonXmlElementWrapper(localName = "testSteps")
	@JacksonXmlProperty(localName = "testStep")
	@JsonManagedReference
	private List<DockerTestStep> testSteps = new ArrayList<>();

	@Override
	@Transient
	public AbstractTest getTestImpl() {
		return new de.tuclausthal.submissioninterface.testframework.tests.impl.DockerTest(this);
	}

	/**
	 * @return the preparationShellCode
	 */
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
