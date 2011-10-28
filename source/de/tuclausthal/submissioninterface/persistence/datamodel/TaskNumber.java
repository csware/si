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
@Table(name = "submissions_tasknumbers")
public class TaskNumber implements Serializable {
	private int tasknumberid;
	private int taskid;
	private int userid;
	private int submissionid;
	private String number;
	private char type;

	// for Hibernate
	public TaskNumber() {}

	public TaskNumber(int taskid, int userid, String number, char type) {
		this.taskid = taskid;
		this.userid = userid;
		this.submissionid = 0;
		this.number = number;
		this.type = type;
	}

	public TaskNumber(int taskid, int userid, int submissionid, String number, char type) {
		this.taskid = taskid;
		this.userid = userid;
		this.submissionid = submissionid;
		this.number = number;
		this.type = type;
	}

	/**
	 * @return the TasknumberID
	 */
	@Id
	@GeneratedValue
	public int getTasknumberid() {
		return tasknumberid;
	}

	/**
	 * @param tasknumberid the tasknumberid to set
	 */
	public void setTasknumberid(int tasknumberid) {
		this.tasknumberid = tasknumberid;
	}

	/**
	 * @return the taskid
	 */
	public int getTaskid() {
		return taskid;
	}

	/**
	 * @param taskid the taskid to set
	 */
	public void setTaskid(int taskid) {
		this.taskid = taskid;
	}

	/**
	 * @return the userid
	 */
	public int getUserid() {
		return userid;
	}

	/**
	 * @param userid the userid to set
	 */
	public void setUserid(int userid) {
		this.userid = userid;
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
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * @param number the number to set
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * @return the type of number
	 */
	public char getType() {
		return type;
	}

	/**
	 * @param type the type of number to set
	 */
	public void setType(char type) {
		this.type = type;
	}
}
