package de.tuclausthal.abgabesystem.persistence.datamodel;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

@Embeddable
public class Points implements Serializable {
	private Integer points;
	private Participation issuedBy;

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
}
