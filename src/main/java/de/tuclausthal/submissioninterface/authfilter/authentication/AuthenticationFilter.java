/*
 * Copyright 2009-2012, 2020-2023 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.authfilter.authentication;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginData;
import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginIf;
import de.tuclausthal.submissioninterface.authfilter.authentication.verify.VerifyIf;
import de.tuclausthal.submissioninterface.authfilter.authentication.verify.VerifyResult;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Authentication filter
 * @author Sven Strickroth
 */
public class AuthenticationFilter implements Filter {
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private boolean bindToIP = false;
	private LoginIf login;
	private VerifyIf verify;

	@Override
	public void destroy() {
		// nothing to do here
	}

	@Override
	public void doFilter(ServletRequest filterRequest, ServletResponse filterResponse, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) filterRequest;
		HttpServletResponse response = (HttpServletResponse) filterResponse;
		Session session = RequestAdapter.getSession(request);
		SessionAdapter sa = RequestAdapter.getSessionAdapter(request);
		response.addHeader("Strict-Transport-Security", "max-age=31536000");
		response.addHeader("X-Frame-Options", "SAMEORIGIN");
		request.setCharacterEncoding("UTF-8"); // set character encoding here, because Eclipse has a bug with web.xml, cf. https://bugs.eclipse.org/bugs/show_bug.cgi?id=543377
		if (sa.getUser() == null || (bindToIP && !sa.isIPCorrect(request.getRemoteAddr()))) {
			LoginData logindata = login.getLoginData(request);
			if (logindata == null) {
				response.addHeader("LoggedIn", "false");
				login.failNoData(request, response);
				if (session.isOpen()) {
					session.close();
				}
				return;
			}
			VerifyResult verifyResult = null;
			// if login requires no verification we load the user named in logindata
			if (login.requiresVerification()) {
				verifyResult = verify.checkCredentials(session, logindata, request);
			} else {
				verifyResult = new VerifyResult(DAOFactory.UserDAOIf(session).getUserByUsername(logindata.getUsername()));
			}
			if (verifyResult == null || !verifyResult.wasLoginSuccessful()) {
				response.addHeader("LoggedIn", "false");
				login.failNoData("Login fehlgeschlagen! Bitte versuchen Sie es erneut.", request, response);
				if (session.isOpen()) {
					session.close();
				}
				return;
			}
			if (verifyResult.wasLoginSuccessful() && verifyResult.verifiedUser == null) {
				// need to create user
				if (verifyResult.matrikelNumber != null) {
					Transaction tx = session.beginTransaction();
					verifyResult.verifiedUser = DAOFactory.UserDAOIf(session).createUser(verifyResult.username, verifyResult.mail, verifyResult.firstName, verifyResult.lastName, verifyResult.matrikelNumber);
					tx.commit();
				} else {
					Transaction tx = session.beginTransaction();
					verifyResult.verifiedUser = DAOFactory.UserDAOIf(session).createUser(verifyResult.username, verifyResult.mail, verifyResult.firstName, verifyResult.lastName);
					tx.commit();
				}
				if (verifyResult.verifiedUser == null) {
					LOG.error("Creating new user failed!");
					login.failNoData("Anlegen des neuen Nutzers fehlgeschlagen. Bitte versuchen Sie es erneut.", request, response);
					session.close();
					return;
				}
			}

			Transaction tx = session.beginTransaction();
			verifyResult.verifiedUser.setLastLoggedIn(ZonedDateTime.now());
			session.persist(verifyResult.verifiedUser);
			tx.commit();
			sa.setUser(verifyResult.verifiedUser, request.getRemoteAddr());
			if (login.redirectAfterLogin() == true) {
				performRedirect(request, response);
				if (session.isOpen()) {
					session.close();
				}
				return;
			}
		} else if (login.redirectAfterLogin() && login.isSubsequentAuthRequest(request)) {
			performRedirect(request, response);
			if (session.isOpen()) {
				session.close();
			}
			return;
		}
		request.setAttribute("username", sa.getUser().getUsername());
		try {
			if ("POST".equalsIgnoreCase(request.getMethod())) {
				StringBuilder correctOrigin = new StringBuilder(request.getScheme());
				correctOrigin.append("://");
				correctOrigin.append(request.getServerName());
				if (("https".equals(request.getScheme()) && request.getLocalPort() != 443) || ("http".equals(request.getScheme()) && request.getLocalPort() != 80)) {
					correctOrigin.append(":");
					correctOrigin.append(request.getLocalPort());
				}
				String origin = request.getHeader("Origin");
				if (origin == null) {
					if (request.getHeader("Referer") == null || !request.getHeader("Referer").startsWith(correctOrigin.toString() + "/")) {
						// just for compatibility of old browsers that do not send the Origin header
						LOG.warn("POST-CORS request-blocked: referer mismatched: \"" + request.getHeader("Referer") + "\"");
						response.sendError(HttpServletResponse.SC_FORBIDDEN, "Cross-Origin POST request blocked");
						return;
					}
				} else if (!correctOrigin.toString().equals(origin)) {
					LOG.warn("POST-CORS request-blocked: Origin mismatched: \"" + origin + "\"");
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "Cross-Origin POST request blocked");
					return;
				}
			}
			chain.doFilter(request, response);
		} finally {
			if (session.getTransaction() != null && session.getTransaction().isActive()) {
				session.getTransaction().rollback();
			}
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	private void performRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String queryString = "";
		if (request.getQueryString() != null) {
			queryString = "?" + request.getQueryString();
		}
		response.sendRedirect(Util.generateRedirectURL((request.getRequestURL().toString() + queryString).replace("\r", "%0d").replace("\n", "%0a"), response));
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if ("true".equalsIgnoreCase(filterConfig.getInitParameter("bindToIP")) || "yes".equalsIgnoreCase(filterConfig.getInitParameter("bindToIP")) || "1".equalsIgnoreCase(filterConfig.getInitParameter("bindToIP"))) {
			bindToIP = true;
		}
		try {
			login = (LoginIf) Class.forName(filterConfig.getInitParameter("login")).getDeclaredConstructor(FilterConfig.class).newInstance(filterConfig);
			verify = (VerifyIf) Class.forName(filterConfig.getInitParameter("verify")).getDeclaredConstructor(FilterConfig.class).newInstance(filterConfig);
		} catch (Exception e) {
			LOG.error("Could not initialize Login or Verify interface", e);
			throw new ServletException(e.getMessage());
		}
	}
}
