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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "dockerteststep")
public class DockerTestStep implements Serializable {
	private static final long serialVersionUID = 1L;

	private int teststepid;
	private DockerTest test;
	private String title;
	private String testcode;
	private String expect;

	// for Hibernate
	protected DockerTestStep() {}

	public DockerTestStep(DockerTest test, String title, String testcode, String expect) {
		this.test = test;
		this.title = title;
		this.testcode = testcode;
		this.expect = expect;
	}

	/**
	 * @return the teststepid
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
	@ManyToOne
	@JoinColumn(name = "testid", nullable = false)
	public DockerTest getTest() {
		return test;
	}

	/**
	 * @param test the test to set
	 */
	public void setTest(DockerTest test) {
		this.test = test;
	}

	/**
	 * @return the testcode
	 */
	@Column(nullable = false, length = 65536)
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
	@Column(nullable = false, length = 65536)
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
	@Column(nullable = false)
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
}
