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

import java.io.Serializable;
import java.lang.invoke.MethodHandles;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Checklist test checklist
 * @author Sven Strickroth
 */
@Entity
@Table(name = "checklisttestcheckitem")
public class ChecklistTestCheckItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private int checkitemid;
	private ChecklistTest test;
	private String title;
	private boolean correct;
	private String feedback;

	/**
	 * @return the checkid
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getCheckitemid() {
		return checkitemid;
	}

	/**
	 * @param checkitemid the checkitemid to set
	 */
	public void setCheckitemid(int checkitemid) {
		this.checkitemid = checkitemid;
	}

	/**
	 * @return the test
	 */
	@ManyToOne
	@JoinColumn(name = "testid", nullable = false)
	public ChecklistTest getTest() {
		return test;
	}

	/**
	 * @param test the test to set
	 */
	public void setTest(ChecklistTest test) {
		this.test = test;
	}

	/**
	 * @return the title
	 */
	@Column(nullable = false, length = 65536)
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the correct
	 */
	public boolean isCorrect() {
		return correct;
	}

	/**
	 * @param correct the correct to set
	 */
	public void setCorrect(boolean correct) {
		this.correct = correct;
	}

	/**
	 * @return the feedback
	 */
	@Column(nullable = false, length = 65536)
	public String getFeedback() {
		return feedback;
	}

	/**
	 * @param feedback the feedback to set
	 */
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): checkitemid:" + getCheckitemid() + "; testid: " + (getTest() == null ? "null" : getTest().getId());
	}
}
