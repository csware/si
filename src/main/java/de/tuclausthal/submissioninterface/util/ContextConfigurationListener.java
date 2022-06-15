/*
 * Copyright 2013, 2020-2022 Sven Strickroth <email@cs-ware.de>
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
import java.util.Set;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.GATEView;

/**
 * Context Configuration
 * @author Sven Strickroth
 */
public class ContextConfigurationListener implements ServletContextListener {
	final private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void contextInitialized(ServletContextEvent event) {
		LOG.info("Initializing Configuration in context [{}]", event.getServletContext().getContextPath());

		Configuration.fillConfiguration(event.getServletContext());

		// configure AuthenticationFilter
		FilterRegistration authenticationFilter = event.getServletContext().getFilterRegistration("AuthenticationFilter");
		authenticationFilter.addMappingForUrlPatterns(null, false, Configuration.SERVLETS_PATH_WITH_BOTHSLASHES + "*");

		// register Views
		Set<Class<?>> viewServlets = new Reflections("de.tuclausthal.submissioninterface.servlets.view").getTypesAnnotatedWith(GATEView.class);
		for (Class<?> servlet : viewServlets) {
			if (!servlet.getSuperclass().equals(HttpServlet.class)) {
				throw new RuntimeException("Class " + servlet.getCanonicalName() + " does not extend HttpServlet");
			}
			event.getServletContext().addServlet(servlet.getSimpleName(), servlet.getCanonicalName());
		}

		// register Controllers
		Set<Class<?>> controllerServlets = new Reflections("de.tuclausthal.submissioninterface.servlets.controller").getTypesAnnotatedWith(GATEController.class);
		for (Class<?> servlet : controllerServlets) {
			if (!servlet.getSuperclass().equals(HttpServlet.class)) {
				throw new RuntimeException("Class " + servlet.getCanonicalName() + " does not extend HttpServlet");
			}
			ServletRegistration.Dynamic registration = event.getServletContext().addServlet(servlet.getSimpleName(), servlet.getCanonicalName());
			if (servlet.getAnnotation(GATEController.class).recursive()) {
				registration.addMapping(Configuration.SERVLETS_PATH_WITH_BOTHSLASHES + servlet.getSimpleName() + "/*");
			} else {
				registration.addMapping(Configuration.SERVLETS_PATH_WITH_BOTHSLASHES + servlet.getSimpleName());
			}
		}

		LOG.info("Initializing Configuration in context [{}] finished", event.getServletContext().getContextPath());
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		LOG.info("Destroying Configuration in context [{}]", event.getServletContext().getContextPath());
		LOG.info("Destroying Configuration in context [{}] finished", event.getServletContext().getContextPath());
	}
}
