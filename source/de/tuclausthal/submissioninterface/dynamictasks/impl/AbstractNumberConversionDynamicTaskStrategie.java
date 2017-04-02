/*
 * Copyright 2011-2012, 2017 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.dynamictasks.impl;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.dynamictasks.AbstractDynamicTaskStrategie;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

public abstract class AbstractNumberConversionDynamicTaskStrategie extends AbstractDynamicTaskStrategie {
	private static final String[] RESULT_FIELDS = { "Lösung der Umrechnung" };
	private static final String[] VARIABLES = { "Wert" };

	public AbstractNumberConversionDynamicTaskStrategie(Session session, Task task) {
		super(session, task);
	}

	@Override
	final public String[] getResultFields(boolean includePartialSolutions) {
		return RESULT_FIELDS;
	}

	@Override
	final public List<String> getCorrectResults(List<TaskNumber> taskNumbers, boolean includePartialSolutions) {
		List<String> results = new LinkedList<>();
		results.add(taskNumbers.get(0).getOrigNumber());
		return results;
	}

	@Override
	final public String[] getVariableNames() {
		return VARIABLES;
	}
}
