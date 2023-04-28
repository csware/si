/*
 * Copyright 2009-2012, 2020-2023 Sven Strickroth <email@cs-ware.de>
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

import java.io.Serializable;
import java.lang.invoke.MethodHandles;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;

/**
 * Function test
 * @author Sven Strickroth
 */
@Entity
@Table(name = "tests")
public abstract class Test implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private int timesRunnableByStudents = 0;
	private boolean forTutors = false;
	private Task task;
	private int timeout = 5;
	private String testTitle = "";
	private String testDescription = "";
	private boolean needsToRun = true;
	private boolean giveDetailsToStudents = false;

	/**
	 * @return the visibleToStudents
	 */
	public int getTimesRunnableByStudents() {
		return timesRunnableByStudents;
	}

	/**
	 * @param timesRunnableByStudents 
	 */
	public void setTimesRunnableByStudents(int timesRunnableByStudents) {
		this.timesRunnableByStudents = timesRunnableByStudents;
	}

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the task
	 */
	@ManyToOne
	@JoinColumn(name = "taskid", nullable = false)
	public Task getTask() {
		return task;
	}

	/**
	 * @param task the task to set
	 */
	public void setTask(Task task) {
		this.task = task;
	}

	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return the forTutors
	 */
	public boolean isForTutors() {
		return forTutors;
	}

	/**
	 * @param forTutors the forTutors to set
	 */
	public void setForTutors(boolean forTutors) {
		this.forTutors = forTutors;
	}

	/**
	 * @return the testTitle
	 */
	public String getTestTitle() {
		return testTitle;
	}

	/**
	 * @param testTitle the testTitle to set
	 */
	public void setTestTitle(String testTitle) {
		this.testTitle = testTitle;
	}

	/**
	 * @return the testDescription
	 */
	public String getTestDescription() {
		return testDescription;
	}

	/**
	 * @param testDescription the testDescription to set
	 */
	public void setTestDescription(String testDescription) {
		this.testDescription = testDescription;
	}

	/**
	 * @param needsToRun the needsToRun to set
	 */
	public void setNeedsToRun(boolean needsToRun) {
		this.needsToRun = needsToRun;
	}

	/**
	 * @return the needsToRun
	 */
	public boolean isNeedsToRun() {
		return needsToRun;
	}

	@Transient
	abstract public AbstractTest getTestImpl();

	/**
	 * @return the giveDetailsToStudents
	 */
	public boolean isGiveDetailsToStudents() {
		return giveDetailsToStudents;
	}

	/**
	 * @param giveDetailsToStudents the giveDetailsToStudents to set
	 */
	public void setGiveDetailsToStudents(boolean giveDetailsToStudents) {
		this.giveDetailsToStudents = giveDetailsToStudents;
	}

	@Transient
	abstract public boolean TutorsCanRun();

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): id:" + getId() + "; testtitle:" + getTestTitle() + "; taskid:" + (getTask() == null ? "null" : getTask().getTaskid());
	}
}
