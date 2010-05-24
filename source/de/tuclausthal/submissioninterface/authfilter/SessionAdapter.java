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

package de.tuclausthal.submissioninterface.authfilter;

import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;

/**
 * Adapter for HTTP-session to have a better interface for our stored variables
 * @author Sven Strickroth
 */
public class SessionAdapter {
	private HttpSession session = null;

	public void startNewSession(HttpServletRequest request) {
		//migrate session contents
		Object userID = session.getAttribute("userID");
		Object queuedTest = session.getAttribute("queuedTest");
		session.invalidate();
		session = request.getSession(true);
		session.setAttribute("userID", userID);
		session.setAttribute("queuedTest", queuedTest);
		session.setAttribute("ip", request.getRemoteAddr());
	}

	public SessionAdapter(HttpServletRequest request) {
		session = request.getSession(true);
	}

	public boolean isIPCorrect(String clientRemoteAddr) {
		return clientRemoteAddr.equals(session.getAttribute("ip"));
	}

	/**
	 * Binds the user to the session
	 * @param user
	 */
	public void setUser(User user) {
		session.setAttribute("userID", user.getUid());
		session.setAttribute("username", user.getEmail());
	}

	/**
	 * Reads the user from the session
	 * @param hibernateSession 
	 * @return the user or null if no user was stored to the session
	 */
	public User getUser(Session hibernateSession) {
		if (session.getAttribute("userID") != null) {
			return DAOFactory.UserDAOIf(hibernateSession).getUser((Integer) session.getAttribute("userID"));
		} else {
			return null;
		}
	}

	public Future<TestExecutorTestResult> getQueuedTest() {
		return (Future<TestExecutorTestResult>) session.getAttribute("queuedTest");
	}

	public void setQueuedTest(Future<TestExecutorTestResult> futureTestResult) {
		session.setAttribute("queuedTest", futureTestResult);
	}
}
