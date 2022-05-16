/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.authfilter.authentication.login;

/**
 * Credential data storage
 * @author Sven Strickroth
 */
public class LoginData {
	private String username;
	private String password;

	/**
	 * @param username
	 * @param password
	 */
	public LoginData(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Gets the stored username
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Gets the stored password
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
}
