package de.tuclausthal.abgabesystem.persistence.dao.impl;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.persistence.dao.TaskDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.Lecture;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;

public class TaskDAO implements TaskDAOIf {

	@Override
	public Task getTask(int taskid) {
		return (Task) MainBetterNameHereRequired.getSession().get(Task.class, taskid);
	}

	@Override
	/**
	 * @param title
	 * @param maxPoints
	 * @param start
	 * @param deadline
	 * @param description
	 * @param submissions
	 * @param lecture
	 */
	public Task newTask(String title, int maxPoints, Date start, Date deadline, String description, Lecture lecture, Date showPoints) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		Task task = new Task(title, maxPoints, start, deadline, description, lecture, showPoints);
		session.save(task);
		tx.commit();
		return task;
	}

	@Override
	public void saveTask(Task task) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		session.save(task);
		tx.commit();
	}

	@Override
	public void deleteTask(Task task) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		session.update(task);
		session.delete(task);
		tx.commit();
	}
}
