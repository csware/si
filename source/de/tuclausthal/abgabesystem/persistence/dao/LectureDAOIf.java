package de.tuclausthal.abgabesystem.persistence.dao;

import java.util.List;

import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public interface LectureDAOIf {
	public List<Lecture> getLectures();

	public Lecture newLecture(String name);

	public Lecture getLecture(int lectureId);

	public List<Lecture> getCurrentLucturesWithoutUser(User user);
}
