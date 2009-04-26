package de.tuclausthal.abgabesystem.persistence.dao.impl;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.persistence.dao.PointsDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.Points;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;

public class PointsDAO implements PointsDAOIf {

	@Override
	public Points createPoints(int issuedPoints, Submission submission, Participation participation) {
		// Hibernate exception abfangen
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		//MainBetterNameHereRequired.getSession().get(User.class, uid, LockMode.UPGRADE);
		session.lock(submission.getTask(), LockMode.UPGRADE);
		if (issuedPoints > submission.getTask().getMaxPoints()) {
			issuedPoints = submission.getTask().getMaxPoints();
		}
		Points points = new Points();
		points.setPoints(issuedPoints);
		points.setIssuedBy(participation);
		submission.setPoints(points);
		session.save(submission);
		tx.commit();
		return points;
	}
}
