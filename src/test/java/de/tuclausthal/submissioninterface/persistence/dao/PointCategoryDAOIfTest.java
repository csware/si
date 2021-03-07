/*
 * Copyright 2020 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.tuclausthal.submissioninterface.BasicTest;
import de.tuclausthal.submissioninterface.GATEDBTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

@GATEDBTest
class PointCategoryDAOIfTest extends BasicTest {
	@Test
	void testCountPointsNone() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(2);
		assertEquals(0, DAOFactory.PointCategoryDAOIf(session).countPoints(task));;
		assertEquals(0, task.getPointCategories().size());
		assertEquals(200, task.getMaxPoints());
	}

	@Test
	void testCountPointsOptional() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(1);
		assertEquals(100, DAOFactory.PointCategoryDAOIf(session).countPoints(task));
		assertEquals(2, task.getPointCategories().size());
		assertEquals(100, task.getMaxPoints());
		int sum = 0;
		for (PointCategory category : task.getPointCategories()) {
			sum += category.getPoints();
		}
		assertEquals(150, sum);
	}

	@Test
	void testCountPoints() {
		Task task = DAOFactory.TaskDAOIf(session).getTask(3);
		assertEquals(150, DAOFactory.PointCategoryDAOIf(session).countPoints(task));
		assertEquals(2, task.getPointCategories().size());
		int sum = 0;
		for (PointCategory category : task.getPointCategories()) {
			sum += category.getPoints();
		}
		assertEquals(150, sum);
	}

	@Test
	void testAddRemove() {
		session.getTransaction().begin();
		Task task = DAOFactory.TaskDAOIf(session).getTask(3);
		assertEquals(2, task.getPointCategories().size());
		assertEquals(150, DAOFactory.PointCategoryDAOIf(session).countPoints(task));
		PointCategory fixCategory = DAOFactory.PointCategoryDAOIf(session).newPointCategory(task, 120, "something", false);
		assertEquals(270, DAOFactory.PointCategoryDAOIf(session).countPoints(task));
		session.refresh(task);
		assertEquals(3, task.getPointCategories().size());
		int sum = 0;
		for (PointCategory category : task.getPointCategories()) {
			sum += category.getPoints();
		}
		assertEquals(270, sum);
		DAOFactory.PointCategoryDAOIf(session).newPointCategory(task, 100, "optional", true);
		assertEquals(270, DAOFactory.PointCategoryDAOIf(session).countPoints(task));
		session.refresh(task);
		assertEquals(4, task.getPointCategories().size());
		sum = 0;
		for (PointCategory category : task.getPointCategories()) {
			sum += category.getPoints();
		}
		assertEquals(370, sum);
		DAOFactory.PointCategoryDAOIf(session).deletePointCategory(fixCategory);
		assertEquals(150, DAOFactory.PointCategoryDAOIf(session).countPoints(task));
		session.getTransaction().rollback();
	}
}
