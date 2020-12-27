/*
 * Copyright 2009, 2021 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

/**
 * Data Access Object Interface for the Similarity-class 
 * @author Sven Strickroth
 */
public interface SimilarityDAOIf {
	/**
	 * Stores a similaty test result in the database
	 * @param similarityTest the similarity test
	 * @param submissionOne
	 * @param submissionTwo
	 * @param percentage the similarity in per cent
	 */
	public void addSimilarityResult(SimilarityTest similarityTest, Submission submissionOne, Submission submissionTwo, int percentage);

	/**
	 * Returns the similarities of a given submission to other submissions
	 * @param similarityTest
	 * @param submission
	 * @return list of similarity
	 */
	public List<Similarity> getUsersWithSimilarity(SimilarityTest similarityTest, Submission submission);

	/**
	 * Returns the maximum similarities of a given submission to other submissions
	 * @param similarityTest
	 * @param submission
	 * @return the list of maximum similarities
	 */
	public List<Similarity> getUsersWithMaxSimilarity(SimilarityTest similarityTest, Submission submission);

	public Map<Integer, Map<Integer, List<Similarity>>> getMaxSimilarities(Task task);
}
