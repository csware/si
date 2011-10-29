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

package de.tuclausthal.submissioninterface.dynamictasks;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.TaskNumberDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * AbtractDynamicTask strategie
 * @author Sven Strickroth
 */
public abstract class AbstractDynamicTaskStrategie implements DynamicTaskStrategieIf {
	private Task task;
	private Session session;

	public AbstractDynamicTaskStrategie(Session session, Task task) {
		this.session = session;
		this.task = task;
	}

	@Override
	public abstract boolean isCorrect(Submission submission);

	@Override
	public abstract String[] getResultFields();

	@Override
	public int getNumberOfResultFields() {
		return getResultFields().length;
	}

	@Override
	public abstract List<String> getCorrectResults(Submission submission);

	@Override
	public abstract String[] getVariableNames();

	@Override
	final public List<TaskNumber> getVariables(Participation participation) {
		if (participation == null) {
			return createTaskNumbers(null);
		}
		TaskNumberDAOIf taskNumberDAO = DAOFactory.TaskNumberDAOIf(session);
		Transaction tx = session.beginTransaction();
		List<TaskNumber> taskNumbers = taskNumberDAO.getTaskNumbersForTaskLocked(task, participation);
		if (taskNumbers.size() == 0) {
			taskNumbers = createTaskNumbers(participation);
			for (TaskNumber taskNumber : taskNumbers) {
				session.save(taskNumber);
			}
		}
		tx.commit();
		return taskNumbers;
	}

	protected abstract List<TaskNumber> createTaskNumbers(Participation participation);

	@Override
	final public List<TaskNumber> getVariables(Submission submission) {
		return DAOFactory.TaskNumberDAOIf(session).getTaskNumbersForSubmission(submission);
	}

	@Override
	final public String getTranslatedDescription(Participation participation) {
		return translateDescription(getVariables(participation));
	}

	@Override
	final public String getTranslatedDescription(Submission submission) {
		return translateDescription(getVariables(submission));
	}

	private String translateDescription(List<TaskNumber> variables) {
		String description = task.getDescription();
		for (int i = 0; i < variables.size(); i++) {
			description = description.replace("$Var" + i + "$", Util.escapeHTML(variables.get(i).getNumber()));
		}
		return description;
	}

	@Override
	final public List<String> getUserResults(Submission submission) {
		List<String> results = DAOFactory.ResultDAOIf(session).getResultsForSubmission(submission);
		if (results.size() == 0) {
			results = new LinkedList<String>();
			for (int i = 0; i < getNumberOfResultFields(); i++) {
				results.add("");
			}
		}
		return results;
	}

	/**
	 * @return the task
	 */
	final protected Task getTask() {
		return task;
	}
}
