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

package de.tuclausthal.submissioninterface.util;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

/**
 * Hibernate Session Helper Singleton+Facade
 * @author Sven Strickroth
 *
 */
public class HibernateSessionHelper {
	private static final ThreadLocal<Session> session = new ThreadLocal<Session>();
	private static final SessionFactory sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();

	/**
	 * Gibt eine Hibernation-Datenbank-Sitzung zurück
	 * @return
	 * @throws HibernateException
	 */
	public static Session getSession() throws HibernateException {
		Session session = HibernateSessionHelper.session.get();
		if (session == null) {
			session = sessionFactory.openSession();
			HibernateSessionHelper.session.set(session);
		}
		return session;
	}
}
