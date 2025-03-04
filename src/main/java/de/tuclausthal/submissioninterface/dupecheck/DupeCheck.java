/*
 * Copyright 2009-2010, 2017, 2020-2024 Sven Strickroth <email@cs-ware.de>
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

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.hibernate.Session;
import org.hibernate.Transaction;
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

	final protected Path path;

	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Creates a new DupeCheck instance
	 * @param path the path where the submissions are located
	 */
	public DupeCheck(final Path path) {
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
		Transaction tx = session.beginTransaction();
		DAOFactory.SimilarityTestDAOIf(session).resetSimilarityTest(similarityTest);
		tx.commit();
		Task task = similarityTest.getTask();
		final Path taskPath = Util.constructPath(path, task);
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

		final ExecutorService executorService = Executors.newFixedThreadPool(CORES);
		// go through all submissions
		for (int outerI = 0; outerI < submissions.size(); ++outerI) {
			final NormalizerCache cache = normalizerCache;
			final int i = outerI;
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					final Session innerSession = HibernateSessionHelper.getSessionFactory().openSession();
					final SimilarityDAOIf similarityDAO = DAOFactory.SimilarityDAOIf(innerSession);
					List<StringBuffer> javaFiles = new ArrayList<>();
					for (final String javaFile : Util.listFilesAsRelativeStringList(taskPath.resolve(String.valueOf(submissions.get(i).getSubmissionid())), excludedFileNames)) {
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
						for (final String javaFile : Util.listFilesAsRelativeStringList(taskPath.resolve(String.valueOf(submissions.get(j).getSubmissionid())), excludedFileNames)) {
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
							final Transaction tx2 = innerSession.beginTransaction();
							similarityDAO.addSimilarityResult(similarityTest, submissions.get(i), submissions.get(j), maxSimilarity);
							tx2.commit();
						}
					}
					innerSession.close();
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
		tx = session.beginTransaction();
		DAOFactory.SimilarityTestDAOIf(session).finish(similarityTest);
		tx.commit();
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
