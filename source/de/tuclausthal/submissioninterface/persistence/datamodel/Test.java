/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Function test
 * @author Sven Strickroth
 */
@Entity
@Table(name = "tests")
public abstract class Test implements Serializable {
	private int id;
	private int timesRunnableByStudents = 0;
	private boolean forTutors = false;
	private Task task;
	private int timeout = 5;

	/**
	 * @return the visibleToStudents
	 */
	@Column(nullable = false)
	public int getTimesRunnableByStudents() {
		return timesRunnableByStudents;
	}

	/**
	 * @param visibleToStudents the visibleToStudents to set
	 */
	public void setTimesRunnableByStudents(int timesRunnableByStudents) {
		this.timesRunnableByStudents = timesRunnableByStudents;
	}

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue
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
	@Column(nullable = false)
	public boolean isForTutors() {
		return forTutors;
	}

	/**
	 * @param forTutors the forTutors to set
	 */
	public void setForTutors(boolean forTutors) {
		this.forTutors = forTutors;
	}
}
