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
	public void init(ServletConfig config) {
		LocalExecutor.CORES = Util.parseInteger(config.getInitParameter("cores"), 2);
		LocalExecutor.dataPath = new ContextAdapter(config.getServletContext()).getDataPath();
		LocalExecutor.getInstance();
	}
}
