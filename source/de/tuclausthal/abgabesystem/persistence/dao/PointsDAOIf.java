package de.tuclausthal.abgabesystem.persistence.dao;

import de.tuclausthal.abgabesystem.persistence.datamodel.Participation;
import de.tuclausthal.abgabesystem.persistence.datamodel.Points;
import de.tuclausthal.abgabesystem.persistence.datamodel.Submission;

/**
 * Data Access Object Interface for the Points-class
 * @author Sven Strickroth
 */
public interface PointsDAOIf {
	/**
	 * Creates a points-instance for a specific submission issued by a specific user/participation
	 * @param issuedPoints points issued by participation
	 * @param submission the submission to which the points should be added
	 * @param participation the participation of the issuer
	 * @return the (new or updated) points instance
	 */
	public Points createPoints(int issuedPoints, Submission submission, Participation participation);
}
