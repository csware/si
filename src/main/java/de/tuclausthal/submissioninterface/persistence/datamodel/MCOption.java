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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Options shown for Multiple Choice Questions
 * @author Sven Strickroth
 */
@Entity
@Table(name = "mcoptions")
public class MCOption implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String title = "";
	private boolean correct = false;
	private Task task;

	// for Hibernate
	private MCOption() {}

	public MCOption(Task task, String option, boolean correct) {
		this.task = task;
		this.title = option;
		this.correct = correct;
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
	 * @return the option
	 */
	@Column(nullable = false)
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the option to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the correct
	 */
	@Column(nullable = false)
	public boolean isCorrect() {
		return correct;
	}

	/**
	 * @param correct the correct to set
	 */
	public void setCorrect(boolean correct) {
		this.correct = correct;
	}

	/**
	 * @return the task
	 */
	@ManyToOne
	@JoinColumn(name = "taskid", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public Task getTask() {
		return task;
	}

	/**
	 * @param task the task to set
	 */
	public void setTask(Task task) {
		this.task = task;
	}
}
