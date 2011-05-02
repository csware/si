/*
 * Copyright 2009 - 2011 Sven Strickroth <email@cs-ware.de>
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

import java.util.Iterator;
import java.util.Map;

import org.hibernate.LockMode;
import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.PointGivenDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.PointsDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointGiven;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointHistory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.util.MailSender;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Data Access Object implementation for the PointsDAOIf
 * @author Sven Strickroth
 */
public class PointsDAO extends AbstractDAO implements PointsDAOIf {
	public PointsDAO(Session session) {
		super(session);
	}

	@Override
	public Points createPoints(int issuedPoints, Submission submission, Participation participation, String publicComment, String internalComment, PointStatus pointStatus, Integer duplicate) {
		Session session = getSession();

		session.lock(submission, LockMode.UPGRADE);
		session.lock(submission.getTask(), LockMode.UPGRADE);
		if (issuedPoints % submission.getTask().getMinPointStep() != 0) {
			issuedPoints = (issuedPoints / submission.getTask().getMinPointStep()) * submission.getTask().getMinPointStep();
		}
		if (issuedPoints < 0) {
			issuedPoints = 0;
		} else if (issuedPoints > submission.getTask().getMaxPoints()) {
			issuedPoints = submission.getTask().getMaxPoints();
		}
		Points oldPoints = submission.getPoints();
		Points points = new Points();
		points.setPoints(issuedPoints);
		points.setPointStatus(pointStatus);
		points.setDuplicate(duplicate);
		points.setIssuedBy(participation);
		submission.setPoints(points);
		points.setPublicComment(publicComment);
		points.setInternalComment(internalComment);
		session.save(submission);

		// TODO: Attention: see @MarkApproved.java and below!!!
		if (oldPoints != null) {
			boolean changed = false;
			if (!oldPoints.getPointStatus().equals(points.getPointStatus())) {
				storeInHistory(submission, "status", PointStatus.values()[oldPoints.getPointStatus()].toString(), PointStatus.values()[points.getPointStatus()].toString(), participation);
				if (!((oldPoints.getPointStatus() == PointStatus.NICHT_ABGENOMMEN.ordinal() && points.getPointStatus() == PointStatus.ABGENOMMEN.ordinal()) || (oldPoints.getPointStatus() == PointStatus.NICHT_ABGENOMMEN.ordinal() && points.getPointStatus() == PointStatus.ABGENOMMEN_FAILED.ordinal()))) {
					changed = true;
				}
			}
			if (oldPoints.getDuplicate() != null || points.getDuplicate() != null) {
				if (oldPoints.getDuplicate() == null && points.getDuplicate() != null) {
					storeInHistory(submission, "duplicate", "", points.getDuplicate() + "", participation);
					changed = true;
				} else if (oldPoints.getDuplicate() != null && points.getDuplicate() == null) {
					storeInHistory(submission, "duplicate", oldPoints.getDuplicate() + "", "", participation);
					changed = true;
				} else if (!oldPoints.getDuplicate().equals(points.getDuplicate())) {
					storeInHistory(submission, "duplicate", oldPoints.getDuplicate() + "", points.getDuplicate() + "", participation);
					changed = true;
				}
			}
			if (!oldPoints.getPoints().equals(points.getPoints())) {
				storeInHistory(submission, "points", Util.showPoints(oldPoints.getPoints()), Util.showPoints(points.getPoints()), participation);
				changed = true;
			}
			if (oldPoints.getInternalComment() != null && !oldPoints.getInternalComment().equals(points.getInternalComment())) {
				storeInHistory(submission, "internalComment", oldPoints.getInternalComment(), points.getInternalComment(), participation);
				changed = true;
			}
			if (oldPoints.getPublicComment() != null && !oldPoints.getPublicComment().equals(points.getPublicComment())) {
				storeInHistory(submission, "publicComment", oldPoints.getPublicComment(), points.getPublicComment(), participation);
				changed = true;
			}
			if (changed && oldPoints.getIssuedBy().getUser().getUid() != participation.getUser().getUid()) {
				// HACK hardcoded URL
				MailSender.sendMail(oldPoints.getIssuedBy().getUser().getFullEmail(), "Mark-Change Notification", "Hallo,\n\n" + participation.getUser().getFullName() + " hat Deine Bewertung von <https://si.in.tu-clausthal.de/submissionsystem/servlets/ShowSubmission?sid=" + submission.getSubmissionid() + "> verändert.\n\n-- \nReply is not possible.");
			}
		} else {
			if (points.getPointStatus() != null) {
				storeInHistory(submission, "status", "", PointStatus.values()[points.getPointStatus()].toString(), participation);
			}
			if (points.getDuplicate() != null) {
				storeInHistory(submission, "duplicate", "", points.getDuplicate() + "", participation);
			}
			if (points.getPoints() != null) {
				storeInHistory(submission, "points", "", Util.showPoints(points.getPoints()), participation);
			}
			if (points.getInternalComment() != null && !"".equals(points.getInternalComment())) {
				storeInHistory(submission, "internalComment", "", points.getInternalComment(), participation);
			}
			if (points.getPublicComment() != null && !"".equals(points.getPublicComment())) {
				storeInHistory(submission, "publicComment", "", points.getPublicComment(), participation);
			}
		}
		return points;
	}

	private void storeInHistory(Submission submission, String field, String removed, String added, Participation marker) {
		PointHistory ph = new PointHistory(submission, field, removed, added, marker);
		getSession().save(ph);
	}

	@Override
	public Points createPoints(Map<String, String[]> pointGiven, Submission submission, Participation participation, String publicComment, String internalComment, PointStatus pointStatus, Integer duplicate) {
		Session session = getSession();

		session.lock(submission, LockMode.UPGRADE);
		session.lock(submission.getTask(), LockMode.UPGRADE);

		boolean changed = false;

		// implement hacky solution since this will be a short list ;)
		PointGivenDAOIf pointGivenDAO = DAOFactory.PointGivenDAOIf(session);
		Iterator<PointGiven> pointsGivenIterator = pointGivenDAO.getPointsGivenOfSubmission(submission).iterator();
		PointGiven lastPointGiven = null;
		if (pointsGivenIterator.hasNext()) {
			lastPointGiven = pointsGivenIterator.next();
		}
		int numPoints = 0;
		for (PointCategory category : submission.getTask().getPointCategories()) {
			while (lastPointGiven != null && category.getPointcatid() > lastPointGiven.getCategory().getPointcatid()) {
				if (pointsGivenIterator.hasNext()) {
					lastPointGiven = pointsGivenIterator.next();
				} else {
					lastPointGiven = null;
					break;
				}
			}
			int issuedPoints = 0;
			if (pointGiven.get("point_" + category.getPointcatid()) != null) {
				issuedPoints = Util.convertToPoints(pointGiven.get("point_" + category.getPointcatid())[0], submission.getTask().getMinPointStep());
			}
			if (issuedPoints > category.getPoints()) {
				issuedPoints = category.getPoints();
			}
			if (lastPointGiven != null && category.getPointcatid() == lastPointGiven.getCategory().getPointcatid()) {
				if (issuedPoints == 0) {
					storeInHistory(submission, category.getDescription(), Util.showPoints(lastPointGiven.getPoints()), "0", participation);
					pointGivenDAO.revokePointGiven(lastPointGiven);
					changed = true;
				} else {
					if (issuedPoints != lastPointGiven.getPoints()) {
						storeInHistory(submission, category.getDescription(), Util.showPoints(lastPointGiven.getPoints()), Util.showPoints(issuedPoints), participation);
						pointGivenDAO.revokePointGiven(lastPointGiven);
						pointGivenDAO.givePoint(issuedPoints, submission, category);
						changed = true;
					}
					numPoints += issuedPoints;
				}
			} else {
				if (issuedPoints > 0) {
					numPoints += issuedPoints;
					storeInHistory(submission, category.getDescription(), "0", Util.showPoints(issuedPoints), participation);
					pointGivenDAO.givePoint(issuedPoints, submission, category);
					changed = true;
				}
			}
		}

		Points oldPoints = submission.getPoints();
		Points points = new Points();
		points.setPoints(numPoints);
		points.setPointStatus(pointStatus);
		points.setDuplicate(duplicate);
		points.setIssuedBy(participation);
		submission.setPoints(points);
		points.setPublicComment(publicComment);
		points.setInternalComment(internalComment);
		session.save(submission);

		// TODO: Attention: see @MarkApproved.java
		if (oldPoints != null) {
			if (!oldPoints.getPointStatus().equals(points.getPointStatus())) {
				storeInHistory(submission, "status", PointStatus.values()[oldPoints.getPointStatus()].toString(), PointStatus.values()[points.getPointStatus()].toString(), participation);
				if (!((oldPoints.getPointStatus() == PointStatus.NICHT_ABGENOMMEN.ordinal() && points.getPointStatus() == PointStatus.ABGENOMMEN.ordinal()) || (oldPoints.getPointStatus() == PointStatus.NICHT_ABGENOMMEN.ordinal() && points.getPointStatus() == PointStatus.ABGENOMMEN_FAILED.ordinal()))) {
					changed = true;
				}
			}
			if (oldPoints.getDuplicate() != null || points.getDuplicate() != null) {
				if (oldPoints.getDuplicate() == null) {
					storeInHistory(submission, "duplicate", "", points.getDuplicate() + "", participation);
					changed = true;
				} else if (points.getDuplicate() == null) {
					storeInHistory(submission, "duplicate", oldPoints.getDuplicate() + "", "", participation);
					changed = true;
				} else if (!oldPoints.getDuplicate().equals(points.getDuplicate())) {
					storeInHistory(submission, "duplicate", oldPoints.getDuplicate() + "", points.getDuplicate() + "", participation);
					changed = true;
				}
			}
			if (!oldPoints.getPoints().equals(points.getPoints())) {
				storeInHistory(submission, "points", Util.showPoints(oldPoints.getPoints()), Util.showPoints(points.getPoints()), participation);
				changed = true;
			}
			if (oldPoints.getInternalComment() != null && !oldPoints.getInternalComment().equals(points.getInternalComment())) {
				storeInHistory(submission, "internalComment", oldPoints.getInternalComment(), points.getInternalComment(), participation);
				changed = true;
			}
			if (oldPoints.getPublicComment() != null && !oldPoints.getPublicComment().equals(points.getPublicComment())) {
				storeInHistory(submission, "publicComment", oldPoints.getPublicComment(), points.getPublicComment(), participation);
				changed = true;
			}
			if (changed && oldPoints.getIssuedBy().getUser().getUid() != participation.getUser().getUid()) {
				// HACK hardcoded URL
				MailSender.sendMail(oldPoints.getIssuedBy().getUser().getFullEmail(), "Mark-Change Notification", "Hallo,\n\n" + participation.getUser().getFullName() + " hat Deine Bewertung von <https://si.in.tu-clausthal.de/submissionsystem/servlets/ShowSubmission?sid=" + submission.getSubmissionid() + "> verändert.\n\n-- \nReply is not possible.");
			}
		} else {
			if (points.getPointStatus() != null) {
				storeInHistory(submission, "status", "", PointStatus.values()[points.getPointStatus()].toString(), participation);
			}
			if (points.getDuplicate() != null) {
				storeInHistory(submission, "duplicate", "", points.getDuplicate() + "", participation);
			}
			if (points.getPoints() != null) {
				storeInHistory(submission, "points", "", Util.showPoints(points.getPoints()), participation);
			}
			if (points.getInternalComment() != null && !"".equals(points.getInternalComment())) {
				storeInHistory(submission, "internalComment", "", points.getInternalComment(), participation);
			}
			if (points.getPublicComment() != null && !"".equals(points.getPublicComment())) {
				storeInHistory(submission, "publicComment", "", points.getPublicComment(), participation);
			}
		}
		return points;
	}
}
