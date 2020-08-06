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

package de.tuclausthal.submissioninterface.util;

import java.io.File;
import java.io.IOException;

import de.tuclausthal.submissioninterface.dupecheck.DupeCheck;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.testframework.TestExecutor;
import de.tuclausthal.submissioninterface.testframework.executor.impl.LocalExecutor;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;

/**
 * Test runner
 * @author Sven Strickroth
 */
public class TestRunner {
	/**
	 * @param args the first argument must be to path to the submissions
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		HibernateSessionHelper.getSessionFactory();
		if (args.length != 1 || !new File(args[0]).isDirectory()) {
			System.out.println("first parameter must point to the submission directory");
			System.exit(1);
		}
		File dataPath = new File(args[0]);
		SimilarityTest similarityTest;
		while ((similarityTest = DAOFactory.SimilarityTestDAOIf(HibernateSessionHelper.getSessionFactory().openSession()).takeSimilarityTest()) != null) {
			DupeCheck dupeCheck = similarityTest.getDupeCheck(dataPath);
			dupeCheck.performDupeCheck(similarityTest);
		}
		LocalExecutor.CORES = 2;
		LocalExecutor.dataPath = dataPath;
		LocalExecutor.getInstance();
		Test test;
		while ((test = DAOFactory.TestDAOIf(HibernateSessionHelper.getSessionFactory().openSession()).takeTest()) != null) {
			for (Submission submission : test.getTask().getSubmissions()) {
				TestExecutor.executeTask(new TestTask(test, submission, true));
			}
		}
		TestExecutor.shutdown();
	}
}
