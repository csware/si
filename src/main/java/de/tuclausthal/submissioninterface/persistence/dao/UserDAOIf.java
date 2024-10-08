/*
 * Copyright 2009-2010, 2021-2022, 2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import java.util.List;

import de.tuclausthal.submissioninterface.persistence.datamodel.User;

/**
 * Data Access Object Interface for the User-class
 * @author Sven Strickroth
 */
public interface UserDAOIf {
	/**
	 * Fetches a user with a specific id from the DB
	 * @param uid the user id
	 * @return the user or null
	 */
	User getUser(int uid);

	/**
	 * Fetch a user based on the username
	 * @param username the username of the user
	 * @return the user or null
	 */
	User getUserByUsername(String username);

	User getUserByEmail(String email);

	/**
	 * Create and store a new user with the username/email-address email in the DB
	 * @param username the username of the user
	 * @param email the email-address/username of the new user
	 * @param firstName
	 * @param lastName
	 * @return the new user
	 */
	User createUser(String username, String email, String firstName, String lastName);

	/**
	 * Create and store a new user with the username/email-address email in the DB
	 * @param username the username of the user
	 * @param email the email-address/username of the new user
	 * @param firstName
	 * @param lastName
	 * @param matrikelno the matrikelnumber of the new user
	 * @return the new user
	 */
	User createUser(String username, String email, String firstName, String lastName, int matrikelno);

	/**
	 * Get all users from the DB
	 * @return list of users
	 */
	List<User> getUsers();

	/**
	 * Get all admin-users from the DB
	 * @return list of admin-users
	 */
	List<User> getSuperUsers();

	void makeUserStudent(int uid, int matrikelno);
}
