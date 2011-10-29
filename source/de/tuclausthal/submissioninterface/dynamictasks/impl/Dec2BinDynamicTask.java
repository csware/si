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
import de.tuclausthal.submissioninterface.util.Util;

public class Dec2BinDynamicTask extends AbstractDynamicTask {

	public Dec2BinDynamicTask(Session session, Task task) {
		super(session, task);
	}

	@Override
	public boolean isCorrect(Submission submission) {
		return getUserResults(submission).get(0).equals(getCorrectResults(submission).get(0));
	}

	@Override
	public String getFields(Participation participation, Submission submission) {
		List<String> results = getUserResults(submission);
		String output = "";
		output += "<p>Bitte füllen Sie das Feld mit Ihrer berechneten Lösung:</p>";
		output += "<p><input type=text name=dynamicresult0 size=20 value=\"" + Util.escapeHTML(results.get(0)) + "\"></p>";
		return output;
	}

	@Override
	public int numberOfFields() {
		return 1;
	}

	@Override
	public List<String> getCorrectResults(Submission submission) {
		List<String> results = new LinkedList<String>();
		results.add(getVariables(submission).get(0).getOrigNumber());
		return results;
	}

	@Override
	public String showUserResult(Submission submission) {
		return "Lösung: " + Util.escapeHTML(getUserResults(submission).get(0));
	}

	@Override
	protected List<TaskNumber> createTaskNumbers(Participation participation) {
		List<TaskNumber> numbers = new LinkedList<TaskNumber>();
		String origNumber = RandomNumber.getRandomNumber(RandomNumber.getRandomParam('c'));
		String number = RandomNumber.getNumber(origNumber, RandomNumber.getRandomParam('c'));
		numbers.add(new TaskNumber(getTask(), participation, number, origNumber));
		return numbers;
	}
}
