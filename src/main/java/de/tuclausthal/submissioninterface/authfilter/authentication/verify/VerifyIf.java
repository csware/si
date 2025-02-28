/*
 * Copyright 2009-2010, 2020, 2024-2025 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.authfilter.authentication.verify;

import jakarta.servlet.http.HttpServletRequest;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginData;

/**
 * Username/Password verifyer interface
 * @author Sven Strickroth
 */
public interface VerifyIf {
	/**
	 * Checks the credentials provided in logindata
	 * @param session
	 * @param logindata
	 * @param request
	 * @return the user or null if authentication failed
	 */
	VerifyResult checkCredentials(Session session, LoginData logindata, HttpServletRequest request);
}
