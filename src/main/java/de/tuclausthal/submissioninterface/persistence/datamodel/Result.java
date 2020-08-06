/*
 * Copyright 2011 Giselle Rodriguez
 * Copyright 2011 Sven Strickroth <email@cs-ware.de>
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "submissions_results")
public class Result implements Serializable {
	private int resultid;
	private Submission submission;
	private String result;

	// for Hibernate
	private Result() {}

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
	@Column(nullable = false)
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
	@Id
	@GeneratedValue
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
	@ManyToOne
	@JoinColumn(name = "submissionid", nullable = false)
	public Submission getSubmission() {
		return submission;
	}

	/**
	 * @param submission the submission to set
	 */
	public void setSubmission(Submission submission) {
		this.submission = submission;
	}
}
