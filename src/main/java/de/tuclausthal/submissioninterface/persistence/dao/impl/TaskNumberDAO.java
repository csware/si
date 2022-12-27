/*
 * Copyright 2011, 2020-2022 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.LockMode;
import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.TaskNumberDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber_;

/**
 * Data Access Object implementation for the TaskNumberDAOIf
 * @author Sven Strickroth
 */
public class TaskNumberDAO extends AbstractDAO implements TaskNumberDAOIf {
	public TaskNumberDAO(Session session) {
		super(session);
	}

	@Override
	public List<TaskNumber> getTaskNumbersForSubmission(Submission submission) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<TaskNumber> criteria = builder.createQuery(TaskNumber.class);
		Root<TaskNumber> root = criteria.from(TaskNumber.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get(TaskNumber_.submission), submission));
		criteria.orderBy(builder.asc(root.get(TaskNumber_.tasknumberid)));
		return session.createQuery(criteria).list();
	}

	@Override
	public List<TaskNumber> getTaskNumbersForTaskLocked(Task task, Participation participation) {
		Session session = getSession();
		session.lock(participation, LockMode.PESSIMISTIC_WRITE);
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<TaskNumber> criteria = builder.createQuery(TaskNumber.class);
		Root<TaskNumber> root = criteria.from(TaskNumber.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get(TaskNumber_.task), task));
		criteria.where(builder.equal(root.get(TaskNumber_.participation), participation));
		criteria.orderBy(builder.asc(root.get(TaskNumber_.tasknumberid)));
		return session.createQuery(criteria).list();
	}

	@Override
	public List<TaskNumber> assignTaskNumbersToSubmission(Submission submission, Participation participation) {
		// only assign new numbers if not already numbers exist
		List<TaskNumber> taskNumbers = getTaskNumbersForSubmission(submission);
		if (getTaskNumbersForSubmission(submission).isEmpty()) {
			taskNumbers = getTaskNumbersForTaskLocked(submission.getTask(), participation);
			for (TaskNumber taskNumber : taskNumbers) {
				taskNumber.setSubmission(submission);
			}
		}
		return taskNumbers;
	}
}
