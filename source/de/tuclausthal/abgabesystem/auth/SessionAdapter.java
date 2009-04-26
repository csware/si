package de.tuclausthal.abgabesystem.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

/**
 * Adapter for HTTP-session to have a better interface for our stored variables
 * @author Sven Strickroth
 */
public class SessionAdapter {
	private HttpSession session = null;

	/*	private String ipAddress;
		private Boolean linkedToIP;*/

	public SessionAdapter(HttpServletRequest request) {
		session = request.getSession(true);
	}

	public void setUser(User user) {
		session.setAttribute("userID", user.getUid());
	}

	public User getUser() {
		if (session.getAttribute("userID") != null) {
			return DAOFactory.UserDAOIf().getUser((Integer) session.getAttribute("userID"));
		} else {
			return null;
		}
	}
}
