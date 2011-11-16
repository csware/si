/*
 * Copyright 2011 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.TaskNumberDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

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
		return getSession().createCriteria(TaskNumber.class).add(Restrictions.eq("submission", submission)).addOrder(Order.asc("tasknumberid")).list();
	}

	@Override
	public List<TaskNumber> getTaskNumbersForTaskLocked(Task task, Participation participation) {
		return getSession().createCriteria(TaskNumber.class).add(Restrictions.eq("task", task)).add(Restrictions.eq("participation", participation)).addOrder(Order.asc("tasknumberid")).setLockMode(LockMode.UPGRADE).list();
	}

	@Override
	public void assignTaskNumbersToSubmission(Submission submission, Participation participation) {
		// only assign new numbers if not already numbers exist
		if (getTaskNumbersForSubmission(submission).size() == 0) {
			for (TaskNumber taskNumber : getTaskNumbersForTaskLocked(submission.getTask(), participation)) {
				taskNumber.setSubmission(submission);
				getSession().update(taskNumber);
			}
		}
	}
}
