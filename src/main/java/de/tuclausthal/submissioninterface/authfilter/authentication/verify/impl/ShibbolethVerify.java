/*
 * Copyright 2020-2021 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.authfilter.authentication.verify.impl;

import java.lang.invoke.MethodHandles;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginData;
import de.tuclausthal.submissioninterface.authfilter.authentication.login.impl.Shibboleth;
import de.tuclausthal.submissioninterface.authfilter.authentication.verify.VerifyIf;
import de.tuclausthal.submissioninterface.authfilter.authentication.verify.VerifyResult;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Shibboleth credential verifyer
 * @author Sven Strickroth
 */
public class ShibbolethVerify implements VerifyIf {
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private String userAttribute;
	private String matrikelNumberAttribute;

	public ShibbolethVerify(FilterConfig filterConfig) {
		this.userAttribute = filterConfig.getInitParameter("userAttribute");
		this.matrikelNumberAttribute = filterConfig.getInitParameter("matrikelNumberAttribute");
	}

	@Override
	public VerifyResult checkCredentials(Session session, LoginData logindata, HttpServletRequest request) {
		if (request.getAttribute("Shib-Identity-Provider") == null) {
			LOG.error("No Shib-Identity-Provider request attribute found.");
			return null;
		}
		if (!logindata.getUsername().equals(Shibboleth.getAttribute(request, userAttribute))) {
			LOG.error("Got mismatching username from Shibboleth and LoginIf.");
			return null;
		}

		User user = DAOFactory.UserDAOIf(session).getUserByUsername(logindata.getUsername());

		String lastName = Shibboleth.getAttribute(request, "sn");
		String firstName = Shibboleth.getAttribute(request, "givenName");
		String mail = Shibboleth.getAttribute(request, "mail");
		VerifyResult result = null;
		if (user != null) {
			result = new VerifyResult(user);
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setEmail(mail);
		} else {
			result = new VerifyResult();
			result.username = logindata.getUsername();
			result.lastName = lastName;
			result.firstName = firstName;
			result.mail = mail;
			if (matrikelNumberAttribute != null) {
				String matrikelnumber = Shibboleth.getAttribute(request, matrikelNumberAttribute);
				if (matrikelnumber != null && Util.isInteger(matrikelnumber)) {
					result.matrikelNumber = Integer.parseInt(matrikelnumber);
				}
			}
		}
		if (!result.wasLoginSuccessful()) {
			LOG.warn("Shibboleth login worked, but not detected as logged in, missing data?!");
		}

		return result;
	}
}
