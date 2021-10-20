/*
 * Copyright 2009-2010, 2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import java.util.List;
import java.util.Map;

import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;

/**
 * Data Access Object Interface for the Group-class
 * @author Sven Strickroth
 */
public interface GroupDAOIf {
	/**
	 * Creates a new group for a lecture and store it in the DB
	 * @param lecture the lecture to which the group is associated to
	 * @param name the name for the group
	 * @param allowStudentsToSignup 
	 * @param allowStudentsToQuit 
	 * @param maxStudents 
	 * @param membersVisibleToStudents 
	 * @return the new group
	 */
	public Group createGroup(Lecture lecture, String name, boolean allowStudentsToSignup, boolean allowStudentsToQuit, int maxStudents, boolean membersVisibleToStudents);

	/**
	 * Fetch a group by the id
	 * @param groupid the id
	 * @return the group or null
	 */
	public Group getGroup(int groupid);

	public Group getGroupLocked(int groupid);

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

	public List<Group> getJoinAbleGroups(Lecture lecture, Group participationGroup);

	public Map<Integer, Integer> getGroupSizes(List<Group> groupsToConsider, Group participationGroup);
}
