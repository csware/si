package de.tuclausthal.abgabesystem.persistence.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "test")
public class Test implements Serializable {
	private int id;
	private Boolean visibleToStudents = false;
	private Task task;

	/**
	 * @return the visibleToStudents
	 */
	@Column(nullable = false)
	public Boolean getVisibleToStudents() {
		return visibleToStudents;
	}

	/**
	 * @param visibleToStudents the visibleToStudents to set
	 */
	public void setVisibleToStudents(Boolean visibleToStudents) {
		this.visibleToStudents = visibleToStudents;
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
	@OneToOne
	@JoinColumn(name = "taskid", nullable = false, unique = true)
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
