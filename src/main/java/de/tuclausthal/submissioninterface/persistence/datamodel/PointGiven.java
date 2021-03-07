/*
 * Copyright 2010, 2020 Sven Strickroth <email@cs-ware.de>
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
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "pointgiven")
public class PointGiven implements Serializable {
	private static final long serialVersionUID = 1L;

	private int pointgivenid;
	private PointCategory category;
	private Submission submission;
	private int points;

	// for Hibernate
	protected PointGiven() {}

	public PointGiven(int issuedPoints, Submission submission, PointCategory category) {
		this.points = issuedPoints;
		this.submission = submission;
		this.category = category;
	}

	/**
	 * @return the pointgivenid
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getPointgivenid() {
		return pointgivenid;
	}

	/**
	 * @param pointgivenid the pointgivenid to set
	 */
	public void setPointgivenid(int pointgivenid) {
		this.pointgivenid = pointgivenid;
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

	/**
	 * @return the points
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * @param points the points to set
	 */
	public void setPoints(int points) {
		this.points = points;
	}

	/**
	 * @return the category
	 */
	@ManyToOne
	@JoinColumn(name = "categoryid", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public PointCategory getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(PointCategory category) {
		this.category = category;
	}
}
