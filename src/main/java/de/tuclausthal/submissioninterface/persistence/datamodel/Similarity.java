/*
 * Copyright 2009, 2020 Sven Strickroth <email@cs-ware.de>
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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "similarities")
public class Similarity implements Serializable {
	private static final long serialVersionUID = 1L;

	private int similarityid;
	private Submission submissionOne;
	private Submission submissionTwo;
	private int percentage;
	private SimilarityTest similarityTest;

	// for Hibernate
	private Similarity() {}

	/**
	 * @param similarityTest 
	 * @param submissionOne
	 * @param submissionTwo
	 * @param percentage
	 */
	public Similarity(SimilarityTest similarityTest, Submission submissionOne, Submission submissionTwo, int percentage) {
		this.similarityTest = similarityTest;
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
	@ManyToOne(optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public Submission getSubmissionOne() {
		return submissionOne;
	}

	/**
	 * @return the submissionTwo
	 */
	@ManyToOne(optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
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

	/**
	 * @return the similarityTest
	 */
	@ManyToOne(optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public SimilarityTest getSimilarityTest() {
		return similarityTest;
	}

	/**
	 * @param similarityTest the similarityTest to set
	 */
	public void setSimilarityTest(SimilarityTest similarityTest) {
		this.similarityTest = similarityTest;
	}
}
