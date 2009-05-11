package de.tuclausthal.abgabesystem.persistence.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "similarities")
public class Similarity implements Serializable {
	private int similarityid;
	private Submission submissionOne;
	private Submission submissionTwo;
	private int percentage;

	// for Hibernate
	private Similarity() {}

	/**
	 * @param submissionOne
	 * @param submissionTwo
	 * @param percentage
	 */
	public Similarity(Submission submissionOne, Submission submissionTwo, int percentage) {
		this.submissionOne = submissionOne;
		this.submissionTwo = submissionTwo;
		this.percentage = percentage;
	}

	/**
	 * @return the similarityid
	 */
	@Id
	@GeneratedValue
	public int getSimilarityid() {
		return similarityid;
	}

	/**
	 * @param similarityid the similarityid to set
	 */
	public void setSimilarityid(int similarityid) {
		this.similarityid = similarityid;
	}

	/**
	 * @return the percentage
	 */
	@Column(nullable = false)
	public int getPercentage() {
		return percentage;
	}

	/**
	 * @param percentage the percentage to set
	 */
	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}

	/**
	 * @return the submissionOne
	 */
	@ManyToOne(optional=false)
	public Submission getSubmissionOne() {
		return submissionOne;
	}

	/**
	 * @return the submissionTwo
	 */
	@ManyToOne(optional=false)
	public Submission getSubmissionTwo() {
		return submissionTwo;
	}

	/**
	 * @param submissionOne the submissionOne to set
	 */
	private void setSubmissionOne(Submission submissionOne) {
		this.submissionOne = submissionOne;
	}

	/**
	 * @param submissionTwo the submissionTwo to set
	 */
	private void setSubmissionTwo(Submission submissionTwo) {
		this.submissionTwo = submissionTwo;
	}
}
