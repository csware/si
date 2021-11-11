/*
 * Copyright 2009-2014, 2017, 2020-2021 Sven Strickroth <email@cs-ware.de>
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.FilenameUtils;
import org.owasp.html.Handler;
import org.owasp.html.HtmlSanitizer;
import org.owasp.html.HtmlStreamRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.NormalizerIf;
import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.StripCommentsNormalizer;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;

/**
 * Utility-class with various helpers
 * @author Sven Strickroth
 */
public final class Util {
	final private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static Clock CLOCK = Clock.systemDefaultZone();

	/**
	 * Escapes HTML sequences
	 * @param message
	 * @return the escaped string
	 */
	public static String escapeHTML(String message) {
		if (message == null) {
			return "";
		}
		StringBuilder result = new StringBuilder(message.length() + 50);
		for (int i = 0; i < message.length(); ++i) {
			char element = message.charAt(i);
			switch (element) {
				case '<':
					result.append("&lt;");
					break;
				case '>':
					result.append("&gt;");
					break;
				case '&':
					result.append("&amp;");
					break;
				case '"':
					result.append("&quot;");
					break;
				default:
					result.append(element);
			}
		}
		return (result.toString());
	}

	public static String generateRedirectURL(String url, HttpServletResponse response) {
		return url;
	}

	public static String generateAbsoluteServletsRedirectURL(String url, HttpServletRequest request, HttpServletResponse response) {
		return generateRedirectURL(request.getServletContext().getContextPath() + Configuration.SERVLETS_PATH_WITH_BOTHSLASHES + url, response);
	}

	public static String generateHTMLLink(String url, HttpServletResponse response) {
		return escapeHTML(url);
	}

	public static String generateAbsoluteServletsHTMLLink(String uri, HttpServletRequest request, HttpServletResponse response) {
		return generateAbsoluteHTMLLink(Configuration.SERVLETS_PATH_WITH_ENDSLASH + uri, request, response);
	}

	public static String generateAbsoluteHTMLLink(String uri, HttpServletRequest request, HttpServletResponse response) {
		return generateHTMLLink(request.getServletContext().getContextPath() + "/" + uri, response);
	}

	public static String encodeURLPathComponent(String path) {
		try {
			return new URI(null, null, path, null).toASCIIString();
		} catch (URISyntaxException e) {
			LOG.error("Could not encode pathcomponent.", e);
		}
		return "";
	}

	/**
	 * Only allow certain HTML tags
	 * based on the Zikula Framework
	 * @param message
	 * @return partly escaped string
	 */
	public static String makeCleanHTML(String message) {
		if (message == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		HtmlStreamRenderer renderer = HtmlStreamRenderer.create(sb,
				new Handler<IOException>() {
					public void handle(IOException ex) {
						// System.out suppresses IOExceptions
						throw new AssertionError(null, ex);
					}
				},
				new Handler<String>() {
					public void handle(String x) {
						throw new AssertionError(x);
					}
				});
		// Use the policy defined above to sanitize the HTML.
		HtmlSanitizer.sanitize(message, HTMLSanitizerPolicy.POLICY_DEFINITION.apply(renderer));
		return sb.toString();
	}

	public static String textToHTML(String message) {
		if (message == null) {
			return "";
		}
		return escapeHTML(message).replace("\n", "<br>");
	}

	/**
	 * Escapes command line parameters
	 * @param message
	 * @return escaped string
	 */
	public static String escapeCommandlineArguments(String message) {
		if (message == null) {
			return null;
		}
		StringBuilder result = new StringBuilder(message.length() + 50);
		for (int i = 0; i < message.length(); ++i) {
			char element = message.charAt(i);
			switch (element) {
				case '<':
					result.append("\\<");
					break;
				case '>':
					result.append("\\>");
					break;
				case '&':
					result.append("\\&");
					break;
				case '"':
					result.append("\\\"");
					break;
				case '\'':
					result.append("\\'");
					break;
				case ';':
					result.append("\\;");
					break;
				case '|':
					result.append("\\|");
					break;
				default:
					result.append(element);
			}
		}
		return (result.toString());
	}

	public static final void copyInputStreamAndClose(File inputFile, File outputFile) throws IOException {
		try (FileInputStream in = new FileInputStream(inputFile)) {
			copyInputStreamAndClose(in, outputFile);
		}
	}

	public static final void copyInputStreamAndClose(InputStream in, File outputfile) throws IOException {
		try (FileOutputStream os = new FileOutputStream(outputfile)) {
			copyInputStreamAndClose(in, os);
		}
	}

	public static final void copyInputStreamAndClose(File inputFile, OutputStream out) throws IOException {
		try (FileInputStream in = new FileInputStream(inputFile)) {
			copyInputStreamAndClose(in, out);
		}
	}

	private static final void copyInputStreamAndClose(InputStream in, OutputStream out) throws IOException {
		try {
			byte[] buffer = new byte[8192];
			int len;
			while ((len = in.read(buffer)) >= 0) {
				out.write(buffer, 0, len);
			}
		} finally {
			if (in instanceof ZipInputStream) {
				((ZipInputStream) in).closeEntry();
			} else
				in.close();
			if (out instanceof ZipOutputStream) {
				((ZipOutputStream) out).closeEntry();
			} else {
				out.close();
			}
		}
	}

	/**
	 * Checks if the string is a valid integer
	 * @param integerString
	 * @return true if the string is an integer or false
	 */
	public static boolean isInteger(String integerString) {
		try {
			Integer.parseInt(integerString);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * Parses a string and returns an integer
	 * @param integerString the string to parse
	 * @param defaultValue the default value if the string is not an integer
	 * @return parsed integer or defaultValue
	 */
	public static int parseInteger(String integerString, int defaultValue) {
		try {
			return Integer.parseInt(integerString);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static List<String> listFilesAsRelativeStringList(File path) {
		List<String> submittedFiles = new ArrayList<>();
		if (path.exists() && path.listFiles() != null) {
			listFilesAsRelativeStringList(submittedFiles, path, "");
		}
		return submittedFiles;
	}

	private static void listFilesAsRelativeStringList(List<String> submittedFiles, File path, String relativePath) {
		for (File file : path.listFiles()) {
			if (file.isDirectory()) {
				listFilesAsRelativeStringList(submittedFiles, file, relativePath + file.getName() + System.getProperty("file.separator"));
			} else {
				submittedFiles.add(relativePath + file.getName());
			}
		}
	}

	public static List<String> listFilesAsRelativeStringList(File path, List<String> excludedFileNames) {
		List<String> submittedFiles = new ArrayList<>();
		if (path.exists() && path.listFiles() != null) {
			listFilesAsRelativeStringList(submittedFiles, path, "", excludedFileNames);
		}
		return submittedFiles;
	}

	private static void listFilesAsRelativeStringList(List<String> submittedFiles, File path, String relativePath, List<String> excludedFileNames) {
		for (File file : path.listFiles()) {
			// check that the file is not excluded
			if (!excludedFileNames.contains(file.getName())) {
				if (file.isDirectory()) {
					listFilesAsRelativeStringList(submittedFiles, file, relativePath + file.getName() + System.getProperty("file.separator"), excludedFileNames);
				} else {
					submittedFiles.add(relativePath + file.getName());
				}
			}
		}
	}

	/**
	 * Recursively delete all contained empty folders
	 * @param toDelete the path to cleanup of empty directories
	 */
	public static void recursiveDeleteEmptySubDirectories(File toDelete) {
		if (toDelete.isDirectory()) {
			for (File subFile : toDelete.listFiles()) {
				recursiveDeleteEmptyDirectories(subFile);
			}
		}
	}

	/**
	 * Recursively delete empty folders (including the specified one)
	 * @param toDelete the path to cleanup of empty directories
	 */
	public static void recursiveDeleteEmptyDirectories(File toDelete) {
		if (toDelete.isDirectory()) {
			for (File subFile : toDelete.listFiles()) {
				recursiveDeleteEmptyDirectories(subFile);
			}
			toDelete.delete();
		}
	}

	/**
	 * Recursive delete files/folders
	 * @param toDelete the path to delete
	 */
	public static void recursiveDelete(File toDelete) {
		if (toDelete.isDirectory()) {
			for (File subFile : toDelete.listFiles()) {
				recursiveDelete(subFile);
			}
		}
		toDelete.delete();
	}

	/**
	 * Recursively copy one folder/file to another
	 * @param fromFile the origin
	 * @param toFile the destination
	 * @throws IOException
	 */
	public static void recursiveCopy(File fromFile, File toFile) throws IOException {
		if (fromFile.isDirectory()) {
			toFile.mkdir();
			for (File subFile : fromFile.listFiles()) {
				recursiveCopy(subFile, new File(toFile.getAbsolutePath() + System.getProperty("file.separator") + subFile.getName()));
			}
			return;
		}
		copyInputStreamAndClose(fromFile, toFile);
	}

	/**
	 * method to recursively adding files from the base directory to a zip archive
	 *
	 * @param out ZipOutputStream
	 * @param path Path of the current directory
	 * @param relativePath relative path of the 'next' directory
	 */
	public static void recursivelyZip(ZipOutputStream out, File path, String relativePath) {
		for (File file : path.listFiles()) {
			try {
				if (file.isFile()) {
					out.putNextEntry(new ZipEntry(relativePath + file.getName()));
					copyInputStreamAndClose(file, out);
				} else {
					recursivelyZip(out, file, relativePath + file.getName() + System.getProperty("file.separator"));
				}
			} catch (Exception e) {
				// ignore ;)
				LOG.error("Error creating zip file", e);
			}
		}
	}

	/**
	 * Returns the current semester
	 * @return the semester encoded as integer
	 */
	public static int getCurrentSemester() {
		if (ZonedDateTime.now(CLOCK).getMonth().getValue() < 3) {
			// running winter lecture of last year
			return (ZonedDateTime.now(CLOCK).getYear() - 1) * 10 + 1;
		} else if (ZonedDateTime.now(CLOCK).getMonth().getValue() > 8) {
			// winter lecture
			return ZonedDateTime.now(CLOCK).getYear() * 10 + 1;
		}
		return ZonedDateTime.now(CLOCK).getYear() * 10;
	}

	public static int decreaseSemester(int semester) {
		if (semester % 2 != 0) {
			return semester - 1;
		}
		return semester - 10 + 1;
	}

	public static int increaseSemester(int semester) {
		if (semester % 2 == 0) {
			return semester + 1;
		}
		return semester + 10 - 1;
	}

	/**
	 * Opens the specified file and returns its contents as string buffer
	 * @param file the file to open
	 * @return the file contents with \n EOL only
	 * @throws IOException
	 */
	public static StringBuffer loadFile(File file) throws IOException {
		StringWriter sw = new StringWriter((int) file.length());
		try (Reader br = new CrLfFilterReader(new BufferedReader(new FileReader(file)))) {
			char[] buffer = new char[8192];
			int len;
			while ((len = br.read(buffer)) >= 0) {
				sw.write(buffer, 0, len);
			}
		}
		return sw.getBuffer();
	}

	/**
	 * Converts a boolean to a better readable html code
	 * @param bool
	 * @param nullValue String to output if bool is null
	 * @return a string representing the value of bool
	 */
	public static String boolToHTML(Boolean bool, String nullValue) {
		if (bool == null)
			return nullValue;
		return boolToHTML(bool.booleanValue());
	}

	/**
	 * Converts a boolean to a better readable html code
	 * @param bool
	 * @return a string representing the value of bool
	 */
	public static String boolToHTML(boolean bool) {
		if (bool)
			return "<span class=green>ja</span>";
		return "<span class=red>nein</span>";
	}

	/**
	 * Creates a temporary directory
	 * @param prefix
	 * @return the temporary directory or null on error
	 */
	public static File createTemporaryDirectory(String prefix) {
		try {
			return Files.createTempDirectory(prefix).toFile();
		} catch (IOException e) {
		}
		return null;
	}

	public static String showPoints(int maxPoints) {
		return NumberFormat.getInstance(Locale.GERMANY).format(maxPoints / 100.0);
	}

	public static int convertToPoints(String parameter) {
		try {
			int points = ((Double) (Double.parseDouble(parameter.replace(",", ".")) * 100.0)).intValue();
			if (points < 0) {
				points = 0;
			}
			return points;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static int convertToPoints(String parameter, int minPointStep) {
		int points = convertToPoints(parameter);
		if (points % minPointStep == 0) {
			return points;
		}
		return (points / minPointStep) * minPointStep;
	}

	public static String getPointsCSSClass(Points points) {
		String pointsClass = "";
		if (points.getPointStatus() == PointStatus.ABGENOMMEN_FAILED.ordinal()) {
			pointsClass = " abgenfailed";
		} else if (points.getDuplicate() != null) {
			pointsClass = " dupe";
		}
		return pointsClass;
	}

	public static String getUploadFileName(Part part) {
		return FilenameUtils.getName(part.getSubmittedFileName());
	}

	/**
	 * Saves a POST-submitted file (item) with filename fileName
	 * * if the file is a Java-file (i.e. ends with .java) parse the package and create the package subfolders below path
	 * * else it directly saves the file to path
	 * @param item uploaded file part
	 * @param path base path to the submission directory
	 * @param fileName name of the file to handle
	 * @throws IOException 
	 */
	public static void saveAndRelocateJavaFile(Part item, File path, String fileName) throws IOException {
		File uploadedFile = new File(path, fileName);
		// handle .java-files differently in order to extract package and move it to the correct folder
		if (fileName.toLowerCase().endsWith(".java")) {
			uploadedFile = File.createTempFile("upload", null, path);
		}
		copyInputStreamAndClose(item.getInputStream(), uploadedFile);
		// extract defined package in java-files
		if (fileName.toLowerCase().endsWith(".java")) {
			NormalizerIf stripComments = new StripCommentsNormalizer();
			StringBuffer javaFileContents = stripComments.normalize(Util.loadFile(uploadedFile));
			Pattern packagePattern = Pattern.compile(".*\\bpackage\\s+([a-zA-Z$]([a-zA-Z0-9_$]|\\.[a-zA-Z0-9_$])*)\\s*;.*", Pattern.DOTALL);
			Matcher packageMatcher = packagePattern.matcher(javaFileContents);
			File destFile = new File(path, fileName);
			if (packageMatcher.matches()) {
				String packageName = packageMatcher.group(1).replace(".", System.getProperty("file.separator"));
				File packageDirectory = new File(path, packageName);
				packageDirectory.mkdirs();
				destFile = new File(packageDirectory, fileName);
			}
			if (destFile.exists() && destFile.isFile()) {
				destFile.delete();
			}
			if (!uploadedFile.renameTo(destFile)) { // renameTo does not work across different filesystems
				Util.recursiveCopy(uploadedFile, destFile);
				uploadedFile.delete();
			}
		}
	}

	public static void lowerCaseExtension(StringBuffer filename) {
		if (filename.lastIndexOf(".") > 0) {
			int lastDot = filename.lastIndexOf(".");
			filename.replace(lastDot, filename.length(), filename.subSequence(lastDot, filename.length()).toString().toLowerCase());
		}
	}
}
