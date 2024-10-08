/*
 * Copyright 2010, 2020, 2022-2024 Sven Strickroth <email@cs-ware.de>
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "pointgiven")
public class PointGiven implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int pointgivenid;
	@ManyToOne
	@JoinColumn(name = "categoryid", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private PointCategory category;
	@ManyToOne
	@JoinColumn(name = "submissionid", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
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
	public PointCategory getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(PointCategory category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): pointgivenid:" + getPointgivenid() + "; points:" + getPoints() + "; categoryid:" + (getCategory() == null ? "null" : getCategory().getPointcatid()) + "; submissionid:" + (getSubmission() == null ? "null" : getSubmission().getSubmissionid());
	}
}
