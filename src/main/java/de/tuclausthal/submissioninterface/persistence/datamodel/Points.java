/*
 * Copyright 2009-2011, 2017, 2020-2024 Sven Strickroth <email@cs-ware.de>
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
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

@Embeddable
public class Points implements Serializable {
	private static final long serialVersionUID = 1L;

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
	 * @param minPointStep 
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
	@JoinColumn(name = "issuedby_id")
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
	@Column(length = 65536)
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
	@Column(length = 65536)
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
		return getPlagiarismPoints(duplicate, getPoints(), minPointStep);
	}

	public static int getPlagiarismPoints(Integer duplicate, int points, int minPointStep) {
		if (duplicate == null) {
			return points;
		} else if (duplicate == 0) {
			return 0;
		} else {
			int divided = points / duplicate;
			if (divided % minPointStep != 0) {
				return minPointStep * (divided / minPointStep);
			}
			return divided;
		}
	}

	/**
	 * @return the pointStatus
	 */
	@Column(columnDefinition = "TINYINT")
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

	@Transient
	public PointStatus getTypedPointStatus() {
		return PointStatus.values()[pointStatus];
	}

	public enum PointStatus {
		NICHT_BEWERTET, NICHT_ABGENOMMEN, ABGENOMMEN_FAILED, ABGENOMMEN
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): points:" + getPoints() + "; pointstatus:" + getTypedPointStatus() + "; issuedBy:" + (getIssuedBy() == null ? "null" : getIssuedBy().getId());
	}
}
