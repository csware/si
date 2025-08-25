/*
 * Copyright 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2025 Christian Wagner <christian.wagner@campus.lmu.de>
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

import java.io.Serial;
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
@Table(name = "haskellruntimetestidentifier")
public class HaskellRuntimeTestIdentifier implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private int identifierid;

	@ManyToOne
	@JoinColumn(name = "testid", nullable = false)
	@JsonBackReference
	private HaskellRuntimeTest haskellRuntimeTest;

	@Column(nullable = false)
	private String identifierClass;

	@Column
	private String functionName;

	@Column
	private String functionType;

	@Column
	private String functionDefaultType;

	@Column
	private String functionConcreteType;

	@Column
	private String newtypeOrDataTypename;

	@Column
	private String newtypeOrDataDefinition;

	@Column(length = 65536)
	private String newtypeOrDataArbitraryInstance;

	@Column
	private String className;

	@Column(length = 65536)
	private String classDefinition;

	// for Hibernate
	protected HaskellRuntimeTestIdentifier() {}

	public HaskellRuntimeTestIdentifier(HaskellRuntimeTest haskellRuntimeTest, String identifierClass) {
		this.haskellRuntimeTest = haskellRuntimeTest;
		this.identifierClass = identifierClass;
	}

	/**
	 * @return the identifierid
	 */
	public int getIdentifierid() {
		return identifierid;
	}

	/**
	 * @param identifierid the identifierid to set
	 */
	public void setIdentifierid(int identifierid) {
		this.identifierid = identifierid;
	}

	/**
	 * @return the haskellRuntimeTest
	 */
	public HaskellRuntimeTest getHaskellRuntimeTest() {
		return haskellRuntimeTest;
	}

	/**
	 * @param haskellRuntimeTest the haskellRuntimeTest to set
	 */
	public void setHaskellRuntimeTest(HaskellRuntimeTest haskellRuntimeTest) {
		this.haskellRuntimeTest = haskellRuntimeTest;
	}

	/**
	 * @return the identifierClass
	 */
	public String getIdentifierClass() {
		return identifierClass;
	}

	/**
	 * @param identifierClass the identifierClass to set
	 */
	public void setIdentifierClass(String identifierClass) {
		this.identifierClass = identifierClass;
	}

	/**
	 * @return the functionName
	 */
	public String getFunctionName() {
		return functionName;
	}

	/**
	 * @param functionName the functionName to set
	 */
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	/**
	 * @return the functionType
	 */
	public String getFunctionType() {
		return functionType;
	}

	/**
	 * @param functionType the functionType to set
	 */
	public void setFunctionType(String functionType) {
		this.functionType = functionType;
	}

	/**
	 * @return the functionDefaultType
	 */
	public String getFunctionDefaultType() {
		return functionDefaultType;
	}

	/**
	 * @param functionDefaultType the functionDefaultType to set
	 */
	public void setFunctionDefaultType(String functionDefaultType) {
		this.functionDefaultType = functionDefaultType;
	}

	/**
	 * @return the functionConcreteType
	 */
	public String getFunctionConcreteType() {
		return functionConcreteType;
	}

	/**
	 * @param functionConcreteType the functionConcreteType to set
	 */
	public void setFunctionConcreteType(String functionConcreteType) {
		this.functionConcreteType = functionConcreteType;
	}

	/**
	 * @return the newtypeOrDataTypename
	 */
	public String getNewtypeOrDataTypename() {
		return newtypeOrDataTypename;
	}

	/**
	 * @param newtypeOrDataTypename the newtypeOrDataTypename to set
	 */
	public void setNewtypeOrDataTypename(String newtypeOrDataTypename) {
		this.newtypeOrDataTypename = newtypeOrDataTypename;
	}

	/**
	 * @return the newtypeOrDataDefinition
	 */
	public String getNewtypeOrDataDefinition() {
		return newtypeOrDataDefinition;
	}

	/**
	 * @param newtypeOrDataDefinition the newtypeOrDataDefinition to set
	 */
	public void setNewtypeOrDataDefinition(String newtypeOrDataDefinition) {
		this.newtypeOrDataDefinition = newtypeOrDataDefinition;
	}

	/**
	 * @return the newtypeOrDataArbitraryInstance
	 */
	public String getNewtypeOrDataArbitraryInstance() {
		return newtypeOrDataArbitraryInstance;
	}

	/**
	 * @param newtypeOrDataArbitraryInstance the newtypeOrDataArbitraryInstance to set
	 */
	public void setNewtypeOrDataArbitraryInstance(String newtypeOrDataArbitraryInstance) {
		this.newtypeOrDataArbitraryInstance = newtypeOrDataArbitraryInstance;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return the classDefinition
	 */
	public String getClassDefinition() {
		return classDefinition;
	}

	/**
	 * @param classDefinition the classDefinition to set
	 */
	public void setClassDefinition(String classDefinition) {
		this.classDefinition = classDefinition;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): identifierid:" + getIdentifierid() + "; testid: " + (getHaskellRuntimeTest() == null ? "null" : getHaskellRuntimeTest().getId());
	}
}
