/*
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;

/**
 * Adapter for HTTP-session to have a better interface for our stored variables
 * @author Sven Strickroth
 */
public class RequestAdapter {
	private HttpServletRequest request;

	public RequestAdapter(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * Reads the user from the session
	 * @return the user or null if no user was stored to the session
	 */
	public User getUser() {
		return getUser(request);
	}

	public static User getUser(HttpServletRequest request) {
		return (User) getSessionAdapter(request).getUser();
	}

	/**
	 * Gibt eine Hibernation-Datenbank-Sitzung zurück
	 * @return Hibernate Session
	 * @throws HibernateException
	 */
	public Session getSession() throws HibernateException {
		return getSession(request);
	}

	public static Session getSession(HttpServletRequest request) throws HibernateException {
		if (request.getAttribute("hibernateSession") == null) {
			request.setAttribute("hibernateSession", HibernateSessionHelper.getSessionFactory().openSession());
		}
		return (Session) request.getAttribute("hibernateSession");
	}

	public SessionAdapter getSessionAdapter() {
		return getSessionAdapter(request);
	}

	/**
	 * @return the sessionAdapter
	 */
	public static SessionAdapter getSessionAdapter(HttpServletRequest request) {
		if (request.getAttribute("sessionAdapter") == null) {
			request.setAttribute("sessionAdapter", new SessionAdapter(request));
		}
		return (SessionAdapter) request.getAttribute("sessionAdapter");
	}
}
