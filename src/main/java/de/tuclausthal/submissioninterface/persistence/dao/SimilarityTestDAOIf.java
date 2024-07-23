/*
 * Copyright 2009-2010, 2017, 2022, 2024 Sven Strickroth <email@cs-ware.de>
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
	SimilarityTest addSimilarityTest(Task task, String type, String basis, boolean normalizeCapitalization, String tabsSpacesNewlinesNormalization, int minimumDifferenceInPercent, String excludeFiles);

	/**
	 * Retireves a similarity test from the db by a given id.
	 * @param similarityTestId
	 * @return the similarity test or null
	 */
	SimilarityTest getSimilarityTest(int similarityTestId);

	SimilarityTest getSimilarityTestLocked(int similarityTestId);

	/**
	 * Removes the given similarity test from the db.
	 * @param similarityTest
	 */
	void deleteSimilarityTest(SimilarityTest similarityTest);

	/**
	 * Rests all results for the given similarity test
	 * ATTENTION: this method starts it's own transaction!
	 * @param similarityTest
	 */
	void resetSimilarityTest(SimilarityTest similarityTest);

	/**
	 * Checks if a similarity test is ready to run
	 * ATTENTION: this method starts it's own transaction!
	 * @return SimilarityTest or null if none is "queued".
	 */
	SimilarityTest takeSimilarityTestTransacted();

	/**
	 * Marks a test as finished
	 * ATTENTION: this method starts it's own transaction!
	 * @param similarityTest 
	 */
	void finish(SimilarityTest similarityTest);
}
