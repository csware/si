package de.tuclausthal.abgabesystem.persistence.dao;

import de.tuclausthal.abgabesystem.persistence.datamodel.Group;
import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;

/**
 * Data Access Object Interface for the Group-class
 * @author Sven Strickroth
 */
public interface GroupDAOIf {
	/**
	 * Creates a new group for a lecture and store it in the DB
	 * @param lecture the lecture to which the group is associated to
	 * @param name the name for the group
	 * @return the new group
	 */
	public Group createGroup(Lecture lecture, String name);

	/**
	 * Fetch a group by the id
	 * @param groupid the id
	 * @return the group or null
	 */
	public Group getGroup(int groupid);

	/**
	 * Deletes a group
	 * @param group the group to delete
	 */
	public void deleteGroup(Group group);

	/**
	 * Updates a group and store it in the DB
	 * @param group
	 */
	public void saveGroup(Group group);
}
