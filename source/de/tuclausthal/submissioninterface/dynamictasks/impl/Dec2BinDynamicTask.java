package de.tuclausthal.submissioninterface.dynamictasks.impl;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.dynamictasks.AbstractDynamicTask;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;
import de.tuclausthal.submissioninterface.util.RandomNumber;

public class Dec2BinDynamicTask extends AbstractDynamicTask {
	private static final String[] RESULT_FIELDS = { "Lösung der Umrechnung" };
	private static final String[] VARIABLES = { "Wert" };

	public Dec2BinDynamicTask(Session session, Task task) {
		super(session, task);
	}

	@Override
	public boolean isCorrect(Submission submission) {
		return getUserResults(submission).get(0).equals(getCorrectResults(submission).get(0));
	}

	@Override
	public String[] getResultFields() {
		return RESULT_FIELDS;
	}

	@Override
	public List<String> getCorrectResults(Submission submission) {
		List<String> results = new LinkedList<String>();
		results.add(getVariables(submission).get(0).getOrigNumber());
		return results;
	}

	@Override
	protected List<TaskNumber> createTaskNumbers(Participation participation) {
		List<TaskNumber> numbers = new LinkedList<TaskNumber>();
		String origNumber = RandomNumber.getRandomNumber(RandomNumber.getRandomParam('c'));
		String number = RandomNumber.getNumber(origNumber, RandomNumber.getRandomParam('c'));
		numbers.add(new TaskNumber(getTask(), participation, number, origNumber));
		return numbers;
	}

	@Override
	public String[] getVariableNames() {
		return VARIABLES;
	}
}
