/*
 * Copyright 2010, 2024 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.PointCategory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

/**
 * Data Access Object Interface for the PointCategory-class
 * @author Sven Strickroth
 */
public interface PointCategoryDAOIf {
	PointCategory newPointCategory(Task task, int points, String description, boolean optional);

	PointCategory getPointCategory(int id);

	void deletePointCategory(PointCategory pointCategory);

	int countPoints(Task task);
}
