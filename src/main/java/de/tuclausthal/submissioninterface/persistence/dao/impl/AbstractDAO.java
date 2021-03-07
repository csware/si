package de.tuclausthal.submissioninterface.persistence.dao.impl;

import org.hibernate.Session;

/**
 * @author Sven Strickroth
 *
 */
public class AbstractDAO {
	private Session session;

	public AbstractDAO(Session session) {
		this.session = session;
	}

	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}
}
