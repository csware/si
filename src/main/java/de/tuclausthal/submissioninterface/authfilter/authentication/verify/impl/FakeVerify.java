/*
 * Copyright 2009-2010, 2020 Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of the SubmissionInterface.
 * 
 * SubmissionInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * 
 * SubmissionInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SubmissionInterface. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.authfilter.authentication.verify.impl;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginData;
import de.tuclausthal.submissioninterface.authfilter.authentication.verify.VerifyIf;
import de.tuclausthal.submissioninterface.authfilter.authentication.verify.VerifyResult;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

/**
 * Fake verifyer: Accepts all stored users with any password
 * @author Sven Strickroth
 */
public class FakeVerify implements VerifyIf {
	final private static Logger LOG = LoggerFactory.getLogger(FakeVerify.class);
	public FakeVerify(FilterConfig filterConfig) {
		LOG.warn("Using FakeVerify as authenticator! No passwords will be validated!");
	}

	@Override
	public VerifyResult checkCredentials(Session session, LoginData logindata, HttpServletRequest request) {
		UserDAOIf userdao = DAOFactory.UserDAOIf(session);
		User user = userdao.getUserByUsername(logindata.getUsername());
		if (user != null) {
			LOG.warn("Login using FakeVerify, no password was validated!");
		}
		return new VerifyResult(user);
	}
}
