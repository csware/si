package de.tuclausthal.abgabesystem.persistence.datamodel;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "participations", uniqueConstraints = { @UniqueConstraint(columnNames = { "lectureid", "uid" }) })
public class Participation implements Serializable {
	private int id;
	private User user;
	private Group group;
	private Lecture lecture;
	private String Role = "normal";
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
	 * @return the role
	 */
	@Column(nullable = false)
	// TODO: String enum?
	private String getRole() {
		return Role;
	}

	/**
	 * @param role the role to set
	 */
	private void setRole(String role) {
		Role = role;
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
	@OneToMany(mappedBy="submitter")
	public Set<Submission> getSubmissions() {
		return submissions;
	}

	/**
	 * @param submissions the submissions to set
	 */
	public void setSubmissions(Set<Submission> submissions) {
		this.submissions = submissions;
	}
}
