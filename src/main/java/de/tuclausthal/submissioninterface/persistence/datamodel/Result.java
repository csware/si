/*
 * Copyright 2011 Giselle Rodriguez
 * Copyright 2011, 2020, 2022 Sven Strickroth <email@cs-ware.de>
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "submissions_results")
public class Result implements Serializable {
	private static final long serialVersionUID = 1L;

	private int resultid;
	private Submission submission;
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
	@OnDelete(action = OnDeleteAction.CASCADE)
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
