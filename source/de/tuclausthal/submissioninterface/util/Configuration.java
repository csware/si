/*
 * Copyright 2009 - 2010, 2013 Sven Strickroth <email@cs-ware.de>
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

import java.io.File;

import javax.servlet.ServletContext;

/**
 * Context Configuration
 * @author Sven Strickroth
 */
public class Configuration {
	static private Configuration instance = null;
	private String dataPath;
	private String servletsPath;
	private String adminMail;
	private String mailServer;
	private String mailFrom;
	private String mailSubjectPrefix;

	private Configuration() {}

	public static Configuration getInstance() {
		return instance;
	}

	static void fillConfiguration(ServletContext context) {
		if (instance == null) {
			instance = new Configuration();
		}

		instance.adminMail = context.getInitParameter("adminmail");
		instance.mailServer = context.getInitParameter("mail-server");
		instance.mailFrom = context.getInitParameter("mail-from");
		instance.mailSubjectPrefix = context.getInitParameter("mail-subject-prefix");

		instance.fillDatapath(context);
		instance.fillServletspath(context);
	}

	/**
	 * Returns the path to the submissions
	 * @return the path
	 */
	public File getDataPath() {
		return new File(dataPath);
	}

	private void fillDatapath(ServletContext context) {
		dataPath = context.getInitParameter("datapath");
		if (dataPath == null) {
			throw new RuntimeException("datapath not specified");
		}
		File path = new File(dataPath);
		if (path.isFile()) {
			throw new RuntimeException("datapath must not be a file");
		}
		if (path.exists() == false) {
			if (!path.mkdirs()) {
				throw new RuntimeException("could not create datapath");
			}
		}
	}

	/**
	 * Returns the path to the servlets
	 * @return the path
	 */
	public String getServletsPath() {
		return servletsPath;
	}

	private void fillServletspath(ServletContext context) {
		servletsPath = context.getInitParameter("servletspath");
		if (servletsPath == null) {
			throw new RuntimeException("servletspath not specified");
		}
	}

	/**
	 * Returns the path to the servlets
	 * @return the path
	 */
	public String getAdminMail() {
		return adminMail;
	}

	public String getMailServer() {
		return mailServer;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public String getMailSubjectPrefix() {
		return mailSubjectPrefix;
	}
}
