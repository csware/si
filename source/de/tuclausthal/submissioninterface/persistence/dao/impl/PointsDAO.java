/*
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import org.hibernate.LockMode;
import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.PointsDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

/**
 * Data Access Object implementation for the PointsDAOIf
 * @author Sven Strickroth
 */
public class PointsDAO extends AbstractDAO implements PointsDAOIf {
	public PointsDAO(Session session) {
		super(session);
	}

	@Override
	public Points createPoints(int issuedPoints, Submission submission, Participation participation, String comment, boolean pointsOk) {
		// Hibernate exception abfangen
		Session session = getSession();
		//MainBetterNameHereRequired.getSession().get(User.class, uid, LockMode.UPGRADE);
		session.lock(submission.getTask(), LockMode.UPGRADE);
		if (issuedPoints > submission.getTask().getMaxPoints()) {
			issuedPoints = submission.getTask().getMaxPoints();
		}
		Points points = new Points();
		points.setPoints(issuedPoints);
		points.setPointsOk(pointsOk);
		points.setIssuedBy(participation);
		submission.setPoints(points);
		points.setComment(comment);
		session.save(submission);
		return points;
	}
}
