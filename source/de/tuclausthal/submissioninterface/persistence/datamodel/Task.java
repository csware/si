/*
 * Copyright 2009 - 2011 Sven Strickroth <email@cs-ware.de>
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
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Session;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.OrderBy;

import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieFactory;
import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieIf;

@Entity
@Table(name = "tasks")
public class Task implements Serializable {
	private int taskid;
	private String title = "";
	private int maxSubmitters = 1;
	private int maxPoints = 0;
	private int minPointStep = 50;
	private Date start;
	private Date deadline;
	private Date showPoints;
	private String description = "";
	private Set<Submission> submissions;
	private TaskGroup taskGroup;
	private Set<PointCategory> pointCategories;
	private Set<Test> tests;
	private Set<SimilarityTest> simularityTests;
	private String filenameRegexp = "[A-Z][A-Za-z0-9_]+\\.java";
	private String archiveFilenameRegexp = "-";
	private boolean showTextArea = false;
	private String featuredFiles = "";
	private boolean tutorsCanUploadFiles = false;
	private boolean allowSubmittersAcrossGroups = false;
	private String dynamicTask = null;

	public Task() {}

	/**
	 * @param title
	 * @param maxPoints
	 * @param minPointStep
	 * @param start
	 * @param deadline
	 * @param description
	 * @param taskGroup
	 * @param showPoints 
	 * @param filenameRegexp 
	 * @param archiveFilenameRegexp 
	 * @param showTextArea 
	 * @param featuredFiles 
	 * @param tutorsCanUploadFiles 
	 * @param maxSubmitters 
	 * @param allowSubmittersAcrossGroups 
	 * @param dynamicTask 
	 */
	public Task(String title, int maxPoints, int minPointStep, Date start, Date deadline, String description, TaskGroup taskGroup, Date showPoints, String filenameRegexp, String archiveFilenameRegexp, boolean showTextArea, String featuredFiles, boolean tutorsCanUploadFiles, int maxSubmitters, boolean allowSubmittersAcrossGroups, String dynamicTask) {
		this.title = title;
		this.maxPoints = maxPoints;
		this.minPointStep = minPointStep;
		this.start = start;
		this.deadline = deadline;
		this.description = description;
		this.taskGroup = taskGroup;
		this.showPoints = showPoints;
		this.setFilenameRegexp(filenameRegexp);
		this.setArchiveFilenameRegexp(archiveFilenameRegexp);
		this.showTextArea = showTextArea;
		this.featuredFiles = featuredFiles;
		this.tutorsCanUploadFiles = tutorsCanUploadFiles;
		this.maxSubmitters = maxSubmitters;
		this.allowSubmittersAcrossGroups = allowSubmittersAcrossGroups;
		this.dynamicTask = dynamicTask;
	}

	/**
	 * @return the taskid
	 */
	@Id
	@GeneratedValue
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
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the maxPoints
	 */
	@Column(nullable = false)
	public int getMaxPoints() {
		return maxPoints;
	}

	/**
	 * @param maxPoints the maxPoints to set
	 */
	public void setMaxPoints(int maxPoints) {
		this.maxPoints = maxPoints;
	}

	/**
	 * @return the start
	 */
	@Column(nullable = false)
	public Date getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(Date start) {
		this.start = start;
	}

	/**
	 * @return the deadline
	 */
	@Column(nullable = false)
	public Date getDeadline() {
		return deadline;
	}

	/**
	 * @param deadline the deadline to set
	 */
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	/**
	 * @return the description
	 */
	@Lob
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the submissions
	 */
	@OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy(clause = "submissionid asc")
	public Set<Submission> getSubmissions() {
		return submissions;
	}

	/**
	 * @param submissions the submissions to set
	 */
	public void setSubmissions(Set<Submission> submissions) {
		this.submissions = submissions;
	}

	/**
	 * @return the taskGroup
	 */
	@ManyToOne
	@JoinColumn(name = "taskgroupid", nullable = false)
	public TaskGroup getTaskGroup() {
		return taskGroup;
	}

	/**
	 * @param taskGroup the taskGroup to set
	 */
	public void setTaskGroup(TaskGroup taskGroup) {
		this.taskGroup = taskGroup;
	}

	/**
	 * @return the test
	 */
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@OrderBy(clause = "id asc")
	public Set<Test> getTests() {
		return tests;
	}

	/**
	 * @param tests 
	 */
	public void setTests(Set<Test> tests) {
		this.tests = tests;
	}

	/**
	 * @return the showPoints
	 */
	public Date getShowPoints() {
		return showPoints;
	}

	/**
	 * @param showPoints the showPoints to set
	 */
	public void setShowPoints(Date showPoints) {
		this.showPoints = showPoints;
	}

	/**
	 * @return the simularityTests
	 */
	@OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy(clause = "similarityTestId asc")
	public Set<SimilarityTest> getSimularityTests() {
		return simularityTests;
	}

	/**
	 * @param simularityTests the simularityTests to set
	 */
	public void setSimularityTests(Set<SimilarityTest> simularityTests) {
		this.simularityTests = simularityTests;
	}

	/**
	 * @return the filenameRegexp
	 */
	public String getFilenameRegexp() {
		return filenameRegexp;
	}

	/**
	 * @param filenameRegexp the filenameRegexp to set
	 */
	public void setFilenameRegexp(String filenameRegexp) {
		try {
			Pattern.compile(filenameRegexp);
			this.filenameRegexp = filenameRegexp;
		} catch (PatternSyntaxException e) {
			this.filenameRegexp = "";
		}
	}

	/**
	 * @return the showTextArea
	 */
	@Column(nullable = false)
	public boolean isShowTextArea() {
		return showTextArea;
	}

	/**
	 * @param showTextArea the showTextArea to set
	 */
	public void setShowTextArea(boolean showTextArea) {
		this.showTextArea = showTextArea;
	}

	/**
	 * @return the maxSubmitters per "group"
	 */
	public int getMaxSubmitters() {
		return maxSubmitters;
	}

	/**
	 * @param maxSubmitters per "group" 
	 */
	public void setMaxSubmitters(int maxSubmitters) {
		this.maxSubmitters = maxSubmitters;
	}

	/**
	 * @return the featuredFiles
	 */
	@Column(nullable = false)
	public String getFeaturedFiles() {
		return featuredFiles;
	}

	/**
	 * @param featuredFiles the featuredFiles to set
	 */
	public void setFeaturedFiles(String featuredFiles) {
		try {
			Pattern.compile(featuredFiles);
			this.featuredFiles = featuredFiles;
		} catch (PatternSyntaxException e) {
			this.featuredFiles = "";
		}
	}

	/**
	 * @return the tutorsCanUploadFiles
	 */
	@Column(nullable = false)
	public boolean isTutorsCanUploadFiles() {
		return tutorsCanUploadFiles;
	}

	/**
	 * @param tutorsCanUploadFiles the tutorsCanUploadFiles to set
	 */
	public void setTutorsCanUploadFiles(boolean tutorsCanUploadFiles) {
		this.tutorsCanUploadFiles = tutorsCanUploadFiles;
	}

	/**
	 * @return the pointCategories
	 */
	@OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy(clause = "pointcatid asc")
	public Set<PointCategory> getPointCategories() {
		return pointCategories;
	}

	/**
	 * @param pointCategories the pointCategories to set
	 */
	public void setPointCategories(Set<PointCategory> pointCategories) {
		this.pointCategories = pointCategories;
	}

	/**
	 * @return the archiveFilenameRegexp
	 */
	public String getArchiveFilenameRegexp() {
		return archiveFilenameRegexp;
	}

	/**
	 * @param archiveFilenameRegexp the archiveFilenameRegexp to set
	 */
	public void setArchiveFilenameRegexp(String archiveFilenameRegexp) {
		try {
			Pattern.compile(archiveFilenameRegexp);
			this.archiveFilenameRegexp = archiveFilenameRegexp;
		} catch (PatternSyntaxException e) {
			this.archiveFilenameRegexp = "-";
		}
	}

	/**
	 * @return the minPointStep
	 */
	public int getMinPointStep() {
		return minPointStep;
	}

	/**
	 * @param minPointStep the minPointStep to set
	 */
	public void setMinPointStep(int minPointStep) {
		this.minPointStep = minPointStep;
	}

	/**
	 * @return the allowSubmittersAcrossGroups
	 */
	public boolean isAllowSubmittersAcrossGroups() {
		return allowSubmittersAcrossGroups;
	}

	/**
	 * @param allowSubmittersAcrossGroups the allowSubmittersAcrossGroups to set
	 */
	public void setAllowSubmittersAcrossGroups(boolean allowSubmittersAcrossGroups) {
		this.allowSubmittersAcrossGroups = allowSubmittersAcrossGroups;
	}

	/**
	 * Checks if the task has a valid dynamic task attached
	 * @return true/false
	 */
	@Transient
	public boolean isADynamicTask() {
		return DynamicTaskStrategieFactory.IsValidStrategieName(dynamicTask);
	}

	/**
	 * @return the dynamicTask
	 */
	public String getDynamicTask() {
		return dynamicTask;
	}

	/**
	 * @param dynamicTask the dynamicTask to set
	 */
	public void setDynamicTask(String dynamicTask) {
		this.dynamicTask = dynamicTask;
	}

	/**
	 * Returns the dynamic task strategie
	 * @param session
	 * @return the dynamicTask Strategie or null
	 */
	@Transient
	public DynamicTaskStrategieIf getDynamicTaskStrategie(Session session) {
		return DynamicTaskStrategieFactory.createDynamicTaskStrategie(session, dynamicTask, this);
	}
}
