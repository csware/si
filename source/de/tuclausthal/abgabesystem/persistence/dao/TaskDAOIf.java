package de.tuclausthal.abgabesystem.persistence.dao;

import java.util.Date;

import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;

public interface TaskDAOIf {
	public Task newTask(String title, int maxPoints, Date start, Date deadline, String description, Lecture lecture,Date showPoints);

	public Task getTask(int taskid);

	public void saveTask(Task task);

	public void deleteTask(Task task);
}
