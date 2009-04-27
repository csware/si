package de.tuclausthal.abgabesystem.persistence.dao.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.abgabesystem.MainBetterNameHereRequired;
import de.tuclausthal.abgabesystem.persistence.dao.TestDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.JUnitTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.RegExpTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.persistence.datamodel.Test;

/**
 * Data Access Object implementation for the TestDAOIf
 * @author Sven Strickroth
 */
public class TestDAO implements TestDAOIf {
	@Override
	public JUnitTest createJUnitTest(Task task) {
		// TODO: race cond.
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		JUnitTest test = new JUnitTest();
		test.setTask(task);
		session.save(test);
		tx.commit();
		return test;
	}

	@Override
	public RegExpTest createRegExpTest(Task task) {
		// TODO: race cond.
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		RegExpTest test = new RegExpTest();
		test.setTask(task);
		session.save(test);
		tx.commit();
		return test;
	}

	@Override
	public void saveTest(Test test) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		session.update(test);
		session.delete(test);
		tx.commit();
	}

	@Override
	public void deleteTest(Test test) {
		Session session = MainBetterNameHereRequired.getSession();
		Transaction tx = session.beginTransaction();
		session.update(test);
		session.delete(test);
		tx.commit();
	}
}
