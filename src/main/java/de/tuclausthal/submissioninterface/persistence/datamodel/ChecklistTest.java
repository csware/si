/*
 * Copyright 2021-2022 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.datamodel;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.OrderBy;

import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.NullTest;

/**
 * Checklist test
 * @author Sven Strickroth
 */
@Entity
public class ChecklistTest extends Test {
	private static final long serialVersionUID = 1L;

	private List<ChecklistTestCheckItem> checkItems;

	@Override
	@Transient
	public AbstractTest getTestImpl() {
		return new NullTest();
	}

	/**
	 * @return the checkItems
	 */
	@OneToMany(mappedBy = "test")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@OrderBy(clause = "checkitemid asc")
	public List<ChecklistTestCheckItem> getCheckItems() {
		return checkItems;
	}

	/**
	 * @param checkItems the checkItems to set
	 */
	public void setCheckItems(List<ChecklistTestCheckItem> checkItems) {
		this.checkItems = checkItems;
	}

	@Override
	@Transient
	public boolean TutorsCanRun() {
		return false;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): id:" + getId() + "; testtitle:" + getTestTitle() + "; taskid:" + (getTask() == null ? "null" : getTask().getTaskid());
	}
}
