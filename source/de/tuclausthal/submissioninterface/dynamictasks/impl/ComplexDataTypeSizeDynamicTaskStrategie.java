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
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

/**
 * @author Sven Strickroth
 */
public class ComplexDataTypeSizeDynamicTaskStrategie extends AbstractDynamicTaskStrategie implements DynamicTaskStrategieIf {
	private static final String[] RESULT_FIELDS = { "Größe des Datentyps in Bits" };
	private static final String[] RESULT_FIELDS_WITH_PARTIAL = { "-Bits für Lfnd. Nr.", "-Bits für Verwendungszweck", "-Bits für Betrag", "Größe des Datentyps in Bits" };
	private static final String[] VARIABLES = { "Max. Lfnd. Nr.", "Länge Verwendungszweck", "Min. Betrag", "Max. Betrag" };

	public ComplexDataTypeSizeDynamicTaskStrategie(Session session, Task task) {
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
		if (!correctResults.get(0).equals(studentSolution.get(0))) {
			return false;
		}
		return true;
	}

	@Override
	public List<String> getCorrectResults(List<TaskNumber> numbers, boolean includePartialSolutions) {
		int lfndNr = Integer.parseInt(numbers.get(0).getNumber());
		int maxVerwZweck = Integer.parseInt(numbers.get(1).getNumber());
		int maxBetrag = Integer.parseInt(numbers.get(2).getNumber());
		int minBetrag = Integer.parseInt(numbers.get(3).getNumber());

		int bitsLfndNr = (int) Math.ceil(Math.log(lfndNr) / Math.log(2)); // + 1 für VZ-bit

		int bitDatum = 10 * 8;

		int bitVerwZweck = 32 * maxVerwZweck; // * 32, weil Unicode-32

		// Wenn der größere Betrag positiv ist, bzw, wenn der positive betrag gleich dem negativen ist, dann muss man auf basis des positiven Betrages den Wert berechnen;
		// Wenn der größere Betrag negativ ist muss man auf basis des negativen berechnen, dabei muss bedacht werden, dass im negativen Bereich eine Zahl mehr abgebildet werden kann
		// Für positive Werte log_2(max+1) gibt Anzahl der Bits an; max+1, weil 0 im positiven Bereich fällt 
		int bitBetrag;
		if (maxBetrag >= -minBetrag) {
			bitBetrag = (int) Math.ceil(Math.log(maxBetrag + 1) / Math.log(2)) + 1; // + 1 für VZ-bit
		} else {
			bitBetrag = (int) Math.ceil(Math.log(-minBetrag) / Math.log(2)) + 1; // + 1 für VZ-bit
		}

		List<String> results = new LinkedList<String>();
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
		List<TaskNumber> numbers = new LinkedList<TaskNumber>();

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
		return "Für ein Buchführungsprogramm sollen Einträge platzsparend gespeichert werden.<br><br>Diese Einträge sollen folgende Daten enthalten:<ul><li>eine laufende Nummer die mit 1 beginnt und maximal $Var0$ groß ist.</li><li>ein Verwendungszweck mit $Var1$ Zeichen mit Unicode-32 codiert.</li><li>der Betrag (gespeichert als ganze Cent), der zwischen $Var3$ und $Var2$ liegt (2er Komplement). Es soll hier der Betrag 0 auch auf 0 abgebildet werden (also keine Verschiebung des Werteraumes).</li><li>das Datum mit 10 ASCII-Zeichen codiert.</li></ul>Wie viele Bit werden mindestens benötigt um diese Werte zu speichern?";
	}
}
