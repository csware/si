/*
 * Copyright 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.datamodel;

import java.time.ZonedDateTime;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;

public enum ModelSolutionProvisionType {
	/** not available for students */
	INTERNAL("nur für TutorInnen, nicht für Studierende"),
	/** available for all students after points deadline */
	ALL_STUDENTS("alle Studierende der Vorlesung (nach Freigabe der Punkte)"),
	/** available for all students after points deadline who submitted a solution */
	SUBMITTING_STUDENTS("alle Studierende, die eine Abgabe durchgeführt haben (nach Freigabe der Punkte)"),
	/** available for all students after points deadline who submitted a solution, the points are valid and did not plagiarize */
	CONFIRMED_STUDENTS("alle Studierende, die nicht plagigiert und die Abnahme bestanden haben (nach Freigabe der Punkte)");

	private String info;

	private ModelSolutionProvisionType(String info) {
		this.info = info;
	}

	public String getInfo() {
		return info;
	}

	static public boolean canStudentAccessModelSolution(Task task, Submission submission, User user, Session session) {
		if (task.getModelSolutionProvisionType() == ModelSolutionProvisionType.INTERNAL) {
			return false;
		}
		if (task.getShowPoints() == null || task.getDeadline().isAfter(ZonedDateTime.now()) || task.getShowPoints().isAfter(ZonedDateTime.now())) {
			return false;
		} else if (task.getModelSolutionProvisionType() == ModelSolutionProvisionType.ALL_STUDENTS) {
			return true;
		}
		if (submission == null && user != null && session != null) {
			SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
			submission = submissionDAO.getSubmission(task, user);
		}
		if (submission == null) {
			return false;
		} else if (task.getModelSolutionProvisionType() == ModelSolutionProvisionType.SUBMITTING_STUDENTS) {
			return true;
		}
		return submission.isPointsVisibleToStudents() && submission.getPoints().getPointStatus() == PointStatus.ABGENOMMEN.ordinal() && submission.getPoints().getDuplicate() == null;
	}
}
