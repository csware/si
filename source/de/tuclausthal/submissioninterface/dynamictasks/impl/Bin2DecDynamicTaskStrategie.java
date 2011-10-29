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
