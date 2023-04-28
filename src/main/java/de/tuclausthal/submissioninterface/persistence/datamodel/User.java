/*
 * Copyright 2009-2010, 2017, 2020-2023 Sven Strickroth <email@cs-ware.de>
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
import java.util.Set;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.OrderBy;

@Entity
@Table(name = "users")
@DiscriminatorFormula("case when matrikelno is null then 0 else 1 end")
@DiscriminatorValue("0")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private int uid;
	private String username;
	private String email;
	private String lastName = "";
	private String firstName = "";
	private boolean superUser = false;
	private Set<Participation> lectureParticipant;
	private ZonedDateTime lastLoggedIn;

	/**
	 * @return the email
	 */
	@Column(nullable = false)
	public String getEmail() {
		return email;
	}

	/**
	 * @return the lectureParticipant
	 */
	@OneToMany(mappedBy = "user")
	@OrderBy(clause = "id desc")
	public Set<Participation> getLectureParticipant() {
		return lectureParticipant;
	}

	/**
	 * @return the uid
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getUid() {
		return uid;
	}

	/**
	 * @return the superUser
	 */
	public boolean isSuperUser() {
		return superUser;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @param lectureParticipant the lectureParticipant to set
	 */
	public void setLectureParticipant(Set<Participation> lectureParticipant) {
		this.lectureParticipant = lectureParticipant;
	}

	/**
	 * @param superUser the superUser to set
	 */
	public void setSuperUser(boolean superUser) {
		this.superUser = superUser;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(int uid) {
		this.uid = uid;
	}

	/**
	 * @return the lastName
	 */
	@Column(nullable = false)
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	@Column(nullable = false)
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the firstName
	 */
	@Column(nullable = false)
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Returns the full name of a user
	 * @return the full name string
	 */
	@Transient
	public String getLastNameFirstName() {
		if (getFirstName().isEmpty()) {
			return getLastName();
		}
		return getLastName() + ", " + getFirstName();
	}

	/**
	 * Returns the full name of a user
	 * @return the full name string
	 */
	@Transient
	public String getFullName() {
		if (getFirstName().isEmpty()) {
			return getLastName();
		}
		return getFirstName() + " " + getLastName();
	}

	/**
	 * @return the username
	 */
	@Column(nullable = false, unique = true)
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the lastLoggedIn
	 */
	@Basic
	public ZonedDateTime getLastLoggedIn() {
		return lastLoggedIn;
	}

	/**
	 * @param lastLoggedIn the lastLoggedIn to set
	 */
	public void setLastLoggedIn(ZonedDateTime lastLoggedIn) {
		this.lastLoggedIn = lastLoggedIn;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): uid:" + getUid() + "; username:" + getUsername();
	}
}
