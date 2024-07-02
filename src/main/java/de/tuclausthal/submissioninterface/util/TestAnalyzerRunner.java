/*
 * Copyright 2009, 2020-2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.testanalyzer.CommonErrorAnalyzer;
import de.tuclausthal.submissioninterface.testframework.TestExecutor;
import de.tuclausthal.submissioninterface.testframework.executor.impl.LocalExecutor;
import de.tuclausthal.submissioninterface.testframework.tests.TestTask;

/**
 * Test runner
 * @author Sven Strickroth
 */
public class TestAnalyzerRunner {
	/**
	 * @param args the first argument must be to path to the submissions
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		HibernateSessionHelper.getSessionFactory();
		final Path dataPath;
		if (args.length != 2 || (dataPath = Path.of(args[0])) == null || !Files.isDirectory(dataPath)) {
			System.out.println("first parameter must point to the submission directory");
			System.out.println("second parameter must be a task number");
			System.exit(1);
			return; // not needed, but to make Eclipse happy
		}
		final Session session = HibernateSessionHelper.getSessionFactory().openSession();
		final Task task = DAOFactory.TaskDAOIf(session).getTask(Integer.parseInt(args[1]));
		if (task == null) {
			System.err.println("Task not found.");
			System.exit(1);
			return;
		}

		LocalExecutor.CORES = (Runtime.getRuntime().availableProcessors() > 4) ? Runtime.getRuntime().availableProcessors() - 2 : 2;
		LocalExecutor.dataPath = dataPath;
		LocalExecutor.getInstance();
		Transaction tx = session.beginTransaction();
		task.setDeadline(ZonedDateTime.now());
		tx.commit();
		for (final Test test : task.getTests()) {
			for (final Submission submission : test.getTask().getSubmissions()) {
				TestExecutor.executeTask(new TestTask(test, submission, true));
			}
		}
		TestExecutor.shutdown();

		tx = session.beginTransaction();
		DAOFactory.CommonErrorDAOIf(session).reset(task);
		final CommonErrorAnalyzer analyzer = new CommonErrorAnalyzer(session);
		for (final Submission sub : task.getSubmissions()) {
			for (final TestResult testResult : sub.getTestResults()) {
				analyzer.runAnalysis(testResult);
			}
		}
		tx.commit();

		session.close();
	}
}
