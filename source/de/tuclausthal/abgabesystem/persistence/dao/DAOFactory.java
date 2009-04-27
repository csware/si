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

public class DAOFactory {
	public static GroupDAOIf GroupDAOIf() {
		return new GroupDAO();
	}

	public static LectureDAOIf LectureDAOIf() {
		return new LectureDAO();
	}

	public static ParticipationDAOIf ParticipationDAOIf() {
		return new ParticipationDAO();
	}

	public static PointsDAOIf PointsDAOIf() {
		return new PointsDAO();
	}

	public static SubmissionDAOIf SubmissionDAOIf() {
		return new SubmissionDAO();
	}

	public static TaskDAOIf TaskDAOIf() {
		return new TaskDAO();
	}

	public static UserDAOIf UserDAOIf() {
		return new UserDAO();
	}

	public static TestResultDAOIf TestResultDAOIf() {
		return new TestResultDAO();
	}

	public static TestDAOIf TestDAOIf() {
		return new TestDAO();
	}
}
