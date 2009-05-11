package de.tuclausthal.abgabesystem.persistence.datamodel;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
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

@Entity
@Table(name = "submissions", uniqueConstraints = { @UniqueConstraint(columnNames = { "submitter", "taskid" }) })
public class Submission implements Serializable {
	private int submissionid;
	private Boolean compiles = false;
	private String stderr;
	private Task task;
	private Participation submitter;
	private Points points;
	private TestResult testResult;
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
	public TestResult getTestResult() {
		return testResult;
	}

	/**
	 * @param testResult the testResult to set
	 */
	public void setTestResult(TestResult testResult) {
		this.testResult = testResult;
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
	 * @return the compiles
	 */
	@Column(nullable = false)
	public Boolean getCompiles() {
		return compiles;
	}

	/**
	 * @param compiles the compiles to set
	 */
	public void setCompiles(Boolean compiles) {
		this.compiles = compiles;
	}

	/**
	 * @return the stderr
	 */
	@Lob
	public String getStderr() {
		return stderr;
	}

	/**
	 * @param stderr the stderr to set
	 */
	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

	/**
	 * @return the similarSubmissions
	 */
	@OneToMany(mappedBy="submissionOne")
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
