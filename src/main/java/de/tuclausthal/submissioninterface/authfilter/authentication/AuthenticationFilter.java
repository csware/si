/*
 * Copyright 2009 - 2012, 2020 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.authfilter.authentication;

import java.io.IOException;
import java.util.Date;

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
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;

/**
 * Authentication filter
 * @author Sven Strickroth
 */
public class AuthenticationFilter implements Filter {
	final private Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

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
			response.addHeader("LoggedIn", "false");
			LoginData logindata = login.getLoginData(request);
			if (logindata == null) {
				login.failNoData(request, response);
				if (session.isOpen()) {
					session.close();
				}
				return;
			}
			User user = null;
			// if login requires no verification we load the user named in logindata
			if (login.requiresVerification()) {
				user = verify.checkCredentials(session, logindata);
			} else {
				user = DAOFactory.UserDAOIf(session).getUserByUsername(logindata.getUsername());
			}
			if (user == null) {
				login.failNoData("Login fehlgeschlagen! Bitte versuchen Sie es erneut.", request, response);
				if (session.isOpen()) {
					session.close();
				}
				return;
			}

			Transaction tx = session.beginTransaction();
			user.setLastLoggedIn(new Date());
			DAOFactory.UserDAOIf(session).saveUser(user);
			tx.commit();
			sa.setUser(user, request.getRemoteAddr());
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
		response.sendRedirect(response.encodeRedirectURL((request.getRequestURL().toString() + queryString).replace("\r", "%0d").replace("\n", "%0a")));
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
			log.error("Could not initialize Login or Verify interface", e);
			throw new ServletException(e.getMessage());
		}
	}
}
