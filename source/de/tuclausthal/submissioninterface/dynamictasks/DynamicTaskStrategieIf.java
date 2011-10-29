/*
 * Copyright 2011 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.dynamictasks;

import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

public interface DynamicTaskStrategieIf {

	public abstract boolean isCorrect(Submission submission);

	public abstract String[] getResultFields();

	public abstract int getNumberOfResultFields();

	public abstract List<String> getCorrectResults(Submission submission);

	public abstract String[] getVariableNames();

	public abstract List<TaskNumber> getVariables(Participation participation);

	public abstract List<TaskNumber> getVariables(Submission submission);

	public abstract String getTranslatedDescription(Participation participation);

	public abstract String getTranslatedDescription(Submission submission);

	public abstract List<String> getUserResults(Submission submission);

}
