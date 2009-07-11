package de.tuclausthal.submissioninterface.executiontask.executer.impl;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;

import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Startup-wrapper for initiating the LocalExecuter.
 * @author Sven Strickroth
 */
public class LocalExecuterStartUp extends HttpServlet {
	public void init(ServletConfig config) {
		LocalExecuter.CORES = Util.parseInteger(config.getInitParameter("cores"), 2);
		LocalExecuter.dataPath = new ContextAdapter(config.getServletContext()).getDataPath();
		LocalExecuter.getInstance();
	}
}
