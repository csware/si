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

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.impl.GroupDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.LectureDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.ParticipationDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.PointsDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.SimilarityDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.SimilarityTestDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.SubmissionDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.TaskDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.TestCountDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.TestDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.TestResultDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.UserDAO;

/**
 * Data Access Object (DAO) Factory
 * @author Sven Strickroth
 */
public class DAOFactory {
	/**
	 * Returns a GroupDAOIf instance
	 * @return a GroupDAOIf instance
	 */
	public static GroupDAOIf GroupDAOIf(Session session) {
		return new GroupDAO(session);
	}

	/**
	 * Returns a LectureDAOIf instance
	 * @return a LectureDAOIf instance
	 */
	public static LectureDAOIf LectureDAOIf(Session session) {
		return new LectureDAO(session);
	}

	/**
	 * Returns a ParticipationDAOIf instance
	 * @return a ParticipationDAOIf instance
	 */
	public static ParticipationDAOIf ParticipationDAOIf(Session session) {
		return new ParticipationDAO(session);
	}

	/**
	 * Returns a PointsDAOIf instance
	 * @return a PointsDAOIf instance
	 */
	public static PointsDAOIf PointsDAOIf(Session session) {
		return new PointsDAO(session);
	}

	/**
	 * Returns a SubmissionDAOIf instance
	 * @return a SubmissionDAOIf instance
	 */
	public static SubmissionDAOIf SubmissionDAOIf(Session session) {
		return new SubmissionDAO(session);
	}

	/**
	 * Returns a TaskDAOIf instance
	 * @return a TaskDAOIf instance
	 */
	public static TaskDAOIf TaskDAOIf(Session session) {
		return new TaskDAO(session);
	}

	/**
	 * Returns a UserDAOIf instance
	 * @return a UserDAOIf instance
	 */
	public static UserDAOIf UserDAOIf(Session session) {
		return new UserDAO(session);
	}

	/**
	 * Returns a TestResultDAOIf instance
	 * @return a TestResultDAOIf instance
	 */
	public static TestResultDAOIf TestResultDAOIf(Session session) {
		return new TestResultDAO(session);
	}

	/**
	 * Returns a TestDAOIf instance
	 * @return a TestDAOIf instance
	 */
	public static TestDAOIf TestDAOIf(Session session) {
		return new TestDAO(session);
	}

	/**
	 * Returns a SimilarityDAOIf instance
	 * @return a SimilarityDAO instance
	 */
	public static SimilarityDAOIf SimilarityDAOIf(Session session) {
		return new SimilarityDAO(session);
	}

	/**
	 * Returns a SimilarityTestDAOIf instance
	 * @return a SimilarityTestDAOIf instance
	 */
	public static SimilarityTestDAOIf SimilarityTestDAOIf(Session session) {
		return new SimilarityTestDAO(session);
	}

	public static TestCountDAOIf TestCountDAOIf(Session session) {
		return new TestCountDAO(session);
	}
}
