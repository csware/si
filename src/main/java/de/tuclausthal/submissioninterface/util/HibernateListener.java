/*
 * Copyright 2009-2010, 2020 Sven Strickroth <email@cs-ware.de>
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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

public class HibernateListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		HibernateSessionHelper.getSessionFactory(); // Just call the static initializer of that class    
	}

	public void contextDestroyed(ServletContextEvent event) {
		HibernateSessionHelper.getSessionFactory().close(); // Free all resources
		AbandonedConnectionCleanupThread.checkedShutdown();
	}
}
