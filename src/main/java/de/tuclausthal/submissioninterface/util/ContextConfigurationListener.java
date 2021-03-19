/*
 * Copyright 2013, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;

import org.reflections.Reflections;

import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.GATEView;

/**
 * Context Configuration
 * @author Sven Strickroth
 */
public class ContextConfigurationListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent event) {
		Configuration.fillConfiguration(event.getServletContext());

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
				registration.addMapping("/servlets/" + servlet.getSimpleName() + "/*");
			} else {
				registration.addMapping("/servlets/" + servlet.getSimpleName());
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {}
}
