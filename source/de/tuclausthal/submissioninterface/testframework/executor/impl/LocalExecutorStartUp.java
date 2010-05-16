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

package de.tuclausthal.submissioninterface.testframework.executor.impl;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;

import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Startup-wrapper for initiating the LocalExecuter.
 * @author Sven Strickroth
 */
public class LocalExecutorStartUp extends HttpServlet {
	@Override
	public void init(ServletConfig config) {
		LocalExecutor.CORES = Util.parseInteger(config.getInitParameter("cores"), 2);
		LocalExecutor.dataPath = new ContextAdapter(config.getServletContext()).getDataPath();
		LocalExecutor.getInstance();
	}
}
