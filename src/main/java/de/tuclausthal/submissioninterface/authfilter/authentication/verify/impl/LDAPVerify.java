/*
 * Copyright 2009-2011, 2017, 2020-2021, 2025 Sven Strickroth <email@cs-ware.de>
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
import java.text.MessageFormat;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginData;
import de.tuclausthal.submissioninterface.authfilter.authentication.verify.VerifyIf;
import de.tuclausthal.submissioninterface.authfilter.authentication.verify.VerifyResult;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * LDAP credential verifyer
 * @author Sven Strickroth
 */
public class LDAPVerify implements VerifyIf {
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private String[] providerURLs;
	private String securityAuthentication;
	private String securityPrincipal;
	private String userAttribute;
	private String matrikelNumberAttribute;

	public LDAPVerify(FilterConfig filterConfig) {
		this.providerURLs = filterConfig.getInitParameter("PROVIDER_URL").split(";");
		this.securityAuthentication = filterConfig.getInitParameter("SECURITY_AUTHENTICATION");
		this.securityPrincipal = filterConfig.getInitParameter("SECURITY_PRINCIPAL");
		this.userAttribute = filterConfig.getInitParameter("userAttribute");
		this.matrikelNumberAttribute = filterConfig.getInitParameter("matrikelNumberAttribute");
	}

	@Override
	public VerifyResult checkCredentials(Session session, LoginData logindata, HttpServletRequest request) {
		User user = null;
		String username = logindata.getUsername();
		String password = logindata.getPassword();

		if (!username.matches("^[a-z0-9A-Z]+$")) {
			// die here!
			return null;
		}

		// Set up environment for creating initial context
		Hashtable<String, String> env = new Hashtable<>(5);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_AUTHENTICATION, securityAuthentication);

		env.put(Context.SECURITY_PRINCIPAL, MessageFormat.format(securityPrincipal, new Object[] { username }));
		env.put(Context.SECURITY_CREDENTIALS, password);

		VerifyResult result = null;
		for (String providerURL : providerURLs) {
			env.put(Context.PROVIDER_URL, providerURL);
			try {
				// Create initial context
				LdapContext ctx = new InitialLdapContext(env, null);

				// if ldap user found, search or create local user
				UserDAOIf userdao = DAOFactory.UserDAOIf(session);
				user = userdao.getUserByUsername(username);

				String lastName = (String) ctx.getAttributes(userAttribute + "=" + username).get("sn").get();
				String firstName = (String) ctx.getAttributes(userAttribute + "=" + username).get("cn").get();
				firstName = firstName.substring(0, firstName.lastIndexOf(lastName) - 1);
				String mail = (String) ctx.getAttributes(userAttribute + "=" + username).get("mail").get();
				if (user != null) {
					result = new VerifyResult(user);
					user.setFirstName(firstName);
					user.setLastName(lastName);
					user.setEmail(mail);
				} else {
					result = new VerifyResult();
					result.username = username;
					result.lastName = lastName;
					result.firstName = firstName;
					result.mail = mail;
					if (matrikelNumberAttribute != null && ctx.getAttributes(userAttribute + "=" + username).get(matrikelNumberAttribute) != null && Util.isInteger((String) ctx.getAttributes(userAttribute + "=" + username).get(matrikelNumberAttribute).get())) {
						result.matrikelNumber = Integer.parseInt((String) ctx.getAttributes(userAttribute + "=" + username).get(matrikelNumberAttribute).get());
					}
				}

				// Close the context when we're done
				ctx.close();

				break;
			} catch (AuthenticationException e) {
				// ignore authentication errors
				break;
			} catch (NamingException e) {
				LOG.error("Could not reach LDAP server \"" + providerURL + "\"", e);
			}
		}
		return result;
	}
}
