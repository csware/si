/*
 * Copyright 2009-2010, 2017, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.dupecheck;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.NormalizerCache;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SimilarityDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Plagiarism test
 * @author Sven Strickroth
 */
public abstract class DupeCheck {
	public static int CORES = 1;

	protected File path;

	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
	 * @param session 
	 */
	public void performDupeCheck(SimilarityTest similarityTest, Session session) {
		DAOFactory.SimilarityTestDAOIf(session).resetSimilarityTest(similarityTest);
		Task task = similarityTest.getTask();
		File taskPath = new File(path.getAbsolutePath() + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid());
		NormalizerCache normalizerCache = null;
		try {
			normalizerCache = new NormalizerCache(taskPath, similarityTest.getNormalizer());
		} catch (IOException ex) {
			LOG.error("Could not initialize normalizer cache", ex);
			// if we get an error here, don't mark run as completed
			return;
		}
		List<Submission> submissions = new ArrayList<>(task.getSubmissions());
		List<String> excludedFileNames = Arrays.asList(similarityTest.getExcludeFiles().split(","));

		ExecutorService executorService = Executors.newFixedThreadPool(CORES);
		// go through all submissions
		for (int outerI = 0; outerI < submissions.size(); ++outerI) {
			final NormalizerCache cache = normalizerCache;
			final int i = outerI;
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					Session session = HibernateSessionHelper.getSessionFactory().openSession();
					SimilarityDAOIf similarityDAO = DAOFactory.SimilarityDAOIf(session);
					List<StringBuffer> javaFiles = new ArrayList<>();
					for (String javaFile : Util.listFilesAsRelativeStringList(new File(taskPath, String.valueOf(submissions.get(i).getSubmissionid())), excludedFileNames)) {
						// cache the files we use more than once
						try {
							javaFiles.add(cache.normalize(submissions.get(i).getSubmissionid() + System.getProperty("file.separator") + javaFile));
						} catch (IOException e) {
							// skip single file
							LOG.error("Skipping file", e);
						}
					}
					// the similariy matrix is symmetric, so it's sufficient
					// to only calculate the upper triangular matrix entries
					for (int j = i + 1; j < submissions.size(); ++j) {
						int maxSimilarity = 0;
						// go through all submitted files of submission j
						for (String javaFile : Util.listFilesAsRelativeStringList(new File(taskPath, String.valueOf(submissions.get(j).getSubmissionid())), excludedFileNames)) {
							// compare all files, file by file
							for (StringBuffer fileOne : javaFiles) {
								StringBuffer fileTwo = null;
								try {
									fileTwo = cache.normalize(submissions.get(j).getSubmissionid() + System.getProperty("file.separator") + javaFile);
									maxSimilarity = Math.max(maxSimilarity, calculateSimilarity(fileOne, fileTwo, 100 - similarityTest.getMinimumDifferenceInPercent()));
								} catch (IOException e) {
									// skip single file
									LOG.error("Skipping file", e);
								}
								if (maxSimilarity == 100) {
									break;
								}
							}
							if (maxSimilarity == 100) {
								break;
							}
						}
						if (similarityTest.getMinimumDifferenceInPercent() <= maxSimilarity) {
							similarityDAO.addSimilarityResult(similarityTest, submissions.get(i), submissions.get(j), maxSimilarity);
						}
					}
					session.close();
				}
			});
		}
		executorService.shutdown();
		try {
			while (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
				LOG.debug("Waiting for all analyses to finish: " + executorService.toString());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		DAOFactory.SimilarityTestDAOIf(session).finish(similarityTest);
		normalizerCache.cleanUp();
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
