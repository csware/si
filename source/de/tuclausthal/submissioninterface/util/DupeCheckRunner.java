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

/**
 * Plagiarism test runner
 * @author Sven Strickroth
 */
public class DupeCheckRunner {
	/**
	 * @param args the first argument must be to path to the submissions
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 1 || !new File(args[0]).isDirectory()) {
			System.out.println("first parameter must point to the submission directory");
			System.exit(1);
		}
		SimilarityTest similarityTest;
		while ((similarityTest = DAOFactory.SimilarityTestDAOIf().takeSimilarityTest()) != null) {
			DupeCheck dupeCheck = similarityTest.getDupeCheck(new File(args[0]));
			dupeCheck.performDupeCheck(similarityTest);
		}
	}
}
