package de.tuclausthal.abgabesystem.persistence.datamodel;

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

@Entity
@Table(name = "users")
@DiscriminatorFormula("case when matrikelno is null then 0 else 1 end")
@DiscriminatorValue("0")
public class User implements Serializable {
	private int uid;
	private String email;
	private String password;
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

	/**
	 * @return the lectureParticipant
	 */
	@OneToMany(mappedBy = "user")
	public Set<Participation> getLectureParticipant() {
		return lectureParticipant;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
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
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
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

	@Transient
	public String getFullName() {
		if (getFirstName().isEmpty()) {
			return getLastName();
		} else {
			return getFirstName() + " " + getLastName();
		}
	}
}
