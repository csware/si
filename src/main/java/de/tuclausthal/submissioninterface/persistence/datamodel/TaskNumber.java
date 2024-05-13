/*
 * Copyright 2011 Giselle Rodriguez
 * Copyright 2011, 2020, 2022-2024 Sven Strickroth <email@cs-ware.de>
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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "submissions_tasknumbers")
public class TaskNumber implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int tasknumberid;
	@ManyToOne
	@JoinColumn(name = "taskid", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Task task;
	@ManyToOne
	@JoinColumn(name = "participationid", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Participation participation;
	@ManyToOne
	@JoinColumn(name = "submissionid") // on delete: set NULL
	private Submission submission = null;
	@Column(nullable = false)
	private String number;
	@Column(nullable = false)
	private String origNumber;

	// for Hibernate
	public TaskNumber() {}

	public TaskNumber(Task task, Participation participation, String number, String origNumber) {
		this(task, participation, null, number, origNumber);
	}

	public TaskNumber(Task task, Participation participation, Submission submission, String number, String origNumber) {
		this.task = task;
		this.participation = participation;
		this.submission = submission;
		this.number = number;
		this.origNumber = origNumber;
	}

	/**
	 * @return the TasknumberID
	 */
	public int getTasknumberid() {
		return tasknumberid;
	}

	/**
	 * @param tasknumberid the tasknumberid to set
	 */
	public void setTasknumberid(int tasknumberid) {
		this.tasknumberid = tasknumberid;
	}

	/**
	 * @return the task
	 */
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
	 * @return the participation
	 */
	public Participation getParticipation() {
		return participation;
	}

	/**
	 * @param participation the participation to set
	 */
	public void setParticipation(Participation participation) {
		this.participation = participation;
	}

	/**
	 * @return the submission
	 */
	public Submission getSubmission() {
		return submission;
	}

	/**
	 * @param submission the submission to set
	 */
	public void setSubmission(Submission submission) {
		this.submission = submission;
	}

	/**
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * @param number the number to set
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * @param origNumber the origNumber to set
	 */
	public void setOrigNumber(String origNumber) {
		this.origNumber = origNumber;
	}

	/**
	 * @return the origNumber
	 */
	public String getOrigNumber() {
		return origNumber;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): tasknumberid:" + getTasknumberid() + "; participationid:" + (getParticipation() == null ? "null" : getParticipation().getId()) + "; taskid:" + (getTask() == null ? "null" : getTask().getTaskid()) + "; submissionid:" + (getSubmission() == null ? "null" : getSubmission().getSubmissionid()) + "; number:" + getNumber() + "; origNumber:" + getOrigNumber();
	}
}
