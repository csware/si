package de.tuclausthal.abgabesystem.persistence.dao;

import java.util.List;

import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

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

	// TODO: keep it?
	public List<Lecture> getCurrentLucturesWithoutUser(User user);
}
