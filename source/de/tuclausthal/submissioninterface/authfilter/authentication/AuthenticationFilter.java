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

package de.tuclausthal.submissioninterface.authfilter.authentication;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginData;
import de.tuclausthal.submissioninterface.authfilter.authentication.login.LoginIf;
import de.tuclausthal.submissioninterface.authfilter.authentication.verify.VerifyIf;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;

/**
 * Authentication filter
 * @author Sven Strickroth
 */
public class AuthenticationFilter implements Filter {

	private LoginIf login;
	private VerifyIf verify;

	@Override
	public void destroy() {
	// nothing to do here
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		Session session = HibernateSessionHelper.getSession();
		SessionAdapter sa = new SessionAdapter((HttpServletRequest) request);
		if (sa.getUser(session) != null) {
			request.setAttribute("user", sa.getUser(session));
		} else {
			LoginData logindata = login.getLoginData((HttpServletRequest) request);
			if (logindata == null) {
				login.failNoData((HttpServletRequest) request, (HttpServletResponse) response);
				return;
			} else {
				User user = null;
				// if login requires no verification we load the user named in logindata
				if (login.requiresVerification()) {
					user = verify.checkCredentials(logindata);
				} else {
					user = DAOFactory.UserDAOIf(HibernateSessionHelper.getSessionFactory().openSession()).getUser(logindata.getUsername());
				}
				if (user == null) {
					login.failNoData("Username or password wrong.", (HttpServletRequest) request, (HttpServletResponse) response);
					return;
				} else {
					sa.setUser(user);
					request.setAttribute("user", sa.getUser());
					if (login.redirectAfterLogin() == true) {
						String queryString = "";
						if (((HttpServletRequest) request).getQueryString() != null) {
							queryString = "?" + ((HttpServletRequest) request).getQueryString();
						}
						((HttpServletResponse) response).sendRedirect(((HttpServletResponse) response).encodeRedirectURL(((HttpServletRequest) request).getRequestURL().toString() + queryString).replace("\r", "%0d").replace("\n", "%0a")));
					}
					return;
				}
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			login = (LoginIf) Class.forName(filterConfig.getInitParameter("login")).getDeclaredConstructor(FilterConfig.class).newInstance(filterConfig);
			verify = (VerifyIf) Class.forName(filterConfig.getInitParameter("verify")).getDeclaredConstructor(FilterConfig.class).newInstance(filterConfig);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}
	}
}
