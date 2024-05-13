/*
 * Copyright 2021-2024 Sven Strickroth <email@cs-ware.de>
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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Checklist test checklist
 * @author Sven Strickroth
 */
@Entity
@Table(name = "checklisttestcheckitem")
public class ChecklistTestCheckItem implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private int checkitemid;
	@ManyToOne
	@JoinColumn(name = "testid", nullable = false)
	@JsonBackReference
	private ChecklistTest test;
	@Column(nullable = false, length = 65536)
	private String title;
	private boolean correct;
	@Column(nullable = false, length = 65536)
	private String feedback;

	/**
	 * @return the checkid
	 */
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
