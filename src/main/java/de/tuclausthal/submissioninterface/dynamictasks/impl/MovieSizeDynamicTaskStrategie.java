/*
 * Copyright 2011-2012, 2017, 2020, 2024 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

/**
 * @author Sven Strickroth
 */
public class MovieSizeDynamicTaskStrategie extends AbstractDynamicTaskStrategie {
	private static final String[] RESULT_FIELDS = { "Größe der Videodatei in Bytes", "Größe der Videodatei in Mebibyte" };
	private static final String[] RESULT_FIELDS_WITH_PARTIAL = { "-Pixel (width x height", "-Video: Byte pro Frame", "-Video Byte pro Sekunde", "-Video Byte pro Minute", "-Sound Byte pro Sekunde", "-Byte pro Sekunde", "-Byte pro Minute", "Größe der Videodatei in Bytes", "Größe der Videodatei in Mebibyte" };
	private static final String[] VARIABLES = { "FP/s", "Auflösung", "Dauer", "Ton durchschnittlich pro Sekunde (KBit/s)" };
	private static final String[] RESOLUTIONS = { "960x540", "640x480", "1440x1080", "720x480", "1280x720", "352x480", "1024x576", "352x756", "480x756" };

	public MovieSizeDynamicTaskStrategie(Session session, Task task) {
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
		try {
			if (Math.abs(Double.parseDouble(correctResults.get(1)) - Double.parseDouble(studentSolution.get(1).trim())) > 0.001d) {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public List<String> getCorrectResults(List<TaskNumber> numbers, boolean includePartialSolutions) {
		String width = numbers.get(1).getNumber().substring(0, numbers.get(1).getNumber().indexOf("x"));
		String height = numbers.get(1).getNumber().substring(numbers.get(1).getNumber().indexOf("x") + 1);
		long fps = Long.parseLong(numbers.get(0).getNumber());
		long dauer = Long.parseLong(numbers.get(2).getNumber());
		long sndks = Long.parseLong(numbers.get(3).getNumber());

		long pixels = Long.parseLong(width) * Long.parseLong(height);
		long byteVideoProFrame = pixels * 3;
		long byteVideoProSecond = byteVideoProFrame * fps;
		long byteVideoProMinute = byteVideoProSecond * 60;
		long byteSoundProSecond = sndks * 1000 / 8;
		long byteProSecond = byteVideoProSecond + byteSoundProSecond;
		long byteProMinute = byteProSecond * 60;
		long bytes = byteProMinute * dauer;

		List<String> results = new ArrayList<>();
		if (includePartialSolutions) {
			results.add(String.valueOf(pixels));
			results.add(String.valueOf(byteVideoProFrame));
			results.add(String.valueOf(byteVideoProSecond));
			results.add(String.valueOf(byteVideoProMinute));
			results.add(String.valueOf(byteSoundProSecond));
			results.add(String.valueOf(byteProSecond));
			results.add(String.valueOf(byteProMinute));
		}
		results.add(String.valueOf(bytes));
		results.add(String.valueOf(bytes / 1024.0 / 1024.0));
		return results;
	}

	@Override
	public String[] getVariableNames() {
		return VARIABLES;
	}

	@Override
	protected List<TaskNumber> createTaskNumbers(Participation participation) {
		List<TaskNumber> numbers = new ArrayList<>();

		int rnd = (int) (Math.random() * RESOLUTIONS.length);
		if (rnd == RESOLUTIONS.length) {
			rnd--;
		}
		int fps = (int) (Math.random() * 25 + 15);
		int dauer = (int) (Math.random() * 30 + 5);
		int sndks = ((8 * (int) (Math.random() * 36)) + 32);
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(fps), String.valueOf(fps)));
		numbers.add(new TaskNumber(getTask(), participation, RESOLUTIONS[rnd], RESOLUTIONS[rnd]));
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(dauer), String.valueOf(dauer)));
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(sndks), String.valueOf(sndks)));
		return numbers;
	}

	@Override
	public String getExampleTaskDescription() {
		return "Ein Video soll gespeichert werden. Dafür werden einfach alle Bilder einzeln und dazu der Ton gespeichert. Berechnen Sie den benötigten Speicherbedarf in Byte.<br><br>Gegeben sind:<ul><li>$Var0$ Bilder pro Sekunde</li> <li>Das Video hat eine $Var1$ Auflösung in RGB True Color</li><li>das Video ist $Var2$ Minuten lang</li><li>Die Tonspur hat $Var3$ Kbit/Sekunde</li></ul>";
	}
}
