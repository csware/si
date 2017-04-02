/*
 * Copyright 2011-2013, 2017 Sven Strickroth <email@cs-ware.de>
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
public class HardDiskCalculation2DynamicTaskStrategie extends AbstractDynamicTaskStrategie implements DynamicTaskStrategieIf {
	private static final String[] RESULT_FIELDS = { "Lösung in Terrabyte", "Lösung in Tebibyte", "Anzahl benötigter Blue-Rays" };
	private static final String[] RESULT_FIELDS_WITH_PARTIAL = { "-Summe der Videos (GB)", "-Summe der Videos (bytes)", "-Summe der Audio-Files (Mebibytes)", "-Summe der Audio-Files (bytes)", "-Lösung in Bytes", "Lösung in Terrabyte", "Lösung in Tebibyte", "Anzahl benötigter Blue-Rays (25 GB)" };
	private static final String[] VARIABLES = { "Anzahl Videos", "Größe der Videos (in GB)", "Anzahl Audio-Dateien", "Größe der Audiodateien (MiB)" };

	public HardDiskCalculation2DynamicTaskStrategie(Session session, Task task) {
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
		try {
			for (int i = 0; i < correctResults.size(); i++) {
				if (Math.abs(Double.parseDouble(correctResults.get(i)) - Double.parseDouble(studentSolution.get(i).trim())) > 0.0001d) {
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
		long videofiles = Integer.parseInt(numbers.get(0).getNumber());
		long videogroesseInGigaByte = Integer.parseInt(numbers.get(1).getNumber());
		long audiofiles = Integer.parseInt(numbers.get(2).getNumber());
		long audiofilegroesseInByte = Integer.parseInt(numbers.get(3).getNumber());

		long gbVideos = videofiles * videogroesseInGigaByte;
		long bytesVideos = gbVideos * 1000l * 1000l * 1000l;
		long mibAudio = audiofilegroesseInByte * audiofiles;
		long bytesAudio = mibAudio * 1024l * 1024l;
		long bytes = bytesVideos + bytesAudio;

		double terabytes = bytes / 1000.0 / 1000.0 / 1000.0 / 1000.0;
		double teribytes = bytes / 1024.0 / 1024.0 / 1024.0 / 1024.0;

		List<String> results = new LinkedList<>();
		if (includePartialSolutions) {
			results.add(String.valueOf(gbVideos));
			results.add(String.valueOf(bytesVideos));
			results.add(String.valueOf(mibAudio));
			results.add(String.valueOf(bytesAudio));
			results.add(String.valueOf(bytes));
		}
		results.add(String.valueOf(terabytes));
		results.add(String.valueOf(teribytes));

		double gigabytes = bytes / 1000.0 / 1000.0 / 1000.0;
		long anzahlBlueRays = (long) Math.ceil((gigabytes / 25.0));
		results.add(String.valueOf(anzahlBlueRays));

		return results;
	}

	@Override
	public String[] getVariableNames() {
		return VARIABLES;
	}

	@Override
	protected List<TaskNumber> createTaskNumbers(Participation participation) {
		List<TaskNumber> numbers = new LinkedList<>();
		int videos = (int) (Math.random() * 30 + 1);
		int videogroesseInGigaByte = 30 + (int) (Math.random() * 35);
		int audiofiles = (int) (Math.random() * 3000 + 10);
		int audiofilegroesseInMebiByte = (int) (Math.random() * 70 + 15);
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(videos), String.valueOf(videos)));
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(videogroesseInGigaByte), String.valueOf(videogroesseInGigaByte)));
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(audiofiles), String.valueOf(audiofiles)));
		numbers.add(new TaskNumber(getTask(), participation, String.valueOf(audiofilegroesseInMebiByte), String.valueOf(audiofilegroesseInMebiByte)));
		return numbers;
	}

	@Override
	public String getExampleTaskDescription() {
		return "Bei einer Usability-Studie wurden Videos in hoher Qualität aufgenommen. Die Videos haben jeweils eine Größe von $Var1$ Gigabyte. Es wurden $Var0$ Videos aufgenommen. Ebenfalls sind bei der Studie $Var2$ Audio-Dateien mit einer jeweiligen Größe von $Var3$ Mebibyte entstanden.<br><br>Wie viel Terabyte und wie viel Tebibyte benötigt man?<br>Angenommen, die Aufzeichnungen sollen für weitere Analysen auf Blu-Ray Discs mit einer Speichergröße von jeweils 25GB gebrannt werden. Wie viele single-layer Blu-Ray Discs sind notwendig?";
	}
}
