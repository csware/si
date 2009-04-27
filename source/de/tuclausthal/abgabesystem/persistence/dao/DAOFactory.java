package de.tuclausthal.abgabesystem.persistence.dao;

import de.tuclausthal.abgabesystem.persistence.dao.impl.GroupDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.LectureDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.ParticipationDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.PointsDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.SubmissionDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.TaskDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.TestDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.TestResultDAO;
import de.tuclausthal.abgabesystem.persistence.dao.impl.UserDAO;

/**
 * Data Access Object (DAO) Factory
 * @author Sven Strickroth
 */
public class DAOFactory {
	/**
	 * Returns a GroupDAO instance
	 * @return GroupDAO instance
	 */
	public static GroupDAOIf GroupDAOIf() {
		return new GroupDAO();
	}

	/**
	 * Returns a LectureDAO instance
	 * @return a LectureDAO instance
	 */
	public static LectureDAOIf LectureDAOIf() {
		return new LectureDAO();
	}

	/**
	 * Returns a ParticipationDAO instance
	 * @return a ParticipationDAO instance
	 */
	public static ParticipationDAOIf ParticipationDAOIf() {
		return new ParticipationDAO();
	}

	/**
	 * Returns a PointsDAO instance
	 * @return a PointsDAO instance
	 */
	public static PointsDAOIf PointsDAOIf() {
		return new PointsDAO();
	}

	/**
	 * Returns a SubmissionDAO instance
	 * @return a SubmissionDAO instance
	 */
	public static SubmissionDAOIf SubmissionDAOIf() {
		return new SubmissionDAO();
	}

	/**
	 * Returns a TaskDAO instance
	 * @return a TaskDAO instance
	 */
	public static TaskDAOIf TaskDAOIf() {
		return new TaskDAO();
	}

	/**
	 * Returns a UserDAO instance
	 * @return a UserDAO instance
	 */
	public static UserDAOIf UserDAOIf() {
		return new UserDAO();
	}

	/**
	 * Returns a TestResultDAO instance
	 * @return a TestResultDAO instance
	 */
	public static TestResultDAOIf TestResultDAOIf() {
		return new TestResultDAO();
	}

	/**
	 * Returns a TestDAO instance
	 * @return a TestDAO instance
	 */
	public static TestDAOIf TestDAOIf() {
		return new TestDAO();
	}
}
