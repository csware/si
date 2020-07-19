/*
 * Copyright 2009 - 2012 Sven Strickroth <email@cs-ware.de>
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
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.OrderBy;

@Entity
@Table(name = "lectures")
public class Lecture implements Serializable {
	private int id;
	private String name;
	private int semester;
	private boolean requiresAbhnahme;
	private Set<Participation> participants;
	private List<TaskGroup> taskGroups;
	private Set<Group> groups;
	private String gradingMethod = "";

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the semester
	 */
	public int getSemester() {
		return semester;
	}

	/**
	 * @param semester the semester to set
	 */
	public void setSemester(int semester) {
		this.semester = semester;
	}

	/**
	 * 
	 * @return the human-readable semester
	 */
	@Transient
	public String getReadableSemester() {
		String semester = ((Integer) getSemester()).toString();
		StringBuffer ret = new StringBuffer(8);
		if (getSemester() % 2 == 1) {
			ret.insert(0, "WS " + semester.substring(0, 4) + "/" + ((getSemester() - 1) / 10 + 1));
		} else {
			ret.insert(0, "SS " + semester.substring(0, 4));
		}
		return ret.toString();
	}

	/**
	 * @return the participants
	 */
	@OneToMany(mappedBy = "lecture")
	@OnDelete(action = OnDeleteAction.CASCADE)
	// TODO: HACK!
	@OrderBy(clause = "user3_.lastname,user3_.firstname")
	public Set<Participation> getParticipants() {
		return participants;
	}

	/**
	 * @param participants the participants to set
	 */
	public void setParticipants(Set<Participation> participants) {
		this.participants = participants;
	}

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the taskGroups
	 */
	@OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy(clause = "taskgroupid asc")
	public List<TaskGroup> getTaskGroups() {
		return taskGroups;
	}

	/**
	 * @param taskGroups the taskGroups to set
	 */
	public void setTaskGroups(List<TaskGroup> taskGroups) {
		this.taskGroups = taskGroups;
	}

	/**
	 * @return the groups
	 */
	@OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy(clause = "name asc")
	public Set<Group> getGroups() {
		return groups;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	/**
	 * @return the requiresAbhnahme
	 */
	public boolean isRequiresAbhnahme() {
		return requiresAbhnahme;
	}

	/**
	 * @param requiresAbhnahme the requiresAbhnahme to set
	 */
	public void setRequiresAbhnahme(boolean requiresAbhnahme) {
		this.requiresAbhnahme = requiresAbhnahme;
	}

	/**
	 * @return the gradingMethod
	 */
	public String getGradingMethod() {
		return gradingMethod;
	}

	/**
	 * @param gradingMethod the gradingMethod to set
	 */
	public void setGradingMethod(String gradingMethod) {
		this.gradingMethod = gradingMethod;
	}
}
