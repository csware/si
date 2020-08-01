/*
 * Copyright 2011-2012, 2017, 2020 Sven Strickroth <email@cs-ware.de>
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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;
import de.tuclausthal.submissioninterface.util.RandomNumber;

public class Dec2BinDynamicTaskStrategie extends AbstractNumberConversionDynamicTaskStrategie {
	public Dec2BinDynamicTaskStrategie(Session session, Task task) {
		super(session, task);
	}

	@Override
	protected List<TaskNumber> createTaskNumbers(Participation participation) {
		List<TaskNumber> numbers = new ArrayList<>();
		String origNumber = RandomNumber.getRandomNumber(RandomNumber.getRandomParam('B'));
		String number = RandomNumber.getNumber(origNumber, RandomNumber.getRandomParam('B'));
		numbers.add(new TaskNumber(getTask(), participation, origNumber, number));
		return numbers;
	}

	@Override
	public String getExampleTaskDescription() {
		return "Berechnen Sie die Binärdarstellung des Dezimal-Wertes $Var0$.";
	}
}
