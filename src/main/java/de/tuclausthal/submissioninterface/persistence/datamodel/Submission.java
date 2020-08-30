/*
 * Copyright 2009-2010, 2017, 2020 Sven Strickroth <email@cs-ware.de>
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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import de.tuclausthal.submissioninterface.util.Util;

@Entity
@Table(name = "submissions")
public class Submission implements Serializable {
	private int submissionid;
	private Task task;
	private Set<Participation> submitters = new HashSet<>();
	private Points points;
	private Set<TestResult> testResults;
	private Set<Similarity> similarSubmissions;
	private Date lastModified = null;
	private Date closedTime = null;
	private Participation closedBy= null;

	// for Hibernate
	private Submission() {}

	/**
	 * @param task
	 * @param submitter
	 */
	public Submission(Task task, Participation submitter) {
		this.task = task;
		this.submitters.add(submitter);
	}

	/**
	 * @return the testResult
	 */
	@OneToMany(mappedBy = "submission", fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy(value = "id asc")
	public Set<TestResult> getTestResults() {
		return testResults;
	}

	/**
	 * @param testResults 
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

	@Transient
	public boolean isPointsVisibleToStudents() {
		if (getTask().getShowPoints() != null && getTask().getShowPoints().before(Util.correctTimezone(new Date())) && getPoints() != null && getPoints().getPointStatus() > Points.PointStatus.NICHT_BEWERTET.ordinal()) {
			return true;
		}
		return false;
	}

	/**
	 * @param points the points to set
	 */
	public void setPoints(Points points) {
		this.points = points;
	}

	/**
	 * @return the submitters
	 */
	@ManyToMany
	@OrderBy(value = "user asc")
	public Set<Participation> getSubmitters() {
		return submitters;
	}

	/**
	 * @param submitters the submitters to set
	 */
	public void setSubmitters(Set<Participation> submitters) {
		this.submitters = submitters;
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

	@Transient
	public String getSubmitterNames() {
		StringBuffer sb = new StringBuffer();
		for (Participation submitter : getSubmitters()) {
			sb.append("; " + submitter.getUser().getFullName());
		}
		return sb.substring(2).toString();
	}

	/**
	 * @return the lastModified
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Returns the time, when the students closed the solution as final.
	 *
	 * @return time of the final submission
	 */
	public Date getClosedTime() {
		return closedTime;
	}

	/**
	 * Sets the time, when the submission was closed by the students.
	 *
	 * @param closedTime time
	 */
	public void setClosedTime(Date closedTime) {
		this.closedTime = closedTime;
	}

	/**
	 * Returns the user id of the student who close the submission.
	 *
	 * @return user id of the student
	 */
	@ManyToOne
	@JoinColumn(name="closedBy")
	public Participation getClosedBy() {
		return closedBy;
	}

	/**
	 * Sets the user, who finally closed the submission.
	 *
	 * @param closedBy user participation
	 */
	public void setClosedBy(Participation closedBy) {
		this.closedBy = closedBy;
	}

	/**
	 * Returns the flag, if the submission is prematurely closed by the students
	 *
	 * @return final submission flag
	 */
	@Transient
	public boolean isClosed() {
		return getClosedBy() != null;
	}
}
