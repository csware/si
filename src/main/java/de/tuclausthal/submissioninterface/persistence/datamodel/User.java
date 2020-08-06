/*
 * Copyright 2009-2010, 2017 Sven Strickroth <email@cs-ware.de>
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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.OrderBy;

import de.tuclausthal.submissioninterface.util.Configuration;

@Entity
@Table(name = "users")
@DiscriminatorFormula("case when matrikelno is null then 0 else 1 end")
@DiscriminatorValue("0")
public class User implements Serializable {
	private int uid;
	private String email;
	private String lastName = "";
	private String firstName = "";
	private boolean superUser = false;
	private Set<Participation> lectureParticipant;

	/**
	 * @return the email
	 */
	@Column(nullable = false, unique = true)
	public String getEmail() {
		return email;
	}

	@Transient
	public String getFullEmail() {
		return getEmail() + "@" + Configuration.getInstance().getMailDomain();
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
	@GeneratedValue
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
	public String getFullName() {
		if (getFirstName().isEmpty()) {
			return getLastName();
		}
		return getLastName() + ", " + getFirstName();
	}
}
