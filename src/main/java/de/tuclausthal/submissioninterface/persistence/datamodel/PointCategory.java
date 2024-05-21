/*
 * Copyright 2010, 2020, 2022-2024 Sven Strickroth <email@cs-ware.de>
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
@Table(name = "pointcategories")
public class PointCategory implements Serializable {
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private int pointcatid;
	@JsonBackReference
	private Task task;
	private int points;
	private String description;
	private boolean optional = false;

	// for Hibernate
	protected PointCategory() {}

	public PointCategory(Task task, int points, String description, boolean optional) {
		this.task = task;
		this.points = points;
		this.description = description;
		this.optional = optional;
	}

	/**
	 * @return the pointcatid
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getPointcatid() {
		return pointcatid;
	}

	/**
	 * @param pointcatid the pointcatid to set
	 */
	public void setPointcatid(int pointcatid) {
		this.pointcatid = pointcatid;
	}

	/**
	 * @return the task
	 */
	@ManyToOne
	@JoinColumn(name = "taskid", nullable = false)
	public Task getTask() {
		return task;
	}

	/**
	 * @param task the task to set
	 */
	public void setTask(Task task) {
		this.task = task;
	}

	/**
	 * @return the points
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * @param points the points to set
	 */
	public void setPoints(int points) {
		this.points = points;
	}

	/**
	 * @return the description
	 */
	@Column(nullable = false)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the optional
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * @param optional the optional to set
	 */
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): pointcatid:" + getPointcatid() + "; points:" + getPoints() + "; optional:" + isOptional() + "; taskid:" + (getTask() == null ? "null" : getTask().getTaskid()) + "; description:" + getDescription();
	}
}
