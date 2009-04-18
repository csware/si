package de.tuclausthal.abgabesystem.persistence.datamodel;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "lectures")
public class Lecture implements Serializable {
	private int id;
	private String name;
	private int semester;
	private Set<Participation> participants;
	private List<Task> tasks;
	private Set<Group> groups;

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
	 * @return the tasks
	 */
	@OneToMany(mappedBy = "lecture")
	@OnDelete(action = OnDeleteAction.CASCADE)
	public List<Task> getTasks() {
		return tasks;
	}

	/**
	 * @param tasks the tasks to set
	 */
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	/**
	 * @return the groups
	 */
	@OneToMany(mappedBy = "lecture")
	@OnDelete(action = OnDeleteAction.CASCADE)
	public Set<Group> getGroups() {
		return groups;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}
}
