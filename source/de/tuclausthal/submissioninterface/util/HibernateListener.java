package de.tuclausthal.submissioninterface.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class HibernateListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		HibernateSessionHelper.getSessionFactory(); // Just call the static initializer of that class    
	}

	public void contextDestroyed(ServletContextEvent event) {
		HibernateSessionHelper.getSessionFactory().close(); // Free all resources
	}
}
