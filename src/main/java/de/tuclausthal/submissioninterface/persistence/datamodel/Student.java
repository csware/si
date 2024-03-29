/*
 * Copyright 2009, 2020, 2022-2023 Sven Strickroth <email@cs-ware.de>
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

import java.lang.invoke.MethodHandles;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("1")
public class Student extends User {
	private static final long serialVersionUID = 1L;

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

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): userid:" + getUid() + "; username:" + getUsername();
	}
}
