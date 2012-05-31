/*
 * Copyright 2011 - 2012 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

/**
 * @author Sven Strickroth
 */
public class HardDiskCalculationDynamicTaskStrategie extends AbstractDynamicTaskStrategie implements DynamicTaskStrategieIf {
	private static final String[] RESULT_FIELDS = { "Lösung in Terrabyte", "Lösung in Tebibyte" };
	private static final String[] RESULT_FIELDS_WITH_PARTIAL = { "-Summe der Blue-Rays (bytes)", "-Summe der Audio-Files (bytes)", "-Lösung in Bytes", "Lösung in Terrabyte", "Lösung in Tebibyte" };
	private static final String[] VARIABLES = { "Anzahl Blue-Rays", "Größe der Blue-Rays", "Anzahl Musikstücke", "Durchschnittliche Größe der Musikstücke" };

	public HardDiskCalculationDynamicTaskStrategie(Session session, Task task) {
		super(session, task);
	}

	@Override
	public String[] getResultFields(boolean includePartialSolutions) {
		if (includePartialSolutions) {
			return RESULT_FIELDS_WITH_PARTIAL;
		} else {
			return RESULT_FIELDS;
		}
	}

	@Override
	public boolean isCorrect(Submission submission) {
		List<String> correctResults = getCorrectResults(submission, false);
		List<String> studentSolution = getUserResults(submission);
		try {
			for (int i = 0; i < correctResults.size(); i++) {
				if (Math.abs(Double.parseDouble(correctResults.get(i)) - Double.parseDouble(studentSolution.get(i))) > 0.0001d) {
					return false;
				}
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public List<String> getCorrectResults(List<TaskNumber> numbers, boolean includePartialSolutions) {
		long bluerays = Integer.parseInt(numbers.get(0).getNumber());
		long blueraysgroesseInGigaByte = Integer.parseInt(numbers.get(1).getNumber());
		long musikstuecke = Integer.parseInt(numbers.get(2).getNumber());
		long musikstueckegroesseInByte = Integer.parseInt(numbers.get(3).getNumber());

		long bytesBL = bluerays * 1000l * 1000l * 1000l * blueraysgroesseInGigaByte;
		long bytesMusi = musikstueckegroesseInByte * musikstuecke;
		long bytes = bytesBL + bytesMusi;

		double terabytes = bytes / 1000.0 / 1000.0 / 1000.0 / 1000.0;
		double teribytes = bytes / 1024.0 / 1024.0 / 1024.0 / 1024.0;

		List<String> results = new LinkedList<String>();
		if (includePartialSolutions) {
			results.add(String.valueOf(bytesBL));
			results.add(String.valueOf(bytesMusi));
			results.add(String.valueOf(bytes));
		}
		results.add(String.valueOf(terabytes));
		results.add(String.valueOf(teribytes));
		return results;
	}

	@Override
	public String[] getVariableNames() {
		return VARIABLES;
	}

	@Override
	protected List<TaskNumber> createTaskNumbers(Participation participation) {
		List<TaskNumber> numbers = new LinkedList<TaskNumber>();
		int bluerays = (int) (Math.random() * 30 + 1);
		int blueraysgroesseInGigaByte = 20 + (int) (Math.random() * 5);
		int musikstuecke = (int) (Math.random() * 20000 + 100);
		int musikstueckegroesseInByte = (int) (Math.random() * 5000000 + 3000000);
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(bluerays), String.valueOf(bluerays)));
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(blueraysgroesseInGigaByte), String.valueOf(blueraysgroesseInGigaByte)));
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(musikstuecke), String.valueOf(musikstuecke)));
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(musikstueckegroesseInByte), String.valueOf(musikstueckegroesseInByte)));
		return numbers;
	}
}
