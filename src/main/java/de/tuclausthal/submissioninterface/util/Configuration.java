/*
 * Copyright 2009-2010, 2013, 2015, 2017, 2020 Sven Strickroth <email@cs-ware.de>
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
import java.lang.reflect.Constructor;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.template.Template;

/**
 * Context Configuration
 * @author Sven Strickroth
 */
public class Configuration {
	static private Configuration instance = null;
	private Constructor<Template> templateConstructor;
	private String dataPath;
	private String serverName;
	private String fullServerURI;
	private String servletsPath;
	private String adminMail;
	private String mailServer;
	private String mailFrom;
	private String mailSubjectPrefix;
	private String mailDomain;
	private boolean matrikelNoAvailableToTutors;
	private boolean matrikelNumberMustBeEnteredManuallyIfMissing;
	private boolean mailLastGradingTutorOnGradeChange;
	private int testFrameworkCores;

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
		instance.mailDomain = context.getInitParameter("mail-users-domain");
		instance.matrikelNoAvailableToTutors = parseBooleanValue(context.getInitParameter("show-matrikelno-to-tutors"), false);
		instance.matrikelNumberMustBeEnteredManuallyIfMissing = parseBooleanValue(context.getInitParameter("matrikelno-must-be-enterend-manually-if-missing"), false);
		instance.testFrameworkCores = Util.parseInteger(context.getInitParameter("testframework-cores"), 2);
		instance.mailLastGradingTutorOnGradeChange = parseBooleanValue(context.getInitParameter("mail-last-grading-tutor-on-grade-change"), true);

		instance.fillDatapath(context);
		instance.fillServletspath(context);
		instance.fillTemplateConstructor(context);
	}

	private void fillTemplateConstructor(ServletContext context) {
		String className = context.getInitParameter("template-class");
		if (className == null) {
			throw new RuntimeException("template-class not specified");
		}
		try {
			templateConstructor = (Constructor<Template>) Class.forName(className).getDeclaredConstructor(HttpServletRequest.class, HttpServletResponse.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("template-class not found");
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("template-class does not have required constructor");
		} catch (SecurityException e) {
			throw new RuntimeException("Cehcking template-class threw security exception");
		}
	}

	private static boolean parseBooleanValue(String initParameter, boolean defaultValue) {
		if (initParameter == null)
			return defaultValue;
		String normalized = initParameter.trim().toLowerCase();
		if ("yes".equals(normalized) || "true".equals(normalized)) {
			return true;
		} else if ("no".equals(normalized) || "false".equals(normalized)) {
			return false;
		}
		if (Integer.parseInt(normalized) == 0) {
			return false;
		}
		return true;
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
		serverName = context.getInitParameter("servername");
		if (serverName == null) {
			throw new RuntimeException("servername not specified");
		}
		fullServerURI = "https://" + serverName + context.getContextPath() + "/" + getServletsPath();
	}

	/**
	 * Returns the path to the servlets
	 * @return the path
	 */
	public String getAdminMail() {
		return adminMail;
	}

	public boolean isMatrikelNoAvailableToTutors() {
		return matrikelNoAvailableToTutors;
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

	public String getMailDomain() {
		return mailDomain;
	}

	public Constructor<Template> getTemplateConstructor() {
		return templateConstructor;
	}

	public boolean isMatrikelNumberMustBeEnteredManuallyIfMissing() {
		return matrikelNumberMustBeEnteredManuallyIfMissing;
	}

	public int getTestframeworkCores() {
		return testFrameworkCores;
	}

	/*
	 * Returns the absolute URI up to the servlets path
	 * @return absolute URI up to the servlets path
	 */
	public String getFullServletsURI() {
		return fullServerURI;
	}

	public boolean isMailLastGradingTutorOnGradeChange() {
		return mailLastGradingTutorOnGradeChange;
	}
}
