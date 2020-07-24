/*
 * Copyright 2010, 2020 Sven Strickroth <email@cs-ware.de>
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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.OrderBy;

@Entity
@Table(name = "taskgroups")
public class TaskGroup implements Serializable {
	private int taskGroupId;
	private String title = "";
	private List<Task> tasks;
	private Lecture lecture;

	public TaskGroup() {}

	public TaskGroup(String title, Lecture lecture) {
		this.title = title;
		this.lecture = lecture;
	}

	/**
	 * @param title
	 */
	public TaskGroup(String title) {
		this.title = title;
	}

	/**
	 * @return the taskGroupId
	 */
	@Id
	@GeneratedValue
	public int getTaskGroupId() {
		return taskGroupId;
	}

	/**
	 * @param taskGroupId the taskGroupId to set
	 */
	public void setTaskGroupId(int taskGroupId) {
		this.taskGroupId = taskGroupId;
	}

	/**
	 * @return the title
	 */
	@Column(nullable = false)
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the tasks
	 */
	@OneToMany(mappedBy = "taskGroup")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy(clause = "taskid asc")
	public List<Task> getTasks() {
		return tasks;
	}

	/**
	 * @param tasks the tasks to set
	 */
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	/**
	 * @return the lecture
	 */

	@ManyToOne
	@JoinColumn(name = "lectureid", nullable = false)
	public Lecture getLecture() {
		return lecture;
	}

	/**
	 * @param lecture the lecture to set
	 */
	public void setLecture(Lecture lecture) {
		this.lecture = lecture;
	}
}
