/*
 * Copyright 2009-2011, 2020-2024 Sven Strickroth <email@cs-ware.de>
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
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "groups")
public class Group implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private int gid;
	@Column(nullable = false)
	private String name;
	@ManyToOne
	@JoinColumn(name = "lectureid", nullable = false)
	@JsonBackReference
	private Lecture lecture;
	@OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
	@JsonIgnore
	private Set<Participation> members = new HashSet<>();
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "groups_tutors", joinColumns = @JoinColumn(name = "groups_gid"))
	@JsonIgnore
	private Set<Participation> tutors = new HashSet<>();
	private boolean allowStudentsToSignup = false;
	private boolean allowStudentsToQuit = false;
	private int maxStudents = 20;
	private boolean submissionGroup = false;
	private boolean membersVisibleToStudents = false;

	/**
	 * @return the gid
	 */
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
		this.maxStudents = Math.max(1, maxStudents);
	}

	/**
	 * @return the tutors
	 */
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

	/**
	 * @return the membersVisibleToStudents
	 */
	public boolean isMembersVisibleToStudents() {
		return membersVisibleToStudents;
	}

	/**
	 * @param membersVisibleToStudents the membersVisibleToStudents to set
	 */
	public void setMembersVisibleToStudents(boolean membersVisibleToStudents) {
		this.membersVisibleToStudents = membersVisibleToStudents;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): gid:" + getGid() + "; name:" + getName() + "; lectureid:" + (getLecture() == null ? "null" : getLecture().getId());
	}
}
