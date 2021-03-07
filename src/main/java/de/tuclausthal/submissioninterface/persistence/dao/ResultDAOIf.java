/*
 * Copyright 2011 Giselle Rodriguez
 * Copyright 2011 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

/**
 * Data Access Object Interface for the Results-class
 * @author Giselle Rodriguez
 * @author Sven Strickroth
 */
public interface ResultDAOIf {
	/**
	 * Creates and stores a the results in the DB
	 * @param submission the submission
	 * @param results the results of a submission
	 */
	public void createResults(Submission submission, List<String> results);

	/**
	 * Returns the results for a submission
	 * @param submission the submission
	 * @return the list of results
	 */
	public List<String> getResultsForSubmission(Submission submission);
}
