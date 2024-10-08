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
@Table(name = "submissions_results")
public class Result implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int resultid;
	@ManyToOne
	@JoinColumn(name = "submissionid", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Submission submission;
	@Column(nullable = false)
	private String result;

	// for Hibernate
	protected Result() {}

	/**
	 * @param submission 
	 * @param result
	 */
	public Result(Submission submission, String result) {
		this.submission = submission;
		this.result = result;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @param result 
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @return the resultID
	 */
	public int getResultid() {
		return resultid;
	}

	/**
	 * @param resultid the resultid to set
	 */
	public void setResultid(int resultid) {
		this.resultid = resultid;
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

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): resultid:" + getResultid() + "; submissionid:" + (getSubmission() == null ? "null" : getSubmission().getSubmissionid());
	}
}
