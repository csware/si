/*
 * Copyright 2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dto;

import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

public class SubmissionAssignPointsDTO {
	private Submission submission;
	private Participation participation;
	private List<Integer> pointCategories;
	private Points points;

	/**
	 * @param submission
	 * @param participation
	 * @param points
	 */
	public SubmissionAssignPointsDTO(Submission submission, Participation participation, Points points) {
		this.submission = submission;
		this.participation = participation;
		this.points = points;
	}

	/**
	 * @param submission
	 * @param participation
	 * @param points
	 * @param pointcategories
	 */
	public SubmissionAssignPointsDTO(Submission submission, Participation participation, Points points, List<Integer> pointcategories) {
		this(submission, participation, points);
		this.pointCategories = pointcategories;
	}

	/**
	 * @return the submission
	 */
	public Submission getSubmission() {
		return submission;
	}

	/**
	 * @return the points
	 */
	public Points getPoints() {
		return points;
	}

	/**
	 * @return the participation
	 */
	public Participation getParticipation() {
		return participation;
	}

	/**
	 * @return the pointCategories
	 */
	public List<Integer> getPointCategories() {
		return pointCategories;
	}
}
