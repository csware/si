/*
 * Copyright 2009-2010, 2020, 2022-2023 Sven Strickroth <email@cs-ware.de>
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

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Function test
 * @author Sven Strickroth
 */
@Entity
@Table(name = "testscounts")
public class TestCount implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private Test test;
	private User user;
	private int timesExecuted = 0;

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the test
	 */
	@ManyToOne(optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public Test getTest() {
		return test;
	}

	/**
	 * @param test the test to set
	 */
	public void setTest(Test test) {
		this.test = test;
	}

	/**
	 * @return the user
	 */
	@ManyToOne(optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the timesExecuted
	 */
	public int getTimesExecuted() {
		return timesExecuted;
	}

	/**
	 * @param timesExecuted the timesExecuted to set
	 */
	public void setTimesExecuted(int timesExecuted) {
		this.timesExecuted = timesExecuted;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): id:" + getId() + "; userid:" + (getUser() == null ? "null" : getUser().getUid()) + "; testid:" + (getTest() == null ? "null" : getTest().getId()) + "; timesExecuted:" + getTimesExecuted();
	}
}
