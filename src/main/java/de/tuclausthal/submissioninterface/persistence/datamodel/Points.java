/*
 * Copyright 2009-2011, 2017, 2020 Sven Strickroth <email@cs-ware.de>
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
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

@Embeddable
public class Points implements Serializable {
	private Integer points;
	private Integer pointStatus; // 0 = ungraded, 1 = nicht abgenommen, 2 = abnahme nicht bestanden, 3 = abgenommen
	private Participation issuedBy;
	private String publicComment;
	private String internalComment;
	private Integer duplicate;

	/**
	 * @return the points
	 */
	public Integer getPoints() {
		return points;
	}

	/**
	 * @return the points
	 */
	@Transient
	public Integer getPointsByStatus(int minPointStep) {
		if (pointStatus <= PointStatus.ABGENOMMEN_FAILED.ordinal()) {
			return 0;
		}
		return getPlagiarismPoints(minPointStep);
	}

	/**
	 * @param points the points to set
	 */
	public void setPoints(Integer points) {
		this.points = points;
	}

	/**
	 * @return the issuedBy
	 */
	@ManyToOne
	@ForeignKey(name = "issuedby")
	public Participation getIssuedBy() {
		return issuedBy;
	}

	/**
	 * @param issuedBy the issuedBy to set
	 */
	public void setIssuedBy(Participation issuedBy) {
		this.issuedBy = issuedBy;
	}

	/**
	 * @return the comment
	 */
	@Lob
	public String getPublicComment() {
		return publicComment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setPublicComment(String comment) {
		this.publicComment = comment;
	}

	/**
	 * @return the pointsOk
	 */
	@Transient
	public Boolean getPointsOk() {
		return (pointStatus >= PointStatus.ABGENOMMEN_FAILED.ordinal());
	}

	/**
	 * @return the internalComment
	 */
	@Lob
	public String getInternalComment() {
		return internalComment;
	}

	/**
	 * @param internalComment the internalComment to set
	 */
	public void setInternalComment(String internalComment) {
		this.internalComment = internalComment;
	}

	/**
	 * @return the isDupe
	 */
	public Integer getDuplicate() {
		return duplicate;
	}

	/**
	 * @param duplicate null = no dupe, 0 = no points, 1 = for historic reasons, other positive values: divisor for points
	 */
	public void setDuplicate(Integer duplicate) {
		if (duplicate != null && duplicate < 0) {
			duplicate = null;
		}
		this.duplicate = duplicate;
	}

	@Transient
	public int getPlagiarismPoints(int minPointStep) {
		if (duplicate == null) {
			return getPoints();
		} else if (duplicate == 0) {
			return 0;
		} else {
			int divided = getPoints() / duplicate;
			if (divided % minPointStep != 0) {
				return minPointStep * (divided / minPointStep);
			}
			return divided;
		}
	}

	/**
	 * @return the pointStatus
	 */
	@Column(columnDefinition="TINYINT")
	public Integer getPointStatus() {
		return pointStatus;
	}

	/**
	 * @param pointStatus the pointStatus to set
	 */
	private void setPointStatus(Integer pointStatus) {
		this.pointStatus = pointStatus;
	}

	/**
	 * @param pointStatus the pointStatus to set
	 */
	@Transient
	public void setPointStatus(PointStatus pointStatus) {
		setPointStatus(pointStatus.ordinal());
	}

	public static enum PointStatus {
		NICHT_BEWERTET, NICHT_ABGENOMMEN, ABGENOMMEN_FAILED, ABGENOMMEN
	}
}
