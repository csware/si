/*
 * Copyright 2009-2010, 2013, 2015, 2017, 2020-2025 Sven Strickroth <email@cs-ware.de>
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.template.Template;

/**
 * Context Configuration
 * @author Sven Strickroth
 */
public class Configuration {
	final public static String GLOBAL_FILENAME_CHARS = "0-9,._`´#§$%^&() \\p{Sc}\\p{No}[\\p{Sm}\\p{So}&&[^<>:|]]-";
	final public static String GLOBAL_ARCHIVEFILENAME_CHARS = "/" + GLOBAL_FILENAME_CHARS;
	final public static String GLOBAL_FILENAME_REGEXP = "^((?:[\\p{L}][\\p{Mn}]*+|[" + GLOBAL_FILENAME_CHARS + "])++)$";
	final public static String GLOBAL_ARCHIVEFILENAME_REGEXP = "^((?:[\\p{L}][\\p{Mn}]*+|[" + GLOBAL_ARCHIVEFILENAME_CHARS + "])++)$";

	final public static String SERVLETS_PATH_WITH_ENDSLASH = "servlets/";
	final public static String SERVLETS_PATH_WITH_BOTHSLASHES = "/" + SERVLETS_PATH_WITH_ENDSLASH;

	final public static int MAX_UPLOAD_SIZE = 100 * 1024 * 1024;
	public static String VERSION_INFO = "unknown";

	static private Configuration instance = null;
	private Constructor<Template> templateConstructor;
	private Path dataPath;
	private Path lecturesPath;
	private String serverName;
	private String fullServerURI;
	private String adminMail;
	private String mailServer;
	private String mailFrom;
	private String mailSubjectPrefix;
	private boolean matrikelNoAvailable;
	private boolean matrikelNoAvailableToTutors;
	private boolean matrikelNumberMustBeEnteredManuallyIfMissing;
	private boolean mailLastGradingTutorOnGradeChange;
	private int testFrameworkCores;
	private ArrayList<String> intranetPrefixes = new ArrayList<>();
	private Charset defaultZipFileCharset;
	private List<String> studiengaenge;

	static {
		Properties versionProperties = new Properties();
		try (var propertiesFile = Configuration.class.getClassLoader().getResourceAsStream("git.properties")) {
			versionProperties.load(propertiesFile);
			VERSION_INFO = versionProperties.getProperty("git.commit.id.abbrev") + " (" + versionProperties.getProperty("git.build.time") + ")";
		} catch (IOException | NullPointerException ex) {
			VERSION_INFO = "unknown";
		}
	}

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
		if (!instance.mailSubjectPrefix.isBlank() && !instance.mailSubjectPrefix.endsWith(" ")) {
			instance.mailSubjectPrefix = instance.mailSubjectPrefix + " ";
		}
		instance.matrikelNoAvailable = parseBooleanValue(context.getInitParameter("matrikelno-available"), false);
		instance.matrikelNoAvailableToTutors = instance.matrikelNoAvailable && parseBooleanValue(context.getInitParameter("show-matrikelno-to-tutors"), false);
		instance.matrikelNumberMustBeEnteredManuallyIfMissing = instance.matrikelNoAvailable && parseBooleanValue(context.getInitParameter("matrikelno-must-be-enterend-manually-if-missing"), false);
		instance.testFrameworkCores = Util.parseInteger(context.getInitParameter("testframework-cores"), 2);
		instance.mailLastGradingTutorOnGradeChange = parseBooleanValue(context.getInitParameter("mail-last-grading-tutor-on-grade-change"), true);
		instance.defaultZipFileCharset = Charset.forName(context.getInitParameter("default-zipfile-charset"));

		if (context.getInitParameter("intranetPrefixes") != null) {
			for (String prefix : context.getInitParameter("intranetPrefixes").split(";")) {
				instance.intranetPrefixes.add(prefix);
			}
		}

		instance.fillDatapath(context);
		instance.fillServerURI(context);
		instance.fillTemplateConstructor(context);
		instance.fillStudiengaenge(context);
	}

	private void fillStudiengaenge(ServletContext context) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResourceAsStream("WEB-INF/studiengaenge.txt")))) {
			studiengaenge = reader.lines().filter(studiengang -> !studiengang.isBlank() && studiengang.charAt(0) != '#').collect(Collectors.toUnmodifiableList());
		} catch (IOException e) {
			throw new RuntimeException("No WEB-INF/studiengaenge.txt file found!", e);
		}
	}

	@SuppressWarnings("unchecked")
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
			throw new RuntimeException("Checking template-class threw security exception");
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
	 * Returns the path to the root data directory
	 * @return the path
	 */
	public Path getDataPath() {
		return dataPath;
	}

	/**
	 * Returns the path to the root of the lectures
	 * @return the path
	 */
	public Path getLecturesPath() {
		return lecturesPath;
	}
	
	private void fillDatapath(ServletContext context) {
		String dataPathValue = context.getInitParameter("datapath");
		if (dataPathValue == null) {
			throw new RuntimeException("datapath not specified");
		}
		dataPath = Path.of(dataPathValue).toAbsolutePath();
		if (Files.isRegularFile(dataPath)) {
			throw new RuntimeException("datapath must not be a file");
		}
		try {
			Util.ensurePathExists(dataPath);
		} catch (IOException e) {
			throw new RuntimeException("could not create datapath", e);
		}
		lecturesPath = dataPath.resolve("lectures");
		if (Files.isRegularFile(lecturesPath)) {
			throw new RuntimeException("lecturespath must not be a file");
		}
		try {
			Util.ensurePathExists(dataPath);
		} catch (IOException e) {
			throw new RuntimeException("could not create lecturespath", e);
		}
	}

	private void fillServerURI(ServletContext context) {
		serverName = context.getInitParameter("servername");
		if (serverName == null) {
			throw new RuntimeException("servername not specified");
		}
		fullServerURI = "https://" + serverName + context.getContextPath() + SERVLETS_PATH_WITH_BOTHSLASHES;
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

	public boolean isMatrikelNoAvailable() {
		return matrikelNoAvailable;
	}

	public boolean isMailLastGradingTutorOnGradeChange() {
		return mailLastGradingTutorOnGradeChange;
	}

	/**
	 * @return the intranetPrefixes
	 */
	public ArrayList<String> getIntranetPrefixes() {
		return intranetPrefixes;
	}

	public Charset getDefaultZipFileCharset() {
		return defaultZipFileCharset;
	}

	public List<String> getStudiengaenge() {
		return studiengaenge;
	}
}
