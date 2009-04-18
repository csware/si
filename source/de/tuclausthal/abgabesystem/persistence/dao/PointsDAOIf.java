package de.tuclausthal.abgabesystem.persistence.dao;

import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.Points;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;

public interface PointsDAOIf {
	public Points createPoints(int issuedPoints, Submission submission, Participation participation);
}
