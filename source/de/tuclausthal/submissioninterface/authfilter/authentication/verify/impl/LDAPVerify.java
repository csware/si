/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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

import java.text.MessageFormat;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.servlet.FilterConfig;

import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginData;
import de.tuclausthal.submissioninterface.authfilter.authentication.verify.VerifyIf;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

/**
 * LDAP credential verifyer
 * @author Sven Strickroth
 */
public class LDAPVerify implements VerifyIf {
	private String providerURL;
	private String securityAuthentication;
	private String securityPrincipal;

	public LDAPVerify(FilterConfig filterConfig) {
		this.providerURL = filterConfig.getInitParameter("PROVIDER_URL");
		this.securityAuthentication = filterConfig.getInitParameter("SECURITY_AUTHENTICATION");
		this.securityPrincipal = filterConfig.getInitParameter("SECURITY_PRINCIPAL");
	}

	@Override
	public User checkCredentials(LoginData logindata) {
		User user = null;
		String username = logindata.getUsername();
		String password = logindata.getPassword();
		// Set up environment for creating initial context
		Hashtable<String, String> env = new Hashtable<String, String>(5);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, providerURL);
		env.put(Context.SECURITY_AUTHENTICATION, securityAuthentication);

		if (!username.matches("^[a-z0-9A-Z]+$")) {
			// die here!
			return null;
		}

		env.put(Context.SECURITY_PRINCIPAL, MessageFormat.format(securityPrincipal, new Object[] { username }));
		env.put(Context.SECURITY_CREDENTIALS, password);

		try {
			// Create initial context
			LdapContext ctx = new InitialLdapContext(env, null);

			// wenn gefunden, dann lokalen user suchen bzw. erstellen
			UserDAOIf userdao = DAOFactory.UserDAOIf();
			user = userdao.getUser((String) ctx.getAttributes("uid=" + username).get("uid").get());

			if (user == null) {
				if (ctx.getAttributes("uid=" + username).get("tucmatrikelNr") != null) {
					userdao.createUser((String) ctx.getAttributes("uid=" + username).get("uid").get());
				}
			}

			// Close the context when we're done
			ctx.close();
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return user;
	}
}
