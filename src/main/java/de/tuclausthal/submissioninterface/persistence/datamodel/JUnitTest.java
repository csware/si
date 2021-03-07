/*
 * Copyright 2009-2012, 2020 Sven Strickroth <email@cs-ware.de>
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

import javax.persistence.Entity;
import javax.persistence.Transient;

import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.JavaJUnitTest;

/**
 * JUnit function test
 * @author Sven Strickroth
 */
@Entity
public class JUnitTest extends Test {
	private static final long serialVersionUID = 1L;

	private String mainClass = "AllTests";
	
	/**
	 * @return the mainClass
	 */
	public String getMainClass() {
		return mainClass;
	}

	/**
	 * @param mainClass the mainClass to set
	 */
	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}
	
	@Override
	@Transient
	public AbstractTest getTestImpl() {
		return new JavaJUnitTest();
	}
}
