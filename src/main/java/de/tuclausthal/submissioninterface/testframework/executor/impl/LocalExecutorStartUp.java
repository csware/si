/*
 * Copyright 2009 - 2010, 2015 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.executor.impl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import de.tuclausthal.submissioninterface.util.Configuration;

/**
 * Startup-wrapper for initiating the LocalExecuter.
 * @author Sven Strickroth
 */
public class LocalExecutorStartUp implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		LocalExecutor.CORES = Configuration.getInstance().getTestframeworkCores();
		LocalExecutor.dataPath = Configuration.getInstance().getDataPath();
		LocalExecutor.getInstance();
	}

	@Override
	synchronized public void contextDestroyed(ServletContextEvent arg0) {
		LocalExecutor.getInstance().shutdown();
	}
}
