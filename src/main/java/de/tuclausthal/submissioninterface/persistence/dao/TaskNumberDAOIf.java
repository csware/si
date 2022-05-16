/*
 * Copyright 2011 Giselle Rodriguez
 * Copyright 2011, 2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

/**
 * Data Access Object Interface for the TaskNumber-class
 * @author Giselle Rodriguez
 * @author Sven Strickroth
 */
public interface TaskNumberDAOIf {
	/**
	 * Return the TaskNumbers of a submission
	 * @param submission 
	 * @return the list of numbers of a Task
	 */
	public List<TaskNumber> getTaskNumbersForSubmission(Submission submission);

	/**
	 * Return the TaskNumbers of a submission
	 * @param task
	 * @param participation 
	 * @return the list of numbers of a Task
	 */
	public List<TaskNumber> getTaskNumbersForTaskLocked(Task task, Participation participation);

	public List<TaskNumber> assignTaskNumbersToSubmission(Submission submission, Participation participation);
}
