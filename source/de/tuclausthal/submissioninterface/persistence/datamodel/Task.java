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
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.OrderBy;

@Entity
@Table(name = "tasks")
public class Task implements Serializable {
	private int taskid;
	private String title = "";
	private int maxPoints = 0;
	private Date start;
	private Date deadline;
	private Date showPoints;
	private String description = "";
	private Set<Submission> submissions;
	private Lecture lecture;
	private Test test;
	private Set<SimilarityTest> simularityTests;

	public Task() {}

	/**
	 * @param title
	 * @param maxPoints
	 * @param start
	 * @param deadline
	 * @param description
	 * @param submissions
	 * @param lecture
	 */
	public Task(String title, int maxPoints, Date start, Date deadline, String description, Lecture lecture, Date showPoints) {
		this.title = title;
		this.maxPoints = maxPoints;
		this.start = start;
		this.deadline = deadline;
		this.description = description;
		this.lecture = lecture;
		this.showPoints = showPoints;
	}

	/**
	 * @return the taskid
	 */
	@Id
	@GeneratedValue
	public int getTaskid() {
		return taskid;
	}

	/**
	 * @param taskid the taskid to set
	 */
	public void setTaskid(int taskid) {
		this.taskid = taskid;
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
	 * @return the maxPoints
	 */
	@Column(nullable = false)
	public int getMaxPoints() {
		return maxPoints;
	}

	/**
	 * @param maxPoints the maxPoints to set
	 */
	public void setMaxPoints(int maxPoints) {
		this.maxPoints = maxPoints;
	}

	/**
	 * @return the start
	 */
	@Column(nullable = false)
	public Date getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(Date start) {
		this.start = start;
	}

	/**
	 * @return the deadline
	 */
	@Column(nullable = false)
	public Date getDeadline() {
		return deadline;
	}

	/**
	 * @param deadline the deadline to set
	 */
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	/**
	 * @return the description
	 */
	@Lob
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the submissions
	 */
	@OneToMany(mappedBy = "task")
	@OnDelete(action = OnDeleteAction.CASCADE)
	public Set<Submission> getSubmissions() {
		return submissions;
	}

	/**
	 * @param submissions the submissions to set
	 */
	public void setSubmissions(Set<Submission> submissions) {
		this.submissions = submissions;
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

	/**
	 * @return the test
	 */
	@OneToOne(mappedBy = "task", cascade = CascadeType.ALL)
	public Test getTest() {
		return test;
	}

	/**
	 * @param test the test to set
	 */
	public void setTest(Test test) {
		this.test = test;
	}

	/**
	 * @return the showPoints
	 */
	public Date getShowPoints() {
		return showPoints;
	}

	/**
	 * @param showPoints the showPoints to set
	 */
	public void setShowPoints(Date showPoints) {
		this.showPoints = showPoints;
	}

	/**
	 * @return the simularityTests
	 */
	@OneToMany(mappedBy = "task")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy(clause = "similarityTestId")
	public Set<SimilarityTest> getSimularityTests() {
		return simularityTests;
	}

	/**
	 * @param simularityTests the simularityTests to set
	 */
	public void setSimularityTests(Set<SimilarityTest> simularityTests) {
		this.simularityTests = simularityTests;
	}
}
