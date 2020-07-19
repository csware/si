/*
 * Copyright 2009, 2020 Sven Strickroth <email@cs-ware.de>
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
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "logs")
public class LogEntry implements Serializable {
	private int id;
	private int userId;
	private Integer testId;
	private int taskId;
	private Date timeStamp = new Date();
	private int action;
	private Boolean result;
	private String testOutput;
	private String uploadFilename;
	private byte[] upload;

	public LogEntry() {}

	public LogEntry(User user, Test test, Task task, LogAction logAction, Boolean result, String testOutput, String uploadFilename, byte[] upload) {
		action = logAction.ordinal();
		if (logAction.compareTo(LogAction.PERFORMED_TEST) == 0) {
			this.result = result;
			this.testOutput = testOutput;
		}
		this.userId = user.getUid();
		if (test != null) {
			this.testId = test.getId();
		} else {
			this.testId = null;
		}
		this.upload = upload;
		this.uploadFilename = uploadFilename;
		this.taskId = task.getTaskid();
	}

	/**
	 * @return the uid
	 */
	@Id
	@GeneratedValue
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
	public int getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * @return the testId
	 */
	public Integer getTestId() {
		return testId;
	}

	/**
	 * @param testId the testId to set
	 */
	public void setTestId(Integer testId) {
		this.testId = testId;
	}

	/**
	 * @return the taskId
	 */
	public int getTaskId() {
		return taskId;
	}

	/**
	 * @param taskId the taskId to set
	 */
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	/**
	 * @return the upload
	 */
	@Lob
	public byte[] getUpload() {
		return upload;
	}

	/**
	 * @param upload the upload to set
	 */
	public void setUpload(byte[] upload) {
		this.upload = upload;
	}

	/**
	 * @return the uploadFilename
	 */
	public String getUploadFilename() {
		return uploadFilename;
	}

	/**
	 * @param uploadFilename the uploadFilename to set
	 */
	public void setUploadFilename(String uploadFilename) {
		this.uploadFilename = uploadFilename;
	}
}
