/*
 * Copyright 2011-2012, 2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.dynamictasks;

import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

public interface DynamicTaskStrategieIf {

	boolean isCorrect(Submission submission);

	String[] getResultFields(boolean includePartialSolutions);

	int getNumberOfResultFields();

	List<String> getCorrectResults(List<TaskNumber> taskNumbers, boolean includePartialSolutions);

	List<String> getCorrectResults(Submission submission, boolean includePartialSolutions);

	String[] getVariableNames();

	List<TaskNumber> getVariables(Participation participation);

	List<TaskNumber> getVariables(Submission submission);

	String getExampleTaskDescription();

	String getTranslatedDescription(Participation participation);

	String getTranslatedDescription(List<TaskNumber> taskNumbers);

	String getTranslatedDescription(Submission submission);

	List<String> getUserResults(Submission submission);

}
