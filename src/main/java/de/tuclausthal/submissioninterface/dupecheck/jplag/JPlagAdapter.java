/* 
 *  Copyright (C) 2020 Sven Strickroth <email@cs-ware.de> 
 * 
 * This file is part of the SubmissionInterface.
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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.dupecheck.DupeCheck;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SimilarityDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.testframework.tests.impl.ProcessOutputGrabber;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * JPlag adapter plagiarism test implementation
 * @author Sven Strickroth
 *
 */
public class JPlagAdapter extends DupeCheck {
	public JPlagAdapter(File path) {
		super(path);
	}

	@Override
	public void performDupeCheck(SimilarityTest similarityTest) {
		Session session = HibernateSessionHelper.getSessionFactory().openSession();
		SimilarityDAOIf similarityDAO = DAOFactory.SimilarityDAOIf(session);
		Task task = similarityTest.getTask();
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		DAOFactory.SimilarityTestDAOIf(session).resetSimilarityTest(similarityTest);
		File tempDir = null;
		try {
			tempDir = Util.createTemporaryDirectory("jplag", null);
			if (tempDir == null) {
				throw new IOException("Failed to create tempdir!");
			}
			File resultsDir = new File(tempDir, "results");
			resultsDir.mkdir();

			File taskPath = new File(path + System.getProperty("file.separator") + task.getTaskGroup().getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator"));

			File submissionsDir;
			if (new File(taskPath, "advisorfiles").exists()) {
				submissionsDir = new File(tempDir, "submissions");
				submissionsDir.mkdir();
				for (File file : taskPath.listFiles()) {
					if (!file.isDirectory()) {
						continue;
					}
					try {
						Integer.parseInt(file.getName());
					} catch (NumberFormatException e) {
						// we just want to handle submission-directories
						continue;
					}
					Util.recursiveCopy(file, new File(submissionsDir, file.getName()));
				}
			} else {
				submissionsDir = taskPath;
			}

			List<String> params = new ArrayList<>();
			params.add("java");
			params.add("-jar");
			params.add(new File(path, "jplag.jar").toString());
			params.add("-s");
			if (similarityTest.getExcludeFiles() != null && !similarityTest.getExcludeFiles().isEmpty()) {
				File excludeFile = new File(tempDir, "exclude.txt");
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(excludeFile))) {
					String[] excludedFiles = similarityTest.getExcludeFiles().split(",");
					for (String file : excludedFiles) {
						bw.write(file);
						bw.newLine();
					}
					params.add("-x");
					params.add(excludeFile.toString());
				}
			}
			params.add("-m");
			params.add(similarityTest.getMinimumDifferenceInPercent() + "%");
			params.add("-l");
			params.add("java19");
			params.add("-r");
			params.add(resultsDir.toString());
			params.add(submissionsDir.toString());

			ProcessBuilder pb = new ProcessBuilder(params);
			pb.directory(tempDir);
			Process process = pb.start();
			ProcessOutputGrabber outputGrapper = new ProcessOutputGrabber(process);
			int exitValue = -1;
			try {
				exitValue = process.waitFor();
			} catch (InterruptedException e) {
			}
			outputGrapper.waitFor();
			if (exitValue == 0) {
				try (BufferedReader resultsFile = new BufferedReader(new FileReader(new File(resultsDir, "matches_avg.csv")))) {
					String line;
					while ((line = resultsFile.readLine()) != null) {
						String[] results = line.split(";");
						if (results.length < 4 || (results.length - 1) % 3 != 0) {
							System.err.println("invalid line in JPlag results file: " + line);
							continue;
						}
						Submission submissionOne = submissionDAO.getSubmission(Integer.parseInt(results[0]));
						for (int i = 0; i < (results.length - 1) / 3; ++i) {
							float similarity = Float.parseFloat(results[(i * 3) + 1 + 2]);
							similarityDAO.addSimilarityResult(similarityTest, submissionOne, submissionDAO.getSubmission(Integer.parseInt(results[(i * 3) + 1 + 1])), Math.round(similarity));
						}
					}
					DAOFactory.SimilarityTestDAOIf(session).finish(similarityTest);
				}
			} else {
				System.err.println("Executing \"" + new File(path, "jplag.jar").toString() + "\" failed: Exit code: " + exitValue);
				System.err.println(outputGrapper.getStdErrBuffer().toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
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
