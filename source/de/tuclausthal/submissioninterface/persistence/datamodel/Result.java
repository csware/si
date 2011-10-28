/*
 * Copyright 2011 Giselle Rodriguez
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
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "submissions_results")
public class Result implements Serializable {
	private int resultid;
	private String result;

	// for Hibernate
	private Result() {}

	/**
	 * @param result
	 */
	public Result(String result) {
		this.result = result;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @param result 
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @return the resultID
	 */
	@Id
	@GeneratedValue
	public int getResultid() {
		return resultid;
	}

	/**
	 * @param resultid the resultid to set
	 */
	public void setResultid(int resultid) {
		this.resultid = resultid;
	}
}
