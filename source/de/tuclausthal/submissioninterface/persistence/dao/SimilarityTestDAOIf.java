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

import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

/**
 * Data Access Object Interface for the SimilarityTest-class
 * @author Sven Strickroth
 */
public interface SimilarityTestDAOIf {
	/**
	 * Adds and stores a new similarity test in the database.
	 * @param task
	 * @param type
	 * @param basis
	 * @param normalizeCapitalization
	 * @param tabsSpacesNewlinesNormalization
	 * @param minimumDifferenceInPercent
	 * @param excludeFiles Comma separated list of filesnames to exclude
	 * @return the similarity test
	 */
	public SimilarityTest addSimilarityTest(Task task, String type, String basis, boolean normalizeCapitalization, String tabsSpacesNewlinesNormalization, int minimumDifferenceInPercent, String excludeFiles);

	/**
	 * Retireves a similarity test from the db by a given id.
	 * @param similarityTestId
	 * @return the similarity test or null
	 */
	public SimilarityTest getSimilarityTest(int similarityTestId);

	/**
	 * Removes the given similarity test from the db.
	 * @param similarityTest
	 */
	public void deleteSimilarityTest(SimilarityTest similarityTest);

	/**
	 * Rests all results for the given similarity test
	 * @param similarityTest
	 */
	public void resetSimilarityTest(SimilarityTest similarityTest);

	/**
	 * Checks if a similarity test is ready to run
	 * @return SimilarityTest or null if none is "queued".
	 */
	public SimilarityTest takeSimilarityTest();
}
