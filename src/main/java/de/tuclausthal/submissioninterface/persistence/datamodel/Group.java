/*
 * Copyright 2009-2011, 2020 Sven Strickroth <email@cs-ware.de>
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
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "groups")
public class Group implements Serializable {
	private int gid;
	private String name;
	private Lecture lecture;
	private Set<Participation> members;
	private Set<Participation> tutors;
	private boolean allowStudentsToSignup = false;
	private boolean allowStudentsToQuit = false;
	private int maxStudents = 20;
	private boolean submissionGroup = false;

	/**
	 * @return the gid
	 */
	@Id
	@GeneratedValue
	public int getGid() {
		return gid;
	}

	/**
	 * @param gid the gid to set
	 */
	public void setGid(int gid) {
		this.gid = gid;
	}

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
	 * @return the lecture
	 */
	@ManyToOne
	@JoinColumn(name = "lectureid", nullable = false)
	public Lecture getLecture() {
		return lecture;
	}

	/**
	 * @param lecture the lecture to set
	 */
	public void setLecture(Lecture lecture) {
		this.lecture = lecture;
	}

	/**
	 * @return the members
	 */
	@ManyToMany(mappedBy = "group", fetch = FetchType.LAZY)
	public Set<Participation> getMembers() {
		return members;
	}

	/**
	 * @param members the members to set
	 */
	public void setMembers(Set<Participation> members) {
		this.members = members;
	}

	/**
	 * @return the allowStudentsToSignup
	 */
	public boolean isAllowStudentsToSignup() {
		return allowStudentsToSignup;
	}

	/**
	 * @param allowStudentsToSignup the allowStudentsToSignup to set
	 */
	public void setAllowStudentsToSignup(boolean allowStudentsToSignup) {
		this.allowStudentsToSignup = allowStudentsToSignup;
	}

	/**
	 * @return the allowStudentsToQuit
	 */
	public boolean isAllowStudentsToQuit() {
		return allowStudentsToQuit;
	}

	/**
	 * @param allowStudentsToQuit the allowStudentsToQuit to set
	 */
	public void setAllowStudentsToQuit(boolean allowStudentsToQuit) {
		this.allowStudentsToQuit = allowStudentsToQuit;
	}

	/**
	 * @return the maxStudents
	 */
	public int getMaxStudents() {
		return maxStudents;
	}

	/**
	 * @param maxStudents the maxStudents to set
	 */
	public void setMaxStudents(int maxStudents) {
		this.maxStudents = maxStudents;
	}

	/**
	 * @return the tutors
	 */
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "groups_tutors")
	public Set<Participation> getTutors() {
		return tutors;
	}

	/**
	 * @param tutors the tutors to set
	 */
	public void setTutors(Set<Participation> tutors) {
		this.tutors = tutors;
	}

	/**
	 * @return the submissionGroup
	 */
	public boolean isSubmissionGroup() {
		return submissionGroup;
	}

	/**
	 * @param submissionGroup the submissionGroup to set
	 */
	public void setSubmissionGroup(boolean submissionGroup) {
		this.submissionGroup = submissionGroup;
	}
}
