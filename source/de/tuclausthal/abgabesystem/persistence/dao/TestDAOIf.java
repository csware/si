package de.tuclausthal.abgabesystem.persistence.dao;

import de.tuclausthal.abgabesystem.persistence.datamodel.JUnitTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.RegExpTest;
import de.tuclausthal.abgabesystem.persistence.datamodel.Task;
import de.tuclausthal.abgabesystem.persistence.datamodel.Test;

/**
 * Data Access Object Interface for the Test-class(es)
 * @author Sven Strickroth
 */
public interface TestDAOIf {
	/**
	 * Create and store a JUnitTest to a specific task
	 * @param task the task to associtate the test to
	 * @return the created test
	 */
	public JUnitTest createJUnitTest(Task task);

	/**
	 * Create and store a JUnitTest to a specific task
	 * @param task the task to associtate the test to
	 * @return the created test
	 */
	public RegExpTest createRegExpTest(Task task);

	/**
	 * Update/save a test
	 * @param test the test to update
	 */
	public void saveTest(Test test);

	/**
	 * Remove a specific test from the DB
	 * @param test the test to remvoe
	 */
	public void deleteTest(Test test);
}
