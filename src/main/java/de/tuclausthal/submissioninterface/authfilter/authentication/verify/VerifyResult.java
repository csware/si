/*
 * Copyright 2020 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.User;

public class VerifyResult {
	public VerifyResult() {}

	public VerifyResult(User user) {
		this.verifiedUser = user;
	}

	public boolean wasLoginSuccessful() {
		return verifiedUser != null || (username != null && lastName != null && firstName != null && mail != null);
	}

	public User verifiedUser = null;
	public String username = null;
	public String lastName = null;
	public String firstName = null;
	public String mail = null;
	public Integer matrikelNumber = null;
}
