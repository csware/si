/*
 * Copyright 2011-2012, 2017, 2020 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.dynamictasks.AbstractDynamicTaskStrategie;
import de.tuclausthal.submissioninterface.dynamictasks.DynamicTaskStrategieIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

/**
 * @author Sven Strickroth
 */
public class ComplexDataTypeSizeWithFloatDynamicTaskStrategie extends AbstractDynamicTaskStrategie implements DynamicTaskStrategieIf {
	private static final String[] RESULT_FIELDS = { "Größe des Datentyps in Bits" };
	private static final String[] RESULT_FIELDS_WITH_PARTIAL = { "-Bits für Lfnd. Nr.", "-Bits für Verwendungszweck", "-Bits für Betrag", "Größe des Datentyps in Bits" };
	private static final String[] VARIABLES = { "Max. Lfnd. Nr.", "Länge Verwendungszweck", "Min. Betrag", "Max. Betrag" };

	public ComplexDataTypeSizeWithFloatDynamicTaskStrategie(Session session, Task task) {
		super(session, task);
	}

	@Override
	public String[] getResultFields(boolean includePartialSolutions) {
		if (includePartialSolutions) {
			return RESULT_FIELDS_WITH_PARTIAL;
		}
		return RESULT_FIELDS;
	}

	@Override
	public boolean checkResults(List<String> correctResults, List<String> studentSolution) {
		if (!correctResults.get(0).equals(studentSolution.get(0).trim())) {
			return false;
		}
		return true;
	}

	@Override
	public List<String> getCorrectResults(List<TaskNumber> numbers, boolean includePartialSolutions) {
		int lfndNr = Integer.parseInt(numbers.get(0).getNumber());
		int maxVerwZweck = Integer.parseInt(numbers.get(1).getNumber());

		int bitsLfndNr = (int) Math.ceil(Math.log(lfndNr) / Math.log(2)); // + 1 für VZ-bit

		int bitDatum = 10 * 8;

		int bitVerwZweck = 32 * maxVerwZweck; // * 32, weil Unicode-32

		int bitBetrag = 32;

		List<String> results = new ArrayList<>();
		if (includePartialSolutions) {
			results.add(String.valueOf(bitsLfndNr));
			results.add(String.valueOf(bitVerwZweck));
			results.add(String.valueOf(bitBetrag));
		}
		results.add(String.valueOf(bitsLfndNr + bitDatum + bitVerwZweck + bitBetrag));
		return results;
	}

	@Override
	public String[] getVariableNames() {
		return VARIABLES;
	}

	@Override
	protected List<TaskNumber> createTaskNumbers(Participation participation) {
		List<TaskNumber> numbers = new ArrayList<>();

		int lfndnr = 100000 * (int) (Math.random() * 1000 + 1);
		int maxVerwZweck = 5 * (int) (Math.random() * 47 + 20);
		int maxBetrag = 1000 * (int) (Math.random() * 10000 + 1);
		int minBetrag = -1000 * (int) (Math.random() * 10000 + 1);
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(lfndnr), String.valueOf(lfndnr)));
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(maxVerwZweck), String.valueOf(maxVerwZweck)));
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(maxBetrag), String.valueOf(maxBetrag)));
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(minBetrag), String.valueOf(minBetrag)));
		return numbers;
	}

	@Override
	public String getExampleTaskDescription() {
		return "Für ein Buchführungsprogramm sollen Einträge platzsparend gespeichert werden.<br><br>Diese Einträge sollen folgende Daten enthalten:<ul><li>eine laufende Nummer die mit 1 beginnt und maximal $Var0$ groß ist.</li><li>ein Verwendungszweck mit $Var1$ Zeichen mit Unicode-32 codiert.</li><li>der Betrag (gespeichert als Kommazahl), der zwischen $Var3$ und $Var2$ liegt.</li><li>das Datum mit 10 ASCII-Zeichen codiert.</li></ul>Wie viele Bit werden mindestens benötigt um diese Werte zu speichern?";
	}
}
