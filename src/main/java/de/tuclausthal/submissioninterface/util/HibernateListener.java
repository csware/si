/*
 * Copyright 2009-2010, 2020-2022, 2025 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.util;

import java.lang.invoke.MethodHandles;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateListener implements ServletContextListener {
	final private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void contextInitialized(ServletContextEvent event) {
		LOG.info("Initializing Hibernate Listener in context [{}]", event.getServletContext().getContextPath());
		if (HibernateSessionHelper.getSessionFactory() == null) { // Just call the static initializer of that class
			throw new RuntimeException("HibernateSessionHelper could not build SessionFactory, see error before");
		}
		LOG.info("Initializing Hibernate Listener in context [{}] finished", event.getServletContext().getContextPath());
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		LOG.info("Destroying Hibernate Listener in context [{}]", event.getServletContext().getContextPath());

		SessionFactory sf = HibernateSessionHelper.getSessionFactory();
		if (sf != null) {
			sf.close(); // Free all resources
			try {
				Thread.sleep(1500); // C3P0 works highly asynchronous; even closing all threads is done asynchronously; this is not nice, but there does not seem to be any better solution ATM
			} catch (InterruptedException e) {
			}
		}

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() != cl) {
				LOG.debug("Not deregistering JDBC driver \"{}\" as it does not belong to this webapp's ClassLoader", driver.toString());
				continue;
			}

			try {
				LOG.debug("Deregistering JDBC driver \"{}\"", driver.toString());
				DriverManager.deregisterDriver(driver);
			} catch (SQLException ex) {
				LOG.error("Error deregistering JDBC driver \"{}\"", driver.toString());
			}
		}
		LOG.info("Destroying Hibernate Listener in context [{}] finished", event.getServletContext().getContextPath());
	}
}
