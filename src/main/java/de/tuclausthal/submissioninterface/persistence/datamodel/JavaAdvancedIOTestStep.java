/*
 * Copyright 2020, 2022-2024 Sven Strickroth <email@cs-ware.de>
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

@Entity
@Table(name = "javaadvancedioteststep")
public class JavaAdvancedIOTestStep implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private int teststepid;
	@ManyToOne
	@JoinColumn(name = "testid", nullable = false)
	@JsonBackReference
	private JavaAdvancedIOTest test;
	@Column(nullable = false)
	private String title;
	@Column(nullable = false, length = 65536)
	private String testcode;
	@Column(nullable = false, length = 65536)
	private String expect;

	// for Hibernate
	protected JavaAdvancedIOTestStep() {}

	public JavaAdvancedIOTestStep(JavaAdvancedIOTest test, String title, String testcode, String expect) {
		this.test = test;
		this.title = title;
		this.testcode = testcode;
		this.expect = expect;
	}

	/**
	 * @return the teststepid
	 */
	public int getTeststepid() {
		return teststepid;
	}

	/**
	 * @param teststepid the teststepid to set
	 */
	public void setTeststepid(int teststepid) {
		this.teststepid = teststepid;
	}

	/**
	 * @return the test
	 */
	public JavaAdvancedIOTest getTest() {
		return test;
	}

	/**
	 * @param test the test to set
	 */
	public void setTest(JavaAdvancedIOTest test) {
		this.test = test;
	}

	/**
	 * @return the testcode
	 */
	public String getTestcode() {
		return testcode;
	}

	/**
	 * @param testcode the testcode to set
	 */
	public void setTestcode(String testcode) {
		this.testcode = testcode;
	}

	/**
	 * @return the expect
	 */
	public String getExpect() {
		return expect;
	}

	/**
	 * @param expect the expect to set
	 */
	public void setExpect(String expect) {
		this.expect = expect;
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

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): teststepid:" + getTeststepid() + "; testid: " + (getTest() == null ? "null" : getTest().getId());
	}
}
