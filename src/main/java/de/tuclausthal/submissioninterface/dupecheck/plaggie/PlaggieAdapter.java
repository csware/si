/*
 *  Copyright (C) 2006 Aleksi Ahtiainen, Mikko Rahikainen.
 *  Copyright (C) 2009-2010, 2017, 2020-2024 Sven Strickroth <email@cs-ware.de> 
 *
 *  This file is part of the homework submission interface.
 *
 *  Plaggie is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published
 *  by the Free Software Foundation; either version 2 of the License,
 *  or (at your option) any later version.
 *
 *  Plaggie is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plaggie; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301  USA
 */

package de.tuclausthal.submissioninterface.dupecheck.plaggie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.dupecheck.DupeCheck;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SimilarityDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.util.Util;
import plag.parser.CachingSimpleSubmissionSimilarityChecker;
import plag.parser.CodeExcluder;
import plag.parser.CodeTokenizer;
import plag.parser.Debug;
import plag.parser.DirectorySubmission;
import plag.parser.ExcludeFilenameFilter;
import plag.parser.ExistingCodeExcluder;
import plag.parser.MultipleCodeExcluder;
import plag.parser.MultipleFilenameFilter;
import plag.parser.SimpleSubmissionSimilarityChecker;
import plag.parser.SimpleTokenSimilarityChecker;
import plag.parser.SubdirectoryFilter;
import plag.parser.Submission;
import plag.parser.SubmissionDetectionResult;
import plag.parser.SubmissionSimilarityChecker;
import plag.parser.TokenList;
import plag.parser.TokenSimilarityChecker;
import plag.parser.java.InterfaceCodeExcluder;
import plag.parser.plaggie.Configuration;

/**
 * Plaggie adapter plagiarism test implementation
 * @author Sven Strickroth
 *
 */
public class PlaggieAdapter extends DupeCheck {
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	final static public String CONFIG_FILE_NAME = "plaggie.properties";

	public PlaggieAdapter(final Path path) {
		super(path);
	}

	private Configuration config = null;
	private CodeTokenizer codeTokenizer = null;

	@Override
	public void performDupeCheck(SimilarityTest similarityTest, Session session) {
		SimilarityDAOIf similarityDAO = DAOFactory.SimilarityDAOIf(session);
		Task task = similarityTest.getTask();
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Transaction tx = session.beginTransaction();
		DAOFactory.SimilarityTestDAOIf(session).resetSimilarityTest(similarityTest);
		tx.commit();
		try {
			// -- Read the configuration file
			config = new Configuration(path.resolve(CONFIG_FILE_NAME).toFile());

			config.htmlReport = false;
			config.minimumSubmissionSimilarityValue = similarityTest.getMinimumDifferenceInPercent() / 100.0;
			config.excludeFiles = similarityTest.getExcludeFiles();

			Debug.setEnabled(config.debugMessages);

			final Path file1 = Util.constructPath(path, task);

			// -- Create the code tokenizer object for parsing the source code files
			codeTokenizer = (CodeTokenizer) Class.forName(config.codeTokenizer).getDeclaredConstructor().newInstance();

			// -- Read and create the submissions, if the results are not
			// -- read from a file
			ArrayList<SubmissionDetectionResult> detResults = null;
			ArrayList<DirectorySubmission> submissions = null;

			// Create the submissions
			submissions = getDirectorySubmissions(file1);

			// -- Create the detection results
			detResults = generateDetectionResults(submissions);
			Iterator<SubmissionDetectionResult> resultsIterator = detResults.iterator();

			tx = session.beginTransaction();
			while (resultsIterator.hasNext()) {
				SubmissionDetectionResult detResult = resultsIterator.next();
				double maxSimilarity = detResult.getMaxFileSimilarityProduct() * 100.0;
				if (maxSimilarity >= similarityTest.getMinimumDifferenceInPercent()) {
					similarityDAO.addSimilarityResult(similarityTest, submissionDAO.getSubmission(Util.parseInteger(detResult.getSubmissionA().getName(), 0)), submissionDAO.getSubmission(Util.parseInteger(detResult.getSubmissionB().getName(), 0)), (int) maxSimilarity);
				}
			}
			DAOFactory.SimilarityTestDAOIf(session).finish(similarityTest);
			tx.commit();
		} catch (Exception e) {
			LOG.error("Plaggy failed", e);
		}
	}

	/**
	 * Generates a list of detection results using the given list of
	 * submissions. Not all detectoin results are stored in the
	 * returned list, they are filtered out according to some
	 * configuration parameters.
	 */
	@SuppressWarnings("rawtypes")
	private ArrayList<SubmissionDetectionResult> generateDetectionResults(ArrayList<DirectorySubmission> submissions) throws Exception {

		ArrayList<SubmissionDetectionResult> detResults = null;

		// Generate the black list
		final HashMap<String, Integer> blacklist = new HashMap<>();
		if (!(config.blacklistFile == null || config.blacklistFile.equals(""))) {
			try (BufferedReader bin = new BufferedReader(new FileReader(config.blacklistFile))) {
				String s;
				final Integer dummy = 0;
				while ((s = bin.readLine()) != null) {
					blacklist.put(s.toUpperCase(), dummy);
				}
			} catch (FileNotFoundException e) {
				// do nothing
			}

		}

		// Create the code excluder
		final ArrayList<CodeExcluder> codeExcluders = new ArrayList<>();

		if (config.excludeInterfaces) {
			codeExcluders.add(new InterfaceCodeExcluder());
		}

		final StringTokenizer tokenizer = new StringTokenizer(config.templates, ",");

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			CodeExcluder cE = new ExistingCodeExcluder(createTokenList(token, codeTokenizer), config.minimumMatchLength);
			codeExcluders.add(cE);
		}

		CodeExcluder codeExcluder = new MultipleCodeExcluder(codeExcluders);

		TokenSimilarityChecker tokenChecker = new SimpleTokenSimilarityChecker(config.minimumMatchLength, codeExcluder);

		// No file excluders currently used, therefore null's
		SubmissionSimilarityChecker checker;
		if (config.cacheTokenLists) {
			checker = new CachingSimpleSubmissionSimilarityChecker(tokenChecker, codeTokenizer, new HashMap());
		} else {
			checker = new SimpleSubmissionSimilarityChecker(tokenChecker, codeTokenizer);
		}

		// Generate all the submission detection results

		detResults = new ArrayList<>();

		for (int i = 0; i < submissions.size(); i++) {
			for (int j = 0; j < i; j++) {
				Submission subA = submissions.get(i);
				Submission subB = submissions.get(j);
				SubmissionDetectionResult detResult = new SubmissionDetectionResult(subA, subB, checker, config.minimumFileSimilarityValueToReport);

				boolean onBlacklist = false;
				if (blacklist.get(detResult.getSubmissionA().getName().toUpperCase()) != null) {
					detResult.setBlacklistedA(true);
					onBlacklist = true;
				}
				if (blacklist.get(detResult.getSubmissionB().getName().toUpperCase()) != null) {
					detResult.setBlacklistedB(true);
					onBlacklist = true;
				}

				boolean alreadyAdded = false;
				if (onBlacklist) {
					if (config.showAllBlacklistedResults) {
						detResults.add(detResult);
						alreadyAdded = true;
					}
				}

				if ((detResult.getSimilarityA() >= config.minimumSubmissionSimilarityValue) || (detResult.getSimilarityB() >= config.minimumSubmissionSimilarityValue)) {
					if (!alreadyAdded) {
						detResults.add(detResult);
					}
				}
			}
		}
		return detResults;

	}

	/**
	 * Returns a list of submissions, that can be found in the directory
	 * given as a parameter. The exact format of the directory
	 * hierarchy depends on the configuration, especially parameter
	 * severalSubmissionDirectories.
	 */
	private ArrayList<DirectorySubmission> getDirectorySubmissions(final Path directory) throws Exception {
		ArrayList<DirectorySubmission> submissions = new ArrayList<>();

		FilenameFilter filter = generateFilenameFilter();

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
			for (final Path file : directoryStream) {
				if (!Files.isDirectory(file) || !Util.isInteger(file.getFileName().toString())) {
					continue;
				}
				Debug.println("Adding directory submission: " + file.toString());
				submissions.add(new DirectorySubmission(file.toFile(), filter, config.useRecursive));
			}
		}
		return submissions;
	}

	/**
	 * Generates a FilenameFilter according to the configuration.
	 */
	private FilenameFilter generateFilenameFilter() throws Exception {

		// Generate the filename filter
		ArrayList<FilenameFilter> filters = new ArrayList<>();

		filters.add((FilenameFilter) Class.forName(config.filenameFilter).getDeclaredConstructor().newInstance());

		filters.add(new ExcludeFilenameFilter(config.excludeFiles));
		filters.add(new SubdirectoryFilter(config.excludeSubdirectories));

		FilenameFilter filter = new MultipleFilenameFilter(filters);
		return filter;
	}

	/**
	 * Returns the token list of the given file.
	 */
	private static TokenList createTokenList(String filename, CodeTokenizer tokenizer) throws Exception {
		TokenList tokens = tokenizer.tokenize(new File(filename));
		return tokens;
	}

	@Override
	protected int calculateSimilarity(StringBuffer fileOne, StringBuffer fileTwo, int maxSimilarity) throws IOException {
		return 0;
	}
}
