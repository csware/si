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

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "participations", uniqueConstraints = { @UniqueConstraint(columnNames = { "lectureid", "uid" }) })
public class Participation implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private User user;
	private Group group;
	private Lecture lecture;
	private String role = "NORMAL";
	private Set<Submission> submissions;

	/**
	 * @return the user
	 */
	@ManyToOne
	@JoinColumn(name = "uid", nullable = false)
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
	 * @return the group
	 */
	@ManyToOne
	@JoinColumn(name = "groupid")
	public Group getGroup() {
		return group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(Group group) {
		this.group = group;
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
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
	 * @return the role
	 */
	@Column(nullable = false)
	private String getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	private void setRole(String role) {
		this.role = role;
	}

	@Transient
	public ParticipationRole getRoleType() {
		return ParticipationRole.valueOf(getRole());
	}

	@Transient
	public void setRoleType(ParticipationRole type) {
		setRole(type.toString());
	}

	/**
	 * @return the submissions
	 */
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "submissions_participations", joinColumns = @JoinColumn(name = "submitters_id"), inverseJoinColumns = @JoinColumn(name = "submissions_submissionid"))
	public Set<Submission> getSubmissions() {
		return submissions;
	}

	/**
	 * @param submissions the submissions to set
	 */
	public void setSubmissions(Set<Submission> submissions) {
		this.submissions = submissions;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): id:" + getId() + "; userid:" + (getUser() == null ? "null" : getUser().getUid()) + "; lectureid:" + (getLecture() == null ? "null" : getLecture().getId()) + "; role:" + getRoleType();
	}
}
