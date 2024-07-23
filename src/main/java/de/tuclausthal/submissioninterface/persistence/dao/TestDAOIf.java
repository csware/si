/*
 * Copyright 2009-2010, 2021-2022, 2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.ChecklistTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommentsMetricTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.CompileTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JUnitTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.UMLConstraintTest;

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
	JUnitTest createJUnitTest(Task task);

	/**
	 * Create and store a UMLConstraintTest to a specific task
	 * @param task the task to associtate the test to
	 * @return the created test
	 */
	UMLConstraintTest createUMLConstraintTest(Task task);

	/**
	 * Create and store a compile/syntax to a specific task
	 * @param task the task to associtate the test to
	 * @return the created test
	 */
	CompileTest createCompileTest(Task task);

	JavaAdvancedIOTest createJavaAdvancedIOTest(Task task);

	CommentsMetricTest createCommentsMetricTest(Task task);

	DockerTest createDockerTest(Task task);

	ChecklistTest createChecklistTest(Task task);

	/**
	 * Remove a specific test from the DB
	 * @param test the test to remvoe
	 */
	void deleteTest(Test test);

	Test getTest(int testId);

	Test getTestLocked(int testId);

	/**
	 * Checks if a test is ready to run
	 * ALERT: this method starts it's own transaction!
	 * @return Test or null if none is "queued".
	 */
	Test takeTestTransacted();

	List<Test> getStudentTests(Task task);

	List<Test> getTutorTests(Task task);
}
