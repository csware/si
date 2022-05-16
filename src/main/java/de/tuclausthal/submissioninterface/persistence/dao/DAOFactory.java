/*
 * Copyright 2009-2010, 2020 Sven Strickroth <email@cs-ware.de>
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

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.impl.GroupDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.LectureDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.MCOptionDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.ParticipationDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.PointCategoryDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.PointGivenDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.PointsDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.ResultDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.SimilarityDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.SimilarityTestDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.SubmissionDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.TaskDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.TaskGroupDAO;
import de.tuclausthal.submissioninterface.persistence.dao.impl.TaskNumberDAO;
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
	 * @param session 
	 * @return a GroupDAOIf instance
	 */
	public static GroupDAOIf GroupDAOIf(Session session) {
		return new GroupDAO(session);
	}

	/**
	 * Returns a LectureDAOIf instance
	 * @param session 
	 * @return a LectureDAOIf instance
	 */
	public static LectureDAOIf LectureDAOIf(Session session) {
		return new LectureDAO(session);
	}

	/**
	 * Returns a ParticipationDAOIf instance
	 * @param session 
	 * @return a ParticipationDAOIf instance
	 */
	public static ParticipationDAOIf ParticipationDAOIf(Session session) {
		return new ParticipationDAO(session);
	}

	/**
	 * Returns a PointsDAOIf instance
	 * @param session 
	 * @return a PointsDAOIf instance
	 */
	public static PointsDAOIf PointsDAOIf(Session session) {
		return new PointsDAO(session);
	}

	/**
	 * Returns a SubmissionDAOIf instance
	 * @param session 
	 * @return a SubmissionDAOIf instance
	 */
	public static SubmissionDAOIf SubmissionDAOIf(Session session) {
		return new SubmissionDAO(session);
	}

	/**
	 * Returns a TaskDAOIf instance
	 * @param session 
	 * @return a TaskDAOIf instance
	 */
	public static TaskDAOIf TaskDAOIf(Session session) {
		return new TaskDAO(session);
	}

	/**
	 * Returns a UserDAOIf instance
	 * @param session 
	 * @return a UserDAOIf instance
	 */
	public static UserDAOIf UserDAOIf(Session session) {
		return new UserDAO(session);
	}

	/**
	 * Returns a TestResultDAOIf instance
	 * @param session 
	 * @return a TestResultDAOIf instance
	 */
	public static TestResultDAOIf TestResultDAOIf(Session session) {
		return new TestResultDAO(session);
	}

	/**
	 * Returns a TestDAOIf instance
	 * @param session 
	 * @return a TestDAOIf instance
	 */
	public static TestDAOIf TestDAOIf(Session session) {
		return new TestDAO(session);
	}

	/**
	 * Returns a SimilarityDAOIf instance
	 * @param session 
	 * @return a SimilarityDAO instance
	 */
	public static SimilarityDAOIf SimilarityDAOIf(Session session) {
		return new SimilarityDAO(session);
	}

	/**
	 * Returns a SimilarityTestDAOIf instance
	 * @param session 
	 * @return a SimilarityTestDAOIf instance
	 */
	public static SimilarityTestDAOIf SimilarityTestDAOIf(Session session) {
		return new SimilarityTestDAO(session);
	}

	public static TestCountDAOIf TestCountDAOIf(Session session) {
		return new TestCountDAO(session);
	}

	public static TaskGroupDAOIf TaskGroupDAOIf(Session session) {
		return new TaskGroupDAO(session);
	}

	public static PointGivenDAOIf PointGivenDAOIf(Session session) {
		return new PointGivenDAO(session);
	}

	public static PointCategoryDAOIf PointCategoryDAOIf(Session session) {
		return new PointCategoryDAO(session);
	}

	public static ResultDAOIf ResultDAOIf(Session session) {
		return new ResultDAO(session);
	}

	public static TaskNumberDAOIf TaskNumberDAOIf(Session session) {
		return new TaskNumberDAO(session);
	}

	public static MCOptionDAOIf MCOptionDAOIf(Session session) {
		return new MCOptionDAO(session);
	}
}
