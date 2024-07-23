/*
 * Copyright 2009-2012, 2017, 2020, 2024 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

/**
 * Data Access Object Interface for the Lecture-class
 * @author Sven Strickroth
 */
public interface LectureDAOIf {
	/**
	 * Returns all groups
	 * @return group-list
	 */
	List<Lecture> getLectures();

	/**
	 * Creates a new lecture and stores it in the DB
	 * @param name the lecture-name
	 * @param requiresAbhnahme 
	 * @param groupWiseGrading grading is done groupwise
	 * @return the new lecture
	 */
	Lecture newLecture(String name, boolean requiresAbhnahme, boolean groupWiseGrading);

	/**
	 * Fetch a lecture by id
	 * @param lectureId
	 * @return the lecture or null
	 */
	Lecture getLecture(int lectureId);

	/**
	 * Returns all lectures which User user doesn't attents
	 * @param user
	 * @return list of lectures
	 */
	List<Lecture> getCurrentLecturesWithoutUser(User user);

	/**
	 * Removes a lecture
	 * @param lecture the lecture to delete
	 */
	void deleteLecture(Lecture lecture);
}
