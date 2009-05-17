/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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
	public List<Lecture> getLectures();

	/**
	 * Creates a new lecture and stores it in the DB
	 * @param name the lecture-name
	 * @return the new lecture
	 */
	public Lecture newLecture(String name);

	/**
	 * Fetch a lecture by id
	 * @param lectureId
	 * @return the lecture or null
	 */
	public Lecture getLecture(int lectureId);

	/**
	 * Returns all lectures which User user doesn't attents
	 * @param user
	 * @return list of lectures
	 */
	public List<Lecture> getCurrentLecturesWithoutUser(User user);

	/**
	 * Removes a lecture
	 * @param lecture the lecture to delete
	 */
	public void deleteLecture(Lecture lecture);

	/**
	 * Calculates the average given points of a lecture
	 * @param lecture
	 * @return the average points number
	 */
	public int getAveragePoints(Lecture lecture);
}
