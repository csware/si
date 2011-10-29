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
