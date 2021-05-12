/*
 * Copyright 2009-2011, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import java.util.List;
import java.util.Map;

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointHistory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

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
	 * @param publicComment 
	 * @param internalComment 
	 * @param pointStatus 
	 * @param duplicate see definition in Points class
	 * @return the (new or updated) points instance
	 */
	public Points createPoints(int issuedPoints, Submission submission, Participation participation, String publicComment, String internalComment, PointStatus pointStatus, Integer duplicate);

	public Points createPoints(List<Integer> pointGiven, Submission submission, Participation participation, String publicComment, String internalComment, PointStatus pointStatus, Integer duplicate);

	public Points createPointsFromRequestParameters(Map<String, String[]> pointGiven, Submission submission, Participation participation, String publicComment, String internalComment, PointStatus pointStatus, Integer duplicate);

	Points createMCPoints(int issuedPoints, Submission submission, String publicComment, PointStatus pointStatus);

	List<PointHistory> getPointHistoryForSubmission(Submission submission);

	public Map<Integer, Integer> getAllPointsForLecture(Lecture lecture);

	public Map<Integer, Integer> getUngradedSubmissionsPerTasks(Lecture lecture);
}
