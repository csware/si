/*
 * Copyright 2009-2010, 2020-2022, 2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

/**
 * Data Access Object Interface for the Participation-class
 * @author Sven Strickroth
 */
public interface ParticipationDAOIf {
	/**
	 * Creates a new Participation for a user to a lecture with a specific role
	 * @param user the user
	 * @param lecture the lecture
	 * @param type the role-type
	 * @return true if a new participation was created
	 */
	boolean createParticipation(User user, Lecture lecture, ParticipationRole type);

	/**
	 * Fetch a participation for a user in a specific lecture
	 * @param user the user
	 * @param lecture the lecture
	 * @return the participation or null
	 */
	Participation getParticipation(User user, Lecture lecture);

	Participation getParticipationLocked(User user, Lecture lecture);

	/**
	 * Fetch a participation by the participation id
	 * @param participationid the id
	 * @return the participation or null
	 */
	Participation getParticipation(int participationid);

	Participation getParticipationLocked(int participationid);

	/**
	 * Returns the participation for a lecture which are not associated with a group for a given lecture
	 * @param lecture the lecture
	 * @return participation list
	 */
	List<Participation> getParticipationsWithoutGroup(Lecture lecture);

	List<Participation> getMarkersAvailableParticipations(Group group);

	List<Participation> getParticipationsWithNoSubmissionToTaskOrdered(Task task);

	List<Participation> getLectureParticipationsOrderedByName(Lecture lecture);

	/**
	 * Returns the participation for a specific group
	 * @param group the group
	 * @return participation list
	 */
	List<Participation> getParticipationsOfGroup(Group group);

	/**
	 * Removes a specific participation
	 * @param participation the participation to delete
	 */
	void deleteParticipation(Participation participation);

	/**
	 * Remove a participation based on the user-lecture association
	 * @param user the user of the participation
	 * @param lecture the lecture
	 */
	void deleteParticipation(User user, Lecture lecture);
}
