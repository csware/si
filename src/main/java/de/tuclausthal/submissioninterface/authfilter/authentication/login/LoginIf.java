/*
 * Copyright 2009, 2024 Sven Strickroth <email@cs-ware.de>
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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Login-Method-Interface
 * @author Sven Strickroth
 */
public interface LoginIf {
	/**
	 * Returns if (further) verification of the credentials is needed
	 * if not: the user from LoginData is loaded automatically
	 * @return whether verification of the gathered credentials is needed
	 */
	boolean requiresVerification();

	boolean isSubsequentAuthRequest(HttpServletRequest request);

	/**
	 * Returns the user credentials
	 * @param request
	 * @return the LoginData or null
	 */
	LoginData getLoginData(HttpServletRequest request);

	/**
	 * Requests credentials from the user
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	void failNoData(HttpServletRequest request, HttpServletResponse response) throws IOException;

	/**
	 * Requests credentials from the user with error-message
	 * @param error error message text
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	void failNoData(String error, HttpServletRequest request, HttpServletResponse response) throws IOException;

	/**
	 * Returns whether a http redirect is needed after login
	 * @return returns true if a redirect is required
	 */
	boolean redirectAfterLogin();
}
