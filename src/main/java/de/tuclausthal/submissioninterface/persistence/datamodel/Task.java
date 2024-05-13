/*
 * Copyright 2009-2012, 2020-2024 Sven Strickroth <email@cs-ware.de>
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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.Session;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieFactory;
import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieIf;
import de.tuclausthal.submissioninterface.persistence.TaskDescriptionSerializer;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

@Entity
@Table(name = "tasks")
public class Task implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private int taskid;
	@Column(nullable = false)
	private String title = "";
	private int maxSubmitters = 1;
	@JacksonXmlProperty(localName = "maxSize")
	private int maxsize = 10485760;
	private int maxPoints = 0;
	private int minPointStep = 50;
	@Column(nullable = false)
	private ZonedDateTime start;
	@Column(nullable = false)
	private ZonedDateTime deadline;
	private ZonedDateTime showPoints;
	@Column(nullable = false, length = 16777215)
	@JsonSerialize(using = TaskDescriptionSerializer.class)
	private String description = "";
	@OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy("submissionid asc")
	@JsonIgnore
	private Set<Submission> submissions;
	@ManyToOne
	@JoinColumn(name = "taskgroupid", nullable = false)
	@JsonBackReference
	private TaskGroup taskGroup;
	@OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy("pointcatid asc")
	@JacksonXmlElementWrapper(localName = "pointCategories")
	@JacksonXmlProperty(localName = "pointCategory")
	@JsonManagedReference
	private List<PointCategory> pointCategories;
	@OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy("id asc")
	@JacksonXmlElementWrapper(localName = "tests")
	@JacksonXmlProperty(localName = "test")
	@JsonManagedReference
	private List<Test> tests;
	@OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy("similarityTestId asc")
	@JacksonXmlElementWrapper(localName = "similarityTests")
	@JacksonXmlProperty(localName = "similarityTest")
	@JsonManagedReference
	private List<SimilarityTest> similarityTests;
	@Column(nullable = false)
	private String filenameRegexp = "[A-Z][A-Za-z0-9_]+\\.java";
	@Column(nullable = false)
	private String archiveFilenameRegexp = "-";
	@Column(nullable = false)
	private String showTextArea = "-";
	@Column(nullable = false, columnDefinition = "TEXT")
	private String featuredFiles = "";
	private boolean tutorsCanUploadFiles = false;
	private boolean allowSubmittersAcrossGroups = false;
	private String dynamicTask = null;
	@JsonIgnore
	private String modelSolutionProvision = null;
	private String type = "";
	private boolean allowPrematureSubmissionClosing = false;

	public Task() {}

	/**
	 * @param title
	 * @param maxPoints
	 * @param start
	 * @param deadline
	 * @param description
	 * @param taskGroup
	 * @param showPoints 
	 * @param maxSubmitters 
	 * @param allowSubmittersAcrossGroups 
	 * @param taskType 
	 * @param dynamicTask 
	 * @param allowPrematureSubmissionClosing 
	 */
	public Task(String title, int maxPoints, ZonedDateTime start, ZonedDateTime deadline, String description, TaskGroup taskGroup, ZonedDateTime showPoints, int maxSubmitters, boolean allowSubmittersAcrossGroups, String taskType, String dynamicTask, boolean allowPrematureSubmissionClosing) {
		this.title = title;
		this.maxPoints = maxPoints;
		this.start = start;
		this.deadline = deadline;
		this.description = description;
		this.taskGroup = taskGroup;
		this.showPoints = showPoints;
		this.maxSubmitters = maxSubmitters;
		this.allowSubmittersAcrossGroups = allowSubmittersAcrossGroups;
		this.type = taskType;
		this.dynamicTask = dynamicTask;
		this.allowPrematureSubmissionClosing = allowPrematureSubmissionClosing;
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
	public int getMaxPoints() {
		return maxPoints;
	}

	/**
	 * @param maxPoints the maxPoints to set
	 */
	public void setMaxPoints(int maxPoints) {
		this.maxPoints = Math.max(0, Util.ensureMinPointStepMultiples(maxPoints, getMinPointStep()));
	}

	/**
	 * @return the start
	 */
	public ZonedDateTime getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(ZonedDateTime start) {
		this.start = start;
	}

	/**
	 * @return the deadline
	 */
	public ZonedDateTime getDeadline() {
		return deadline;
	}

	/**
	 * @param deadline the deadline to set
	 */
	public void setDeadline(ZonedDateTime deadline) {
		this.deadline = deadline;
	}

	/**
	 * @return the description
	 */
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
	public List<Test> getTests() {
		return tests;
	}

	/**
	 * @param tests 
	 */
	public void setTests(List<Test> tests) {
		this.tests = tests;
	}

	/**
	 * @return the showPoints
	 */
	public ZonedDateTime getShowPoints() {
		return showPoints;
	}

	/**
	 * @param showPoints the showPoints to set
	 */
	public void setShowPoints(ZonedDateTime showPoints) {
		this.showPoints = showPoints;
	}

	/**
	 * @return the similarityTests
	 */
	public List<SimilarityTest> getSimilarityTests() {
		return similarityTests;
	}

	/**
	 * @param similarityTests the similarityTests to set
	 */
	public void setSimilarityTests(List<SimilarityTest> similarityTests) {
		this.similarityTests = similarityTests;
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
	public String getShowTextArea() {
		return showTextArea;
	}

	/**
	 * @param showTextArea the showTextArea to set
	 */
	public void setShowTextArea(String showTextArea) {
		if (showTextArea.isBlank() || !Pattern.compile(Configuration.GLOBAL_FILENAME_REGEXP).matcher(showTextArea).matches()) {
			this.showTextArea = "-";
			return;
		}
		showTextArea = FilenameUtils.normalizeNoEndSeparator(showTextArea);
		if (showTextArea == null) {
			this.showTextArea = "-";
			return;
		}
		this.showTextArea = showTextArea;
	}

	@Transient
	public boolean showTextArea() {
		return !"-".equals(getShowTextArea());
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
		this.maxSubmitters = Math.max(1, maxSubmitters);
	}

	/**
	 * @return the featuredFiles
	 */
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
	public List<PointCategory> getPointCategories() {
		return pointCategories;
	}

	/**
	 * @param pointCategories the pointCategories to set
	 */
	public void setPointCategories(List<PointCategory> pointCategories) {
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
		this.minPointStep = Math.max(1, minPointStep);
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

	/**
	 * Returns the maximum upload size in bytes
	 * @return the maxsize
	 */
	public int getMaxsize() {
		return maxsize;
	}

	/**
	 * Sets the maximum upload size for this task in bytes
	 * @param maxsize the maxsize to set
	 */
	public void setMaxsize(int maxsize) {
		this.maxsize = Math.max(1024, Math.min(Configuration.MAX_UPLOAD_SIZE, maxsize));
	}

	/**
	 * @return the modelSolutionProvision
	 */
	public String getModelSolutionProvision() {
		return modelSolutionProvision;
	}

	/**
	 * @param modelSolutionProvision the modelSolutionProvision to set
	 */
	public void setModelSolutionProvision(String modelSolutionProvision) {
		this.modelSolutionProvision = modelSolutionProvision;
	}

	@Transient
	public ModelSolutionProvisionType getModelSolutionProvisionType() {
		if (getModelSolutionProvision() == null) {
			return ModelSolutionProvisionType.INTERNAL;
		}
		return ModelSolutionProvisionType.valueOf(getModelSolutionProvision());
	}

	@Transient
	public void setModelSolutionProvisionType(ModelSolutionProvisionType type) {
		setModelSolutionProvision(type.toString());
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param taskType the type to set
	 */
	public void setType(String taskType) {
		if (!List.of("", "sc", "mc", "cloze", "dynamicTask").contains(taskType)) {
			throw new RuntimeException("Invalid taskType");
		}
		this.type = taskType;
	}

	/**
	 * Checks if the task is a single choice task
	 * @return true/false
	 */
	@Transient
	public boolean isSCTask() {
		return "sc".equals(getType());
	}

	/**
	 * Checks if the task is a  multiple choice task
	 * @return true/false
	 */
	@Transient
	public boolean isMCTask() {
		return "mc".equals(getType());
	}

	/**
	 * Checks if the task is a single or  multiple choice task
	 * @return true/false
	 */
	@Transient
	public boolean isSCMCTask() {
		return isSCTask() || isMCTask();
	}

	/**
	 * Checks if the task is a cloze text
	 * @return true/false
	 */
	@Transient
	public boolean isClozeTask() {
		return "cloze".equals(getType());
	}

	/**
	 * Returns the flag whether the task has the close submission before deadline feature enabled
	 *
	 * @return the live submission flag
	 */
	public boolean isAllowPrematureSubmissionClosing() {
		return allowPrematureSubmissionClosing;
	}

	/**
	 * Sets the close submission before deadline submission flag fot the task
	 *
	 * @param allowPrematureSubmissionClosing 
	 */
	public void setAllowPrematureSubmissionClosing(boolean allowPrematureSubmissionClosing) {
		this.allowPrematureSubmissionClosing = allowPrematureSubmissionClosing;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "):  taskid:" + getTaskid() + "; title:" + getTitle() + "; taskgroupid:" + (getTaskGroup() == null ? "null" : getTaskGroup().getTaskGroupId());
	}
}
