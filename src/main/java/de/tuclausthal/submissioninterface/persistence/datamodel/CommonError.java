/*
 * Copyright 2021-2024 Sven Strickroth <email@cs-ware.de>
 * Copyright 2021 Florian Holzinger <f.holzinger@campus.lmu.de>
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
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "commonerrors")
public class CommonError implements Serializable {
	private static final long serialVersionUID = 1L;

	private int errorID;
	private String title;
	private String commonErrorName;
	private Set<TestResult> testResults = new HashSet<>();
	private Integer type;
	private Test test;

	public static enum Type {
		CompileTimeError, RunTimeError
	}

	public CommonError() {}

	public CommonError(String title, String commonErrorName, Test test) {
		this.title = title;
		this.commonErrorName = commonErrorName;
		this.test = test;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getErrorID() {
		return errorID;
	}

	public void setErrorID(int errorID) {
		this.errorID = errorID;
	}

	@Column(nullable = false)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "testresults_commonerror", joinColumns = @JoinColumn(name = "errorID"), inverseJoinColumns = @JoinColumn(name = "testresultID"))
	public Set<TestResult> getTestResults() {
		return testResults;
	}

	public void setTestResults(Set<TestResult> testResults) {
		this.testResults = testResults;
	}

	@Column(columnDefinition = "TINYINT")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Transient
	public void setType(Type type) {
		this.setType(type.ordinal());
	}

	@Transient
	public Type getTypedType() {
		if (this.type == null) {
			return null;
		}
		return Type.values()[this.type];
	}

	@Column(nullable = false)
	public String getCommonErrorName() {
		return commonErrorName;
	}

	public void setCommonErrorName(String commonErrorName) {
		this.commonErrorName = commonErrorName;
	}

	/**
	 * @return the test
	 */
	@ManyToOne
	@JoinColumn(name = "testId", nullable = false)
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

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): errorid:" + getErrorID() + "; testid: " + (getTest() == null ? "null" : getTest().getId());
	}
}
