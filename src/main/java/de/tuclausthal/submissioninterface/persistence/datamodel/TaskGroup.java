/*
 * Copyright 2010, 2020, 2022-2024 Sven Strickroth <email@cs-ware.de>
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
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@Entity
@Table(name = "taskgroups")
public class TaskGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private int taskGroupId;
	@Column(nullable = false)
	private String title = "";
	@OneToMany(mappedBy = "taskGroup", cascade = CascadeType.PERSIST)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy("taskid asc")
	@JacksonXmlElementWrapper(localName = "tasks")
	@JacksonXmlProperty(localName = "task")
	@JsonManagedReference
	private List<Task> tasks;
	@ManyToOne
	@JoinColumn(name = "lectureid", nullable = false)
	@JsonBackReference
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

	public Lecture getLecture() {
		return lecture;
	}

	/**
	 * @param lecture the lecture to set
	 */
	public void setLecture(Lecture lecture) {
		this.lecture = lecture;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): taskgroupid:" + getTaskGroupId() + "; title:" + getTitle() + "; lectureid:" + (getLecture() == null ? "null" : getLecture().getId());
	}
}
