/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("1")
public class Student extends User {
	private int matrikelno;
	private String studiengang;

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

	/**
	 * @return the studiengang
	 */
	public String getStudiengang() {
		return studiengang;
	}

	/**
	 * @param studiengang the studiengang to set
	 */
	public void setStudiengang(String studiengang) {
		this.studiengang = studiengang;
	}
}
