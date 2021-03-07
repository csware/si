/*
 * Copyright 2011-2012, 2017, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import java.util.ArrayList;
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
	final public boolean isCorrect(Submission submission) {
		List<String> correctResults = getCorrectResults(submission, false);
		List<String> studentSolution = getUserResults(submission);
		return checkResults(correctResults, studentSolution);
	}

	public boolean checkResults(List<String> correctResults, List<String> studentSolution) {
		for (int i = 0; i < studentSolution.size(); i++) {
			if (!studentSolution.get(i).trim().equals(correctResults.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public abstract String[] getResultFields(boolean includePartialSolutions);

	@Override
	public int getNumberOfResultFields() {
		return getResultFields(false).length;
	}

	@Override
	public List<String> getCorrectResults(Submission submission, boolean includePartialSolutions) {
		return getCorrectResults(getVariables(submission), includePartialSolutions);
	}

	@Override
	public abstract List<String> getCorrectResults(List<TaskNumber> taskNumbers, boolean includePartialSolutions);

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
		if (taskNumbers.isEmpty()) {
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
		return getTranslatedDescription(getVariables(participation));
	}

	@Override
	final public String getTranslatedDescription(Submission submission) {
		return getTranslatedDescription(getVariables(submission));
	}

	@Override
	final public String getTranslatedDescription(List<TaskNumber> variables) {
		String description = task.getDescription();
		for (int i = 0; i < variables.size(); i++) {
			description = description.replace("$Var" + i + "$", Util.escapeHTML(variables.get(i).getNumber()));
		}
		return description;
	}

	@Override
	final public List<String> getUserResults(Submission submission) {
		List<String> results = DAOFactory.ResultDAOIf(session).getResultsForSubmission(submission);
		if (results.isEmpty()) {
			results = new ArrayList<>();
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
