/*
 * Copyright 2010, 2020-2023 Sven Strickroth <email@cs-ware.de>
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
import java.time.ZonedDateTime;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "pointhistory")
public class PointHistory implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private Submission submission;
	private String field;
	private String removed;
	private String added;
	private Participation who;
	private ZonedDateTime date = ZonedDateTime.now();

	public PointHistory() {}

	public PointHistory(Submission submission, String field, String removed, String added, Participation marker) {
		this.submission = submission;
		this.field = field;
		this.removed = removed;
		this.added = added;
		this.who = marker;
	}

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
	 * @return the submission
	 */
	@ManyToOne(optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public Submission getSubmission() {
		return submission;
	}

	/**
	 * @param submission the submission to set
	 */
	public void setSubmission(Submission submission) {
		this.submission = submission;
	}

	/**
	 * @return the field
	 */
	@Column(nullable = false)
	public String getField() {
		return field;
	}

	/**
	 * @param field the field to set
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * @return the ZonedDateTime
	 */
	@Basic
	@Column(nullable = false)
	public ZonedDateTime getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(ZonedDateTime date) {
		this.date = date;
	}

	/**
	 * @return the removed
	 */
	@Column(nullable = false, length = 65536)
	public String getRemoved() {
		return removed;
	}

	/**
	 * @param removed the removed to set
	 */
	public void setRemoved(String removed) {
		this.removed = removed;
	}

	/**
	 * @return the added
	 */
	@Column(nullable = false, length = 65536)
	public String getAdded() {
		return added;
	}

	/**
	 * @param added the added to set
	 */
	public void setAdded(String added) {
		this.added = added;
	}

	/**
	 * @return the who
	 */
	@ManyToOne(optional = false)
	public Participation getWho() {
		return who;
	}

	/**
	 * @param who the who to set
	 */
	public void setWho(Participation who) {
		this.who = who;
	}

	@Override
	public String toString() {
		return MethodHandles.lookup().lookupClass().getSimpleName() + " (" + Integer.toHexString(hashCode()) + "): id:" + getId() + "; who:" + (getWho() == null ? "null" : getWho().getId()) + "; date:" + getDate();
	}
}
