/*
 * Copyright 2010, 2020-2022 Sven Strickroth <email@cs-ware.de>
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

import javax.persistence.Entity;
import javax.persistence.Transient;

import de.tuclausthal.submissioninterface.testframework.tests.AbstractTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.JavaCommentsMetricTest;

@Entity
public class CommentsMetricTest extends Test {
	private static final long serialVersionUID = 1L;

	private int minProzent;
	private String excludedFiles;

	/**
	 * @return the minProzent
	 */
	public int getMinProzent() {
		return minProzent;
	}

	/**
	 * @param minProzent the minProzent to set
	 */
	public void setMinProzent(int minProzent) {
		this.minProzent = minProzent;
	}

	@Override
	@Transient
	public AbstractTest getTestImpl() {
		return new JavaCommentsMetricTest(this);
	}

	/**
	 * @return the excludedFiles
	 */
	public String getExcludedFiles() {
		return excludedFiles;
	}

	/**
	 * @param excludedFiles the excludedFiles to set
	 */
	public void setExcludedFiles(String excludedFiles) {
		this.excludedFiles = excludedFiles;
	}

	@Override
	@Transient
	public boolean TutorsCanRun() {
		return true;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): id:" + getId() + "; testtitle:" + getTestTitle() + "; taskid:" + (getTask() == null ? "null" : getTask().getTaskid());
	}
}
