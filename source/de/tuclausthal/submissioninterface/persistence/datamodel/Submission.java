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
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.OrderBy;

@Entity
@Table(name = "submissions", uniqueConstraints = { @UniqueConstraint(columnNames = { "submitter", "taskid" }) })
public class Submission implements Serializable {
	private int submissionid;
	private Task task;
	private Participation submitter;
	private Points points;
	private Set<TestResult> testResults;
	private Set<Similarity> similarSubmissions;

	// for Hibernate
	private Submission() {}

	/**
	 * @param task
	 * @param submitter
	 */
	public Submission(Task task, Participation submitter) {
		this.task = task;
		this.submitter = submitter;
	}

	/**
	 * @return the testResult
	 */
	@OneToMany(mappedBy = "submission", fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy(clause = "id asc")
	public Set<TestResult> getTestResults() {
		return testResults;
	}

	/**
	 * @param testResult the testResult to set
	 */
	public void setTestResults(Set<TestResult> testResults) {
		this.testResults = testResults;
	}

	/**
	 * @return the points
	 */
	public Points getPoints() {
		return points;
	}

	/**
	 * @param points the points to set
	 */
	public void setPoints(Points points) {
		this.points = points;
	}

	/**
	 * @return the submitter
	 */
	@ManyToOne
	@JoinColumn(name = "submitter", nullable = false)
	public Participation getSubmitter() {
		return submitter;
	}

	/**
	 * @param submitter the submitter to set
	 */
	public void setSubmitter(Participation submitter) {
		this.submitter = submitter;
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
	 * @return the submissionid
	 */
	@Id
	@GeneratedValue
	public int getSubmissionid() {
		return submissionid;
	}

	/**
	 * @param submissionid the submissionid to set
	 */
	public void setSubmissionid(int submissionid) {
		this.submissionid = submissionid;
	}

	/**
	 * @return the similarSubmissions
	 */
	@OneToMany(mappedBy = "submissionOne", fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public Set<Similarity> getSimilarSubmissions() {
		return similarSubmissions;
	}

	/**
	 * @param similarSubmissions the similarSubmissions to set
	 */
	public void setSimilarSubmissions(Set<Similarity> similarSubmissions) {
		this.similarSubmissions = similarSubmissions;
	}
}
