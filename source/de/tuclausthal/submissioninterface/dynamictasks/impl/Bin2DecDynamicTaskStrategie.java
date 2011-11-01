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

package de.tuclausthal.submissioninterface.dynamictasks.impl;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;
import de.tuclausthal.submissioninterface.util.RandomNumber;

public class Bin2DecDynamicTaskStrategie extends AbstractNumberConversionDynamicTaskStrategie {
	public Bin2DecDynamicTaskStrategie(Session session, Task task) {
		super(session, task);
	}

	@Override
	protected List<TaskNumber> createTaskNumbers(Participation participation) {
		List<TaskNumber> numbers = new LinkedList<TaskNumber>();
		String origNumber = RandomNumber.getRandomNumber(RandomNumber.getRandomParam('B'));
		String number = RandomNumber.getNumber(origNumber, RandomNumber.getRandomParam('B'));
		numbers.add(new TaskNumber(getTask(), participation, number, origNumber));
		return numbers;
	}
}
