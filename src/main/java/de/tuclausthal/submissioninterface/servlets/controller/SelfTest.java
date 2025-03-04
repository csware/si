/*
 * Copyright 2021-2025 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.controller;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParsingException;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletException;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.dupecheck.jplag.JPlagAdapter;
import de.tuclausthal.submissioninterface.dupecheck.plaggie.PlaggieAdapter;
import de.tuclausthal.submissioninterface.persistence.datamodel.DockerTestStep;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.JavaAdvancedIOTestStep;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.servlets.view.SelfTestView;
import de.tuclausthal.submissioninterface.testframework.executor.TestExecutorTestResult;
import de.tuclausthal.submissioninterface.testframework.executor.impl.LocalExecutor;
import de.tuclausthal.submissioninterface.testframework.tests.impl.DockerTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.JavaFunctionTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.JavaJUnitTest;
import de.tuclausthal.submissioninterface.testframework.tests.impl.JavaSyntaxTest;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.MailSender;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller for a short Self-Test
 * @author Sven Strickroth
 */
@GATEController
public class SelfTest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final static private Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if (RequestAdapter.getUser(request) == null) {
			LOG.error("SelfTest was accessed without proper authentication. AuthenticationFilter does not seem to be working correctly!");
			request.setAttribute("title", "Selbsttest");
			request.setAttribute("message", "<div class=red>Selbsttest OHNE Authentifizierung erreichbar!</div>");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}
		if (!RequestAdapter.getUser(request).isSuperUser()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		List<TestResult> testresults = new ArrayList<>();
		try {
			Properties versionProperties = new Properties();
			versionProperties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));
			String versionInfo = versionProperties.getProperty("git.commit.id.abbrev");
			versionInfo += " (" + versionProperties.getProperty("git.build.time");
			if (versionProperties.getProperty("git.build.number") != null) {
				versionInfo += "; CI-Build-ID: " + versionProperties.getProperty("git.build.number");
			}
			versionInfo += ")";
			testresults.add(new TestResult("Selbsttest ausgeführt mit GATE-Version:", Util.escapeHTML(versionInfo), null));
		} catch (IOException | NullPointerException ex) {
		}
		testresults.add(new TestResult("Selbsttest ausgeführt am (Zeitzone muss auf allen Systemen identisch sein):", Util.escapeHTML(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z").format(ZonedDateTime.now())), null));
		try {
			testresults.add(new TestResult("Selbsttest ausgeführt auf:", Util.escapeHTML(InetAddress.getLocalHost().getHostName()), null));
		} catch (UnknownHostException e) {
		}
		testresults.add(new TestResult("Betriebsystem des Servlet-Containers:", System.getProperty("os.name", "-") + " version " + System.getProperty("os.version", "-") + " running on " + System.getProperty("os.arch", "-") + "; " + System.getProperty("file.encoding", "-") + "; " + System.getProperty("user.country", "-"), null));
		testresults.add(new TestResult("Server-Software:", Util.escapeHTML(getServletContext().getServerInfo()), null));
		testresults.add(new TestResult("Java-Version des Servlet-Containers:", Util.escapeHTML(System.getProperty("java.runtime.name", "-") + " " + System.getProperty("java.runtime.version", "-") + " (" + System.getProperty("java.version", "-") + "; " + System.getProperty("java.vm.name", "-") + " " + System.getProperty("java.vm.version", "-") + "; " + System.getProperty("java.vendor", "-") + ")"), null));

		try {
			Session session = RequestAdapter.getSession(request);
			testresults.add(new TestResult("Datenbank-Software:", Util.escapeHTML(session.createNativeQuery("SELECT VERSION();", String.class).uniqueResult()), null));
		} catch (PersistenceException e) {
			testresults.add(new TestResult("Datenbank-Software:", Util.escapeHTML(e.getMessage()), false));
			LOG.error("Could not get version from MySQL compliant server.", e);
		}

		try {
			Session session = RequestAdapter.getSession(request);
			testresults.add(new TestResult("Datenbank-Zeitzone (sollte Servletcontainerzeitzone entsprechen, siehe oben):", Util.escapeHTML(session.createNativeQuery("SELECT IF(@@time_zone='SYSTEM',@@system_time_zone,@@time_zone);", String.class).uniqueResult()), null));
		} catch (PersistenceException e) {
			testresults.add(new TestResult("Datenbank-Zeitzone (sollte Servletcontainerzeitzone entsprechen, siehe oben):", Util.escapeHTML(e.getMessage()), false));
			LOG.error("Could not get timezone from MySQL compliant server.", e);
		}

		FilterRegistration filter = getServletContext().getFilterRegistration("AuthenticationFilter");
		testresults.add(new TestResult("AuthenticationFilter ist installiert.", filter != null && request.getAttribute("username") != null));
		if (filter != null) {
			testresults.add(new TestResult("AuthenticationFilter ist nicht FakeVerify (kritisch für Produktivbetrieb).", Util.escapeHTML(filter.getInitParameter("verify")), filter.getInitParameter("verify") != null && !filter.getInitParameter("verify").endsWith("FakeVerify")));
		}
		String guessedServerAddress = request.getScheme() + "://" + request.getServerName();
		if (("https".equals(request.getScheme()) && request.getLocalPort() != 443) || ("http".equals(request.getScheme()) && request.getLocalPort() != 80)) {
			guessedServerAddress += ":" + request.getLocalPort();
		}
		guessedServerAddress += getServletContext().getContextPath();
		testresults.add(new TestResult("Ermittelte genutzte Server-Adresse:", Util.escapeHTML(guessedServerAddress), null));
		testresults.add(new TestResult("Servername ist in web.xml gesetzt und sieht gültig aus (eigentlich nur wichtig für Links in Mails).", Util.escapeHTML(Configuration.getInstance().getFullServletsURI()), Configuration.getInstance().getFullServletsURI().startsWith(guessedServerAddress)));
		testresults.add(new TestResult("Session-Tracking Mode ist Cookie.", Util.escapeHTML(getServletContext().getEffectiveSessionTrackingModes().toString()), getServletContext().getEffectiveSessionTrackingModes().size() == 1 && getServletContext().getEffectiveSessionTrackingModes().contains(SessionTrackingMode.COOKIE)));
		testresults.add(new TestResult("Admin-E-Mail-Adresse ist in web.xml gesetzt.", Util.escapeHTML(Configuration.getInstance().getAdminMail()), !Configuration.getInstance().getAdminMail().isBlank() && !"admin@localhost".equals(Configuration.getInstance().getAdminMail())));
		testresults.add(new TestResult("Mail-Server ist in web.xml gesetzt.", Util.escapeHTML(Configuration.getInstance().getMailServer()), !Configuration.getInstance().getMailServer().isBlank()));
		testresults.add(new TestResult("Absender-Adresse ist in web.xml gesetzt.", Util.escapeHTML(Configuration.getInstance().getMailFrom()), !Configuration.getInstance().getMailFrom().isBlank() && !"noreply@localhost".equals(Configuration.getInstance().getMailFrom())));
		if (MailSender.sendMail(Configuration.getInstance().getAdminMail(), "Test-Mail", "Test-Inhalt.", MailSender.newMailOptions().enableAutoSubmittedAutoGenerated())) {
			testresults.add(new TestResult("Test-Mail wurde an Admin-E-Mail-Adresse gesendet. Empfang manuell prüfen!", null));
		} else {
			testresults.add(new TestResult("Test-Mail wurde an Admin-E-Mail-Adresse gesendet (Logs prüfen).", false));
		}
		try {
			Field field = LocalExecutor.class.getDeclaredField("instance");
			field.setAccessible(true);
			testresults.add(new TestResult("Testframework-Listener läuft.", field.get(null) != null && LocalExecutor.getInstance().isRunning()));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			testresults.add(new TestResult("Testframework-Listener läuft.", false));
			LOG.error("Could not access LocalExecutor.", e);
		}
		testresults.add(new TestResult("TMP-Verzeichnis ist beschreibbar.", Util.escapeHTML(Configuration.getInstance().getLecturesPath().toString()), checkTemp()));
		testresults.add(new TestResult("Dateien mit Umlauten können erstellt werden.", Util.escapeHTML(Configuration.getInstance().getLecturesPath().toString()), checkSpecialFilename()));
		testresults.add(new TestResult("Daten-Verzeichnis existiert und ist lesbar.", Util.escapeHTML(Configuration.getInstance().getDataPath().toString()), checkDataDir()));
		testresults.add(new TestResult("Daten-Verzeichnis ist nicht beschreibbar (erhöht die Sicherheit).", !Files.isWritable(Configuration.getInstance().getDataPath())));
		testresults.add(new TestResult("Parent vom Daten-Verzeichnis ist nicht beschreibbar (erhöht die Sicherheit).", !Files.isWritable(Configuration.getInstance().getDataPath().getParent())));
		testresults.add(new TestResult("Lectures-Verzeichnis existiert, ist les- und beschreibbar.", Util.escapeHTML(Configuration.getInstance().getLecturesPath().toString()), checkLecturesDir()));
		checkRequiredFilesInDataDir(testresults);
		if (Files.isRegularFile(Configuration.getInstance().getDataPath().resolve(JPlagAdapter.JPLAG_JAR))) {
			testresults.add(new TestResult("\"" + JPlagAdapter.JPLAG_JAR + "\" ist im Daten-Verzeichnis installiert: JPlag prinzipiell verfügbar.", null));
		} else {
			testresults.add(new TestResult("\"" + JPlagAdapter.JPLAG_JAR + "\" ist nicht im Daten-Verzeichnis installiert: JPlag nicht verfügbar.", null));
		}

		testJavaTests(testresults);
		if (Files.isRegularFile(Path.of(DockerTest.SAFE_DOCKER_SCRIPT))) {
			testresults.add(new TestResult("\"" + DockerTest.SAFE_DOCKER_SCRIPT + "\" Skript ist installiert: DockerIOTests prinzipiell verfügbar.", null));
			final Path tempDir = Util.createTemporaryDirectory("emptydir");
			try {
				de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest dockerTestSpec = new de.tuclausthal.submissioninterface.persistence.datamodel.DockerTest();
				dockerTestSpec.setPreparationShellCode("");
				dockerTestSpec.setTestSteps(new ArrayList<>());
				dockerTestSpec.getTestSteps().add(new DockerTestStep(dockerTestSpec, "HelloWorld", "echo \"Hello World\"", "Hello World"));
				DockerTest dockerTest = new DockerTest(dockerTestSpec);
				TestExecutorTestResult result = new TestExecutorTestResult();
				dockerTest.performTest(Configuration.getInstance().getDataPath(), tempDir, result);
				testresults.add(new TestResult("DockerIOTest erfolgreich.", Util.escapeHTML(result.getTestOutput()), result.isTestPassed()));

				dockerTestSpec.setTestSteps(new ArrayList<>());
				dockerTestSpec.getTestSteps().add(new DockerTestStep(dockerTestSpec, "HelloWorld", "echo \"Hello old World\"", "Hello World"));
				dockerTest.performTest(Configuration.getInstance().getDataPath(), tempDir, result);
				testresults.add(new TestResult("DockerIOTest erfolgreich Fehler erkannt.", Util.escapeHTML(result.getTestOutput()), !result.isTestPassed()));
			} catch (Exception e) {
				testresults.add(new TestResult("DockerIOTest erfolgreich.", Util.escapeHTML(e.getMessage()), false));
				LOG.error("DockerIOTest failed.", e);
			}
			Util.recursiveDelete(tempDir);
		} else {
			testresults.add(new TestResult("\"" + DockerTest.SAFE_DOCKER_SCRIPT + "\" Skript ist nicht installiert: DockerIOTests nicht verfügbar (grundsätzlich nur verfügbar für Linux).", null));
		}

		request.setAttribute("testresults", testresults);
		getServletContext().getNamedDispatcher(SelfTestView.class.getSimpleName()).forward(request, response);
	}

	private static boolean checkDataDir() {
		final Path dataDir = Configuration.getInstance().getDataPath();
		if (!Files.isDirectory(dataDir) || !Files.isReadable(dataDir)) {
			return false;
		}
		try (Stream<Path> fileStream = Files.list(dataDir)) {
			fileStream.count();
		} catch (SecurityException | IOException e) {
			LOG.error("Cannot read data-path,", e);
			return false;
		}
		return true;
	}

	private static boolean checkTemp() {
		try {
			final Path testFile = Files.createTempFile("sometext", ".tmp");
			Files.delete(testFile);
		} catch (IOException e) {
			LOG.error("Cannot create tempfile", e);
			return false;
		}
		final Path tmpPath = Util.createTemporaryDirectory("test");
		if (tmpPath == null) {
			LOG.error("Could not create temporary directory");
			return false;
		}
		if (Files.notExists(tmpPath)) {
			LOG.error("Created directory does not exist");
			return false;
		}
		Util.recursiveDelete(tmpPath);
		return true;
	}

	private static boolean checkSpecialFilename() {
		try {
			final Path testFile = Files.createTempFile("ÜÜmlaut", ".tmp");
			Files.delete(testFile);
		} catch (IOException | InvalidPathException e) {
			LOG.error("Cannot create filenames containing umlauts, (InvalidPathException: LANG=*.UTF8 or LANG=C.UTF8 set?)", e);
			return false;
		}
		return true;
	}

	private static boolean checkLecturesDir() {
		final Path lecturesDir = Configuration.getInstance().getLecturesPath();
		if (!Files.isDirectory(lecturesDir) || !Files.isReadable(lecturesDir) || !Files.isWritable(lecturesDir)) {
			return false;
		}
		try (Stream<Path> fileStream = Files.list(lecturesDir)) {
			fileStream.count();
		} catch (SecurityException | IOException e) {
			LOG.error("Cannot read lecture-path", e);
			return false;
		}
		try {
			final Path testFile = Files.createFile(lecturesDir.resolve("sometest"));
			Files.delete(testFile);
		} catch (IOException e) {
			LOG.error("Cannot write lecture-path", e);
			return false;
		}
		return true;
	}

	private static void checkRequiredFilesInDataDir(List<TestResult> testresults) {
		String[] filesToCheck = { JavaFunctionTest.SECURITYMANAGER_JAR, JavaJUnitTest.JUNIT_JAR, PlaggieAdapter.CONFIG_FILE_NAME };
		for (String filename : filesToCheck) {
			final Path file = Configuration.getInstance().getDataPath().resolve(filename);
			testresults.add(new TestResult("\"" + filename + "\" ist im Daten-Verzeichnis vorhanden und lesbar.", Files.isRegularFile(file) && Files.isReadable(file)));
		}
	}

	private static void testJavaTests(List<TestResult> testresults) {
		final Path tempDir = Util.createTemporaryDirectory("javatests");
		if (tempDir == null) {
			testresults.add(new TestResult("Java-Tests erfolgreich.", "Konnte kein temporäres Verzeichnis erstellen.", false));
			LOG.error("Failed to create tempdir!");
			return;
		}

		try (Writer fw = Files.newBufferedWriter(tempDir.resolve("HelloWorld.java"))) {
			fw.write("public class HelloWorld {\n	public static void main(String[] args) {\n		System.out.println(\"Hello World!\");\n	}\n}\n");
		} catch (IOException e) {
			testresults.add(new TestResult("Java-Tests erfolgreich.", "Konnte keine Datei im temporären Verzeichnis (" + Util.escapeHTML(tempDir.toString()) + ") erstellen.", false));
			LOG.error("Could not write to tempDir.", e);
			Util.recursiveDelete(tempDir);
			return;
		}

		try {
			TestExecutorTestResult result = new TestExecutorTestResult();
			boolean compileResult = JavaSyntaxTest.compileJava(tempDir, null, tempDir, result);
			testresults.add(new TestResult("Java-Syntax-Test erfolgreich.", Util.escapeHTML(result.getTestOutput()), compileResult && compileResult == result.isTestPassed()));
		} catch (Exception e) {
			testresults.add(new TestResult("Java-Syntax-Test erfolgreich.", Util.escapeHTML(e.getMessage()), false));
			LOG.error("Java syntax text failed.", e);
			Util.recursiveDelete(tempDir);
			return;
		}

		try {
			try (Writer fw = Files.newBufferedWriter(tempDir.resolve("HelloWorld.java"))) {
				fw.write("public class HelloWorld {\n	public static void main(String[] args) {\n		System.out.println(\"Hello World!);\n	 }\n}\n");
			}

			TestExecutorTestResult result = new TestExecutorTestResult();
			boolean compileResult = JavaSyntaxTest.compileJava(tempDir, null, tempDir, result);
			testresults.add(new TestResult("Java-Syntaxtest erkennt syntaktisch falsche Lösung (\"HelloWorld.java:3: error: unclosed string literal\").", Util.escapeHTML(result.getTestOutput()), !compileResult && compileResult == result.isTestPassed() && result.getTestOutput().contains("HelloWorld.java:3: error: unclosed string literal")));
			Files.delete(tempDir.resolve("HelloWorld.java"));
		} catch (Exception e) {
			testresults.add(new TestResult("Java-Syntaxtest erkennt syntaktisch falsche Lösung.", Util.escapeHTML(e.getMessage()), false));
			LOG.error("Java syntax text failed for syntactically incorrect solution.", e);
			Util.recursiveDelete(tempDir);
			return;
		}

		try {
			try (Writer fw = Files.newBufferedWriter(tempDir.resolve("Triangle.java"))) {
				fw.write("public class Triangle {\r\n" + "	static void drawTriangle(int sizeOfTriangle) {\r\n" + "		for (int i = 0; i < sizeOfTriangle; i++) {\r\n" + "			for (int j = 0; j <= i; j++) {\r\n" + "				System.out.print(\"*\");\r\n" + "			}\r\n" + "			System.out.println();\r\n" + "		}\r\n" + "	}\r\n" + "}");
			}

			JavaAdvancedIOTest javaAdvancedIOTest = new JavaAdvancedIOTest();
			javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Empty", "Triangle.drawTriangle(0);", ""));
			javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "One star", "Triangle.drawTriangle(1);", "*"));
			javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Two stars", "Triangle.drawTriangle(2);", "*\n**"));
			javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Four stars", "Triangle.drawTriangle(4);", "*\n**\n***\n****"));

			de.tuclausthal.submissioninterface.testframework.tests.impl.JavaAdvancedIOTest javaFunctionTest = new de.tuclausthal.submissioninterface.testframework.tests.impl.JavaAdvancedIOTest(javaAdvancedIOTest);
			TestExecutorTestResult result = new TestExecutorTestResult();
			javaFunctionTest.performTest(Configuration.getInstance().getDataPath(), tempDir, result);
			testresults.add(new TestResult("JavaAdvancedIOTest erfolgreich.", Util.escapeHTML(result.getTestOutput()), result.isTestPassed()));
			final boolean functionTestGenerallyOK = result.isTestPassed();

			javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Four stars", "Triangle.drawTriangle(4);", "something else"));
			result = new TestExecutorTestResult();
			javaFunctionTest.performTest(Configuration.getInstance().getDataPath(), tempDir, result);
			testresults.add(new TestResult("JavaAdvancedIOTest erkennt erfolgreich Fehler.", Util.escapeHTML(result.getTestOutput()), !result.isTestPassed()));

			javaAdvancedIOTest.setTimeout(2);
			try (Writer fw = Files.newBufferedWriter(tempDir.resolve("Triangle.java"))) {
				fw.write("public class Triangle {\n	static void drawTriangle(int sizeOfTriangle) {\n		while (true) {\n			try {\n				Thread.sleep(1000);\n			} catch (InterruptedException e) {\n				System.out.println(\"Ignore InterruptedException\");\n			}\n		}\n	}\n}\n");
			}
			javaFunctionTest.performTest(Configuration.getInstance().getDataPath(), tempDir, result);
			JsonObject object = null;
			try (JsonReader jsonReader = Json.createReader(new StringReader(result.getTestOutput()))) {
				object = jsonReader.readObject();
				testresults.add(new TestResult("JavaAdvancedIOTest erkennt erfolgreich Timeout.", Util.escapeHTML(result.getTestOutput()), !result.isTestPassed() && object.containsKey("time-exceeded") && object.getBoolean("time-exceeded")));
			} catch (JsonParsingException ex) {
				testresults.add(new TestResult("JavaAdvancedIOTest-Ausgabe ist kein gültiges JSON.", Util.escapeHTML(result.getTestOutput()), false));
			}

			if (functionTestGenerallyOK) {
				javaAdvancedIOTest.getTestSteps().clear();
				javaAdvancedIOTest.getTestSteps().add(new JavaAdvancedIOTestStep(javaAdvancedIOTest, "Empty", "Triangle.drawTriangle(0);", ""));
				try (Writer fw = Files.newBufferedWriter(tempDir.resolve("Triangle.java"))) {
					fw.write("public class Triangle {\n	static void drawTriangle(int sizeOfTriangle) {\n		System.out.println(\"----start here----\\n\" + System.getProperty(\"java.runtime.name\", \"-\") + \" \" + System.getProperty(\"java.runtime.version\", \"-\") + \" (\" + System.getProperty(\"java.version\", \"-\") + \"; \" + System.getProperty(\"java.vm.name\", \"-\") + \" \" + System.getProperty(\"java.vm.version\", \"-\")+ \"; \" + System.getProperty(\"java.vendor\", \"-\") + \")\\n----to here----\");\n	}\n}\n");
				}
				javaFunctionTest.performTest(Configuration.getInstance().getDataPath(), tempDir, result);
				try (JsonReader jsonReader = Json.createReader(new StringReader(result.getTestOutput()))) {
					object = jsonReader.readObject();

					String output = object.getString("stdout", "STDOUT empty");
					if (output.contains("----start here----") && output.contains("----to here----")) {
						int start = output.indexOf("----start here----\n") + "----start here----\n".length();
						output = output.substring(start, output.length() - "\n----to here----\n".length());
					}
					testresults.add(new TestResult("Java-Version des JavaAdvancedIOTests:", Util.escapeHTML(output), null));
				} catch (JsonParsingException ex) {
					testresults.add(new TestResult("JavaAdvancedIOTest-Ausgabe ist kein gültiges JSON.", Util.escapeHTML(result.getTestOutput()), false));
				}
			}
		} catch (Exception e) {
			testresults.add(new TestResult("JavaAdvancedIOTest erfolgreich.", Util.escapeHTML(e.getMessage()), false));
			LOG.error("JavaAdvancedIO test failed.", e);
			Util.recursiveDelete(tempDir);
			return;
		}

		Util.recursiveDelete(tempDir);
	}

	public static class TestResult {
		final public String test;
		final public String details;
		final public Boolean result;

		private TestResult(String test) {
			this.test = test;
			this.result = true;
			this.details = "";
		}

		private TestResult(String test, Boolean result) {
			this.test = test;
			this.result = result;
			this.details = "";
		}

		private TestResult(String test, String details, Boolean result) {
			this.test = test;
			this.details = details;
			this.result = result;
		}
	}
}
