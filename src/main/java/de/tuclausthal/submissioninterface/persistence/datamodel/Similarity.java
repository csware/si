/*
 * Copyright 2009, 2020-2023 Sven Strickroth <email@cs-ware.de>
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

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "similarities", uniqueConstraints = @UniqueConstraint(columnNames = { "similarityTest_similarityTestId", "submissionOne_submissionid", "submissionTwo_submissionid" }))
public class Similarity implements Serializable {
	private static final long serialVersionUID = 1L;

	private int similarityid;
	private Submission submissionOne;
	private Submission submissionTwo;
	private int percentage;
	private SimilarityTest similarityTest;

	// for Hibernate
	protected Similarity() {}

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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
	protected void setSubmissionOne(Submission submissionOne) {
		this.submissionOne = submissionOne;
	}

	/**
	 * @param submissionTwo the submissionTwo to set
	 */
	protected void setSubmissionTwo(Submission submissionTwo) {
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

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): similarityid:" + getSimilarityid() + "; similarityTestId:" + (getSimilarityTest() == null ? "null" : getSimilarityTest().getSimilarityTestId()) + "; submissionOne:" + (getSubmissionOne() == null ? "null" : getSubmissionOne().getSubmissionid()) + "; submissionTwo:" + (getSubmissionTwo() == null ? "null" : getSubmissionTwo().getSubmissionid()) + "; percent:" + getPercentage();
	}
}
