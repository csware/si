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

package de.tuclausthal.submissioninterface.dupecheck;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.NormalizerCache;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SimilarityDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

/**
 * Plagiarism test
 * @author Sven Strickroth
 */
public abstract class DupeCheck {
	protected File path;

	/**
	 * Creates a new DupeCheck instance
	 * @param path the path where the submissions are located
	 */
	public DupeCheck(File path) {
		this.path = path;
	}

	/**
	 * Performs a Duplicatecheck as specified in similarityTest
	 * If not overridden, it iterates over all submissions and calls
	 * calculateSimilarity for similarity calculation
	 * @param similarityTest test to perform
	 */
	public void performDupeCheck(SimilarityTest similarityTest) {
		SimilarityDAOIf similarityDAO = DAOFactory.SimilarityDAOIf();
		DAOFactory.SimilarityTestDAOIf().resetSimilarityTest(similarityTest);
		NormalizerCache normalizerCache = null;
		try {
			normalizerCache = new NormalizerCache(similarityTest.getNormalizer());
			Task task = similarityTest.getTask();
			List<Submission> submissions = new LinkedList<Submission>(task.getSubmissions());
			List<String> excludedFileNames = Arrays.asList(similarityTest.getExcludeFiles().split(","));
			// go through all submission of submission i
			for (int i = 0; i < submissions.size(); i++) {
				List<StringBuffer> javaFiles = new LinkedList<StringBuffer>();
				if (new File(path.getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submissions.get(i).getSubmissionid() + System.getProperty("file.separator")).listFiles() != null) {
					for (File javaFile : new File(path.getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submissions.get(i).getSubmissionid() + System.getProperty("file.separator")).listFiles()) {
						// check that the file is a java-file and is not excluded
						if (!javaFile.getName().endsWith(".class") && !excludedFileNames.contains(javaFile.getName())) {
							// cache the files we use more than once
							javaFiles.add(normalizerCache.normalize(submissions.get(i), javaFile));
						}
					}
					// the similariy matrix is symmetric, so it's sufficient
					// to only calculate the upper triangular matrix entries
					for (int j = i + 1; j < submissions.size(); j++) {
						int maxSimilarity = 0;
						// go through all submitted files of submission j
						if (new File(path.getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submissions.get(j).getSubmissionid() + System.getProperty("file.separator")).listFiles() != null) {
							for (File javaFile : new File(path.getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submissions.get(j).getSubmissionid() + System.getProperty("file.separator")).listFiles()) {
								// check that the file is a java-file and is not excluded
								if (!javaFile.getName().endsWith(".class") && !excludedFileNames.contains(javaFile.getName())) {
									// compare all files, file by file
									for (StringBuffer fileOne : javaFiles) {
										StringBuffer fileTwo = normalizerCache.normalize(submissions.get(j), javaFile);
										maxSimilarity = Math.max(maxSimilarity, calculateSimilarity(fileOne, fileTwo, 100 - similarityTest.getMinimumDifferenceInPercent()));
										if (maxSimilarity == 100) {
											break;
										}
									}
									if (maxSimilarity == 100) {
										break;
									}
								}
							}
							if (similarityTest.getMinimumDifferenceInPercent() <= maxSimilarity) {
								similarityDAO.addSimilarityResult(similarityTest, submissions.get(i), submissions.get(j), maxSimilarity);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (normalizerCache != null) {
				normalizerCache.cleanUp();
			}
		}
	}

	/**
	 * Calculates the similarity of two files
	 * @param fileOne
	 * @param fileTwo
	 * @param maximumDifferenceInPercent the maximum difference in per cent (a trashold to skip too weak similarities)
	 * @return similarity value in per cent
	 * @throws IOException
	 */
	protected abstract int calculateSimilarity(StringBuffer fileOne, StringBuffer fileTwo, int maximumDifferenceInPercent) throws IOException;
}
