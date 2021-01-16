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

import javax.persistence.Transient;

import de.tuclausthal.submissioninterface.persistence.datamodel.Points;

public class SubmissionPointsDTO {
	private int submissionid;
	private int participationid;
	private Integer points;
	private Integer duplicate;
	private int minPointStep;

	/**
	 * @param submissionid
	 * @param participationid
	 * @param points
	 * @param duplicate 
	 * @param minPointStep
	 */
	public SubmissionPointsDTO(int submissionid, int participationid, Integer points, Integer duplicate, int minPointStep) {
		this.submissionid = submissionid;
		this.participationid = participationid;
		this.points = points;
		this.duplicate = duplicate;
		this.minPointStep = minPointStep;
	}

	/**
	 * @return the submissionid
	 */
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
	 * @return the participationid
	 */
	public int getParticipationid() {
		return participationid;
	}

	/**
	 * @param participationid the participationid to set
	 */
	public void setParticipationid(int participationid) {
		this.participationid = participationid;
	}

	/**
	 * @return the points
	 */
	public Integer getPoints() {
		return points;
	}

	/**
	 * @param points the points to set
	 */
	public void setPoints(Integer points) {
		this.points = points;
	}

	/**
	 * @return the minPointStep
	 */
	public int getMinPointStep() {
		return minPointStep;
	}

	/**
	 * @param minPointStep the minPointStep to set
	 */
	public void setMinPointStep(int minPointStep) {
		this.minPointStep = minPointStep;
	}

	/**
	 * @return the duplicate
	 */
	public Integer getDuplicate() {
		return duplicate;
	}

	/**
	 * @param duplicate the duplicate to set
	 */
	public void setDuplicate(Integer duplicate) {
		this.duplicate = duplicate;
	}

	@Transient
	public int getPlagiarismPoints() {
		return Points.getPlagiarismPoints(duplicate, getPoints(), minPointStep);
	}
}
