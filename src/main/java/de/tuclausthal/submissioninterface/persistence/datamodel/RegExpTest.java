/*
 * Copyright 2009-2010, 2020-2024 Sven Strickroth <email@cs-ware.de>
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
import java.nio.file.Path;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;

@Entity
public class RegExpTest extends Test {
	private static final long serialVersionUID = 1L;
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
	public AbstractTest<Test> getTestImpl() {
		return new AbstractTest<>(this) {
			@Override
			public void performTest(Path basePath, Path submissionPath, TestExecutorTestResult testResult) throws Exception {
				LOG.warn("Deprecated Java RegExp Test requested: taskid={}, testid={}", getTask().getTaskid(), getId());
				testResult.setTestPassed(false);
				testResult.setTestOutput("Deprecated Java RegExp Test. Not executed. Use JavaAdvancedIOTest which is more powerful.");
			}
		};
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
