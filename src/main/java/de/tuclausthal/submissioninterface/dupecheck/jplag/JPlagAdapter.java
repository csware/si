/*
 *  Copyright (C) 2020-2024 Sven Strickroth <email@cs-ware.de> 
 *
 * This file is part of the GATE.
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

package de.tuclausthal.submissioninterface.dupecheck.jplag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.dupecheck.DupeCheck;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SimilarityDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.testframework.tests.impl.ProcessOutputGrabber;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * JPlag adapter plagiarism test implementation
 * @author Sven Strickroth
 *
 */
public class JPlagAdapter extends DupeCheck {
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	final static public String JPLAG_JAR = "jplag.jar";

	public JPlagAdapter(final Path path) {
		super(path);
	}

	@Override
	public void performDupeCheck(SimilarityTest similarityTest, Session session) {
		SimilarityDAOIf similarityDAO = DAOFactory.SimilarityDAOIf(session);
		Task task = similarityTest.getTask();
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Transaction tx = session.beginTransaction();
		DAOFactory.SimilarityTestDAOIf(session).resetSimilarityTest(similarityTest);
		tx.commit();
		Path tempDir = null;
		try {
			tempDir = Util.createTemporaryDirectory("jplag");
			if (tempDir == null) {
				throw new IOException("Failed to create tempdir!");
			}
			final Path resultsDir = tempDir.resolve("results");
			Files.createDirectory(resultsDir);

			final Path taskPath = Util.constructPath(path, task);

			final Path jplagSubmissionsDir = tempDir.resolve("submissions");
			Files.createDirectory(jplagSubmissionsDir);
			try (DirectoryStream<Path> taskDirStream = Files.newDirectoryStream(taskPath)) {
				for (final Path file : taskDirStream) {
					if (!Files.isDirectory(file) || !Util.isInteger(file.getFileName().toString())) {
						continue;
					}
					Util.recursiveCopy(file, jplagSubmissionsDir.resolve(file.getFileName()));
				}
			}

			List<String> params = new ArrayList<>();
			params.add("java");
			params.add("-jar");
			params.add(path.resolve(JPLAG_JAR).toAbsolutePath().toString());
			params.add("-s");
			if (similarityTest.getExcludeFiles() != null && !similarityTest.getExcludeFiles().isEmpty()) {
				final Path excludeFile = tempDir.resolve("exclude.txt");
				try (BufferedWriter bw = Files.newBufferedWriter(excludeFile)) {
					String[] excludedFiles = similarityTest.getExcludeFiles().split(",");
					for (String file : excludedFiles) {
						bw.write(file);
						bw.newLine();
					}
					params.add("-x");
					params.add(excludeFile.toAbsolutePath().toString());
				}
			}
			params.add("-m");
			params.add(similarityTest.getMinimumDifferenceInPercent() + "%");
			params.add("-l");
			params.add("java19");
			params.add("-r");
			params.add(resultsDir.toString());
			params.add(jplagSubmissionsDir.toString());

			ProcessBuilder pb = new ProcessBuilder(params);
			pb.directory(tempDir.toFile());
			LOG.debug("Executing external process: {} in {}", params, tempDir);
			Process process = pb.start();
			ProcessOutputGrabber outputGrapper = new ProcessOutputGrabber(process);
			int exitValue = -1;
			try {
				exitValue = process.waitFor();
			} catch (InterruptedException e) {
			}
			outputGrapper.waitFor();
			if (exitValue == 0) {
				try (BufferedReader resultsFile = Files.newBufferedReader(resultsDir.resolve("matches_avg.csv"))) {
					tx = session.beginTransaction();
					String line;
					while ((line = resultsFile.readLine()) != null) {
						String[] results = line.split(";");
						if (results.length < 4 || (results.length - 1) % 3 != 0) {
							LOG.error("invalid line in JPlag results file: " + line);
							continue;
						}
						Submission submissionOne = submissionDAO.getSubmission(Integer.parseInt(results[0]));
						for (int i = 0; i < (results.length - 1) / 3; ++i) {
							float similarity = Float.parseFloat(results[(i * 3) + 1 + 2]);
							similarityDAO.addSimilarityResult(similarityTest, submissionOne, submissionDAO.getSubmission(Integer.parseInt(results[(i * 3) + 1 + 1])), Math.round(similarity));
						}
					}
					DAOFactory.SimilarityTestDAOIf(session).finish(similarityTest);
					tx.commit();
				}
			} else {
				LOG.error("Executing \"" + path.resolve(JPLAG_JAR).toAbsolutePath().toString() + "\" failed: Exit code: " + exitValue);
				LOG.error(outputGrapper.getStdErrBuffer().toString());
			}
		} catch (Exception e) {
			LOG.error("Error executing JPlag", e);
		} finally {
			if (tempDir != null) {
				Util.recursiveDelete(tempDir);
			}
		}
	}

	@Override
	protected int calculateSimilarity(StringBuffer fileOne, StringBuffer fileTwo, int maxSimilarity) throws IOException {
		return 0;
	}
}
