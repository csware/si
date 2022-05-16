/*
 * Copyright 2011-2012, 2017, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.dynamictasks.impl;

import java.util.ArrayList;
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
		List<TaskNumber> numbers = new ArrayList<>();
		do {
			String origNumber = RandomNumber.getRandomNumber(RandomNumber.getRandomParam('B'));
			String number = RandomNumber.getNumber(origNumber, RandomNumber.getRandomParam('B'));
			if (number.length() < 7 || number.length() > 8) {
				continue;
			}
			long ones = number.chars().filter(character -> character == '1').count();
			if (ones < 3 || number.length() - ones < 3) {
				continue;
			}
			numbers.add(new TaskNumber(getTask(), participation, number, origNumber));
		} while (numbers.isEmpty());
		return numbers;
	}

	@Override
	public String getExampleTaskDescription() {
		return "Berechnen Sie die Dezimaldarstellung des Binär-Wertes $Var0$.";
	}
}
