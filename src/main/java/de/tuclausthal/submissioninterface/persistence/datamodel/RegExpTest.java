/*
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.testframework.tests.impl.JavaIORegexpTest;

@Entity
public class RegExpTest extends Test {
	private String commandLineParameter;
	private String mainClass;

	/**
	 * @return the regularExpression
	 */
	public String getRegularExpression() {
		return regularExpression;
	}

	/**
	 * @param regularExpression the regularExpression to set
	 */
	public void setRegularExpression(String regularExpression) {
		this.regularExpression = regularExpression;
	}

	private String regularExpression;

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

	/**
	 * @return the commandLineParameter
	 */
	public String getCommandLineParameter() {
		return commandLineParameter;
	}

	/**
	 * @param commandLineParameter the commandLineParameter to set
	 */
	public void setCommandLineParameter(String commandLineParameter) {
		this.commandLineParameter = commandLineParameter;
	}

	@Override
	@Transient
	public AbstractTest getTestImpl() {
		return new JavaIORegexpTest();
	}
}
