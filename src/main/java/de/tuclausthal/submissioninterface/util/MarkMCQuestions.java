/*
 * Copyright 2021 Sven Strickroth <email@cs-ware.de>
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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.MCOptionDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.ResultDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

/**
 * MC Question Marker
 * @author Sven Strickroth
 */
public class MarkMCQuestions {
	/**
	 * @param args the first parameter must be the task number
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("first parameter must be the task number");
			System.exit(1);
		}

		HibernateSessionHelper.getSessionFactory();
		Session session = HibernateSessionHelper.getSessionFactory().openSession();
		session.beginTransaction();

		Task task = DAOFactory.TaskDAOIf(session).getTask(Integer.parseInt(args[0]));
		if (task == null || !task.isSCMCTask()) {
			System.err.println("Task not found OR task is not a multiple-choice task.");
			System.exit(1);
			return;
		}

		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Submission> criteria = builder.createQuery(Submission.class);
		Root<Submission> root = criteria.from(Submission.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get(Submission_.task), task));

		ResultDAOIf resultDAO = DAOFactory.ResultDAOIf(session);
		MCOptionDAOIf mcOptionDAO = DAOFactory.MCOptionDAOIf(session);
		List<MCOption> options = mcOptionDAO.getMCOptionsForTask(task);
		List<Integer> correctOptions = options.stream().filter(option -> option.isCorrect()).map(option -> option.getId()).collect(Collectors.toList());

		for (Submission submission : session.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).list()) {
			// don't update or check submissions that already were marked manually
			if (submission.getPoints() != null && submission.getPoints().getIssuedBy() != null) {
				continue;
			}

			List<String> results = resultDAO.getResultsForSubmission(submission);
			boolean allCorrect = (results.size() == correctOptions.size()) && correctOptions.containsAll(results.stream().map(result -> Integer.parseInt(result)).collect(Collectors.toList()));

			int points = allCorrect ? task.getMaxPoints() : 0;
			if (submission.getPoints() != null && submission.getPoints().getPoints() == points) {
				continue;
			}
			DAOFactory.PointsDAOIf(session).createMCPoints(points, submission, "", task.getTaskGroup().getLecture().isRequiresAbhnahme() ? PointStatus.NICHT_ABGENOMMEN : PointStatus.ABGENOMMEN);
		}

		session.getTransaction().commit();
		session.close();
		HibernateSessionHelper.getSessionFactory().close();
	}
}
