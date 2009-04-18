package de.tuclausthal.abgabesystem.persistence.datamodel;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("1")
public class Student extends User {
	private int matrikelno;

	/**
	 * @return the matrikelno
	 */
	public int getMatrikelno() {
		return matrikelno;
	}

	/**
	 * @param matrikelno the matrikelno to set
	 */
	public void setMatrikelno(int matrikelno) {
		this.matrikelno = matrikelno;
	}
}
