/*
 * Copyright 2009, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "logs")
public class LogEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private User user;
	private Test test;
	private Task task;
	private Date timeStamp = new Date();
	private int action;
	private Boolean result;
	private String testOutput;
	private String additionalData;

	public LogEntry() {}

	public LogEntry(User user, Test test, Task task, LogAction logAction, Boolean result, String testOutput, String additionalData) {
		action = logAction.ordinal();
		if (logAction.compareTo(LogAction.PERFORMED_TEST) == 0) {
			this.result = result;
			this.testOutput = testOutput;
		}
		this.user = user;
		this.test = test;
		this.additionalData = additionalData;
		this.task = task;
	}

	/**
	 * @return the uid
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId() {
		return id;
	}

	public static enum LogAction {
		UPLOAD, PERFORMED_TEST, DELETE_FILE, UPLOAD_ADMIN
	}

	/**
	 * @return the action
	 */
	public int getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(int action) {
		this.action = action;
	}

	/**
	 * @return the result
	 */
	public Boolean isResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(Boolean result) {
		this.result = result;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the testOutput
	 */
	@Lob
	public String getTestOutput() {
		return testOutput;
	}

	/**
	 * @param testOutput the testOutput to set
	 */
	public void setTestOutput(String testOutput) {
		this.testOutput = testOutput;
	}

	/**
	 * @return the timeStamp
	 */
	public Date getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the userId
	 */
	@ManyToOne
	@JoinColumn(name = "userId", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the test
	 */
	@ManyToOne
	@JoinColumn(name = "testId", nullable = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public Test getTest() {
		return test;
	}

	/**
	 * @param test the test to set
	 */
	public void setTest(Test test) {
		this.test = test;
	}

	/**
	 * @return the task
	 */
	@ManyToOne
	@JoinColumn(name = "taskId", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public Task getTask() {
		return task;
	}

	/**
	 * @param task the task to set
	 */
	public void setTask(Task task) {
		this.task = task;
	}

	/**
	 * @return the additionalData
	 */
	@Lob
	public String getAdditionalData() {
		return additionalData;
	}

	/**
	 * @param additionalData the additionalData to set
	 */
	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}
}
