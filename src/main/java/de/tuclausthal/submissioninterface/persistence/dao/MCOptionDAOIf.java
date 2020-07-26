/*
 * Copyright 2020 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

/**
 * Data Access Object Interface for the MCOption-class
 * @author Sven Strickroth
 */
public interface MCOptionDAOIf {
	/**
	 * Creates and stores a new MC Option for a task
	 * @param task the task
	 * @param option the visible string
	 * @param correct whether this MC option is correct
	 * @return the mcoption
	 */
	public MCOption createMCOption(Task task, String option, boolean correct);

	/**
	 * Returns the M Options for a task
	 * @param task the task
	 * @return the list of options
	 */
	public List<MCOption> getMCOptionsForTask(Task task);

	/**
	 * Delete a MC option
	 * @param mcoption the mcoption to delete
	 */
	public void deleteMCOption(MCOption mcoption);
}
