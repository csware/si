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
import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;
import de.tuclausthal.submissioninterface.util.RandomNumber;

/**
 * @author Sven Strickroth
 */
public class HexFloatMultiplikationDynamkicTaskStrategie extends AbstractDynamicTaskStrategie implements DynamicTaskStrategieIf {
	private static final String[] RESULT_FIELDS = { "Lösung der Berechnung (binär)", "Lösung der Berechnung (hex)", "Fehler der Lösung" };
	private static final String[] RESULT_FIELDS_WITH_PARTIAL = { "-Lösung der Berechnung (decimal)", "Lösung der Berechnung (binär)", "Lösung der Berechnung (hex)", "-Float-Ergebnis", "Fehler der Lösung" };
	private static final String[] VARIABLES = { "Wert 1", "Wert 2" };

	public HexFloatMultiplikationDynamkicTaskStrategie(Session session, Task task) {
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
	public boolean checkResults(List<String> correctResults, List<String> studentSolution) {
		if (!studentSolution.get(0).trim().equals(correctResults.get(0))) {
			return false;
		}
		if (!RandomNumber.trimLeadingZeros(studentSolution.get(1).trim()).equals(RandomNumber.trimLeadingZeros(correctResults.get(1)))) {
			return false;
		}
		try {
			if (Math.abs(Double.parseDouble(correctResults.get(2)) - Double.parseDouble(studentSolution.get(2).trim())) > 0.0000000001d) {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public List<String> getCorrectResults(List<TaskNumber> numbers, boolean includePartialSolutions) {
		double result = Double.parseDouble(numbers.get(0).getOrigNumber()) * Double.parseDouble(numbers.get(1).getOrigNumber());
		List<String> results = new LinkedList<>();
		if (includePartialSolutions) {
			results.add(String.valueOf(result));
		}
		results.add(RandomNumber.getFloatBits((float) result));
		results.add(RandomNumber.binStringToHex(results.get(results.size() - 1)));
		if (includePartialSolutions) {
			results.add(String.valueOf((float) result));
		}
		double diff = result - Double.valueOf(Float.toString((float) result));
		results.add(String.valueOf((float) diff));
		return results;
	}

	@Override
	public String[] getVariableNames() {
		return VARIABLES;
	}

	@Override
	protected List<TaskNumber> createTaskNumbers(Participation participation) {
		List<TaskNumber> numbers = new LinkedList<>();
		String[] floatNumbers = RandomNumber.getFloatBitsTruncated(RandomNumber.getFloat(true));
		while (floatNumbers[0].endsWith(".0")) {
			floatNumbers = RandomNumber.getFloatBitsTruncated(RandomNumber.getFloat(true));
		}
		numbers.add(new TaskNumber(getTask(), participation, RandomNumber.binStringToHex(floatNumbers[1]), floatNumbers[0]));
		floatNumbers = RandomNumber.getFloatBitsTruncated(RandomNumber.getFloat(true));
		while (floatNumbers[0].endsWith(".0")) {
			floatNumbers = RandomNumber.getFloatBitsTruncated(RandomNumber.getFloat(true));
		}
		numbers.add(new TaskNumber(getTask(), participation, RandomNumber.binStringToHex(floatNumbers[1]), floatNumbers[0]));
		return numbers;
	}

	@Override
	public String getExampleTaskDescription() {
		return "Gegeben sind die zwei Zahlen $Var0$ und $Var1$ als Hexadezimaldarstellung einer Fließkommazahl nach IEEE 754.<br><br>Berechnen Sie die Dezimaldarstellung, dann multiplizieren Sie die Zahlen. Codieren Sie das Ergebnis neu als Fließkommazahl. Falls die Umrechnung zurück einen Rundungsfehler beinhaltet, muss der auch angegeben werden.<br><br>Wie sieht die Fließkommazahl als Binärfolge aus? Wie ist die Hexadezimaldarstellung dieser Binärfolge?";
	}
}
