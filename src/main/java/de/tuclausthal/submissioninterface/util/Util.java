/*
 * Copyright 2009-2014, 2017, 2020-2024 Sven Strickroth <email@cs-ware.de>
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tuclausthal.submissioninterface.dupecheck.normalizers.NormalizerIf;
import de.tuclausthal.submissioninterface.dupecheck.normalizers.impl.StripCommentsNormalizer;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

/**
 * Utility-class with various helpers
 * @author Sven Strickroth
 */
public final class Util {
	final private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static Clock CLOCK = Clock.systemDefaultZone();
	final private static String FILE_SEPARATOR = System.getProperty("file.separator");

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

	private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' }; // RFC3986 recommends uppercase letters

	// based on java.net.URI::encode(String)
	public static String encodeURLPathComponent(String path) {
		int length = path.length();
		if (length == 0)
			return path;

		try {
			String s = new URI(null, null, path, null).toString();

			// Check whether we actually need to path encode the string
			for (int i = 0;;) {
				if (s.charAt(i) >= '\u0080')
					break;
				if (++i >= length)
					return s;
			}

			ByteBuffer bb = null;
			try {
				bb = StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(s));
			} catch (CharacterCodingException x) {
				assert false;
			}

			StringBuilder sb = new StringBuilder();
			while (bb.hasRemaining()) {
				int b = bb.get() & 0xff;
				if (b >= 0x80) {
					sb.append('%');
					sb.append(hexDigits[(b >> 4) & 0x0f]);
					sb.append(hexDigits[(b >> 0) & 0x0f]);
				} else {
					sb.append((char) b);
				}
			}
			return sb.toString();
		} catch (URISyntaxException e) {
			LOG.error("Could not encode pathcomponent.", e);
		}
		return "";
	}

	/**
	 * Only allow certain HTML tags
	 * @param message
	 * @return partly escaped string
	 */
	public static String makeCleanHTML(String message) {
		return HTMLSanitizerPolicy.POLICY_DEFINITION.sanitize(message);
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

	public static final void copyInputStreamAndClose(final Path inputFile, final Path outputFile) throws IOException {
		try (InputStream in = Files.newInputStream(inputFile)) {
			copyInputStreamAndClose(in, outputFile);
		}
	}

	public static final void copyInputStreamAndClose(final InputStream in, final Path outputfile) throws IOException {
		try (OutputStream os = Files.newOutputStream(outputfile)) {
			copyInputStreamAndClose(in, os);
		}
	}

	public static final void copyInputStreamAndClose(final Path inputFile, final OutputStream out) throws IOException {
		try (InputStream in = Files.newInputStream(inputFile)) {
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

	public static List<String> listFilesAsRelativeStringList(final Path path) {
		List<String> submittedFiles = new ArrayList<>();
		try {
			if (Files.exists(path) && !PathUtils.isEmptyDirectory(path)) {
				listFilesAsRelativeStringList(submittedFiles, path, "");
			}
		} catch (IOException e) {
			// ignore ;)
			LOG.error("Error listing all files of a path", e);
		}
		return submittedFiles;
	}

	public static List<String> listFilesAsRelativeStringListSorted(final Path path) {
		List<String> submittedFiles = listFilesAsRelativeStringList(path);
		Collections.sort(submittedFiles);
		return submittedFiles;
	}

	private static void listFilesAsRelativeStringList(final List<String> submittedFiles, final Path path, final String relativePath) throws IOException {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
			for (final Path file : directoryStream) {
				if (Files.isDirectory(file)) {
					listFilesAsRelativeStringList(submittedFiles, file, relativePath + file.getFileName() + FILE_SEPARATOR);
				} else {
					submittedFiles.add(relativePath + file.getFileName());
				}
			}
		}
	}

	public static List<String> listFilesAsRelativeStringList(final Path path, final List<String> excludedFileNames) {
		List<String> submittedFiles = new ArrayList<>();
		try {
			if (Files.exists(path) && !PathUtils.isEmptyDirectory(path)) {
				listFilesAsRelativeStringList(submittedFiles, path, "", excludedFileNames);
			}
		} catch (IOException e) {
			// ignore ;)
			LOG.error("Error listing all files of a path", e);
		}
		return submittedFiles;
	}

	private static void listFilesAsRelativeStringList(final List<String> submittedFiles, final Path path, final String relativePath, final List<String> excludedFileNames) throws IOException {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
			for (final Path file : directoryStream) {
				// check that the file is not excluded
				if (!excludedFileNames.contains(file.getFileName().toString())) {
					if (Files.isDirectory(file)) {
						listFilesAsRelativeStringList(submittedFiles, file, relativePath + file.getFileName() + FILE_SEPARATOR, excludedFileNames);
					} else {
						submittedFiles.add(relativePath + file.getFileName());
					}
				}
			}
		}
	}

	/**
	 * Recursively delete all contained empty folders
	 * @param toDelete the path to cleanup of empty directories
	 * @throws IOException 
	 */
	public static void recursiveDeleteEmptySubDirectories(final Path toDelete) throws IOException {
		if (Files.isDirectory(toDelete)) {
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(toDelete)) {
				for (final Path subFile : directoryStream) {
					recursiveDeleteEmptyDirectories(subFile);
				}
			}
		}
	}

	/**
	 * Recursively delete empty folders (including the specified one)
	 * @param toDelete the path to cleanup of empty directories
	 * @throws IOException 
	 */
	public static void recursiveDeleteEmptyDirectories(final Path toDelete) throws IOException {
		if (Files.isDirectory(toDelete)) {
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(toDelete)) {
				for (final Path subFile : directoryStream) {
					recursiveDeleteEmptyDirectories(subFile);
				}
			}
			if (PathUtils.isEmptyDirectory(toDelete)) {
				Files.delete(toDelete);
			}
		}
	}

	/**
	 * Recursive delete files/folders
	 * @param toDelete the path to delete
	 */
	public static void recursiveDelete(final Path toDelete) {
		if (Files.isDirectory(toDelete)) {
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(toDelete)) {
				for (final Path subFile : directoryStream) {
					recursiveDelete(subFile);
				}
			} catch (IOException e) {
				// ignore ;)
				LOG.error("Error deleting a path", e);
			}
		}
		try {
			Files.deleteIfExists(toDelete);
		} catch (IOException e) {
			// ignore ;)
			LOG.error("Error deleting a path", e);
		}
	}

	/**
	 * Recursively copy one folder/file to another
	 * @param fromFile the origin
	 * @param toFile the destination
	 * @throws IOException
	 */
	public static void recursiveCopy(final Path fromFile, final Path toFile) throws IOException {
		if (Files.isDirectory(fromFile)) {
			if (!Files.isDirectory(toFile)) {
				Files.createDirectory(toFile);
			}
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fromFile)) {
				for (final Path path : directoryStream) {
					recursiveCopy(path, toFile.resolve(path.getFileName()));
				}
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
	public static void recursivelyZip(final ZipOutputStream out, final Path path, final String relativePath) {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
			for (final Path file : directoryStream) {
				if (Files.isRegularFile(file)) {
					out.putNextEntry(new ZipEntry(relativePath + file.getFileName()));
					copyInputStreamAndClose(file, out);
				} else {
					recursivelyZip(out, file, relativePath + file.getFileName() + FILE_SEPARATOR);
				}
			}
		} catch (Exception e) {
			// ignore ;)
			LOG.error("Error creating zip file", e);
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
	public static StringBuffer loadFile(final Path file) throws IOException {
		if (Files.size(file) >= Integer.MAX_VALUE) {
			throw new IOException("File too large");
		}
		final StringWriter sw = new StringWriter((int) Files.size(file));
		// Files.newBufferedReader(file) does not work as it checks for valid UTF-8 input
		try (Reader br = new CrLfFilterReader(new BufferedReader(new InputStreamReader(Files.newInputStream(file))))) {
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
	public static Path createTemporaryDirectory(final String prefix) {
		try {
			return Files.createTempDirectory(prefix);
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
		return ensureMinPointStepMultiples(points, minPointStep);
	}

	public static int ensureMinPointStepMultiples(int points, int minPointStep) {
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
	public static void saveAndRelocateJavaFile(final Part item, final Path path, final String fileName) throws IOException {
		Path uploadedFile = path.resolve(fileName);
		// handle .java-files differently in order to extract package and move it to the correct folder
		if (fileName.toLowerCase().endsWith(".java")) {
			uploadedFile = Files.createTempFile(path, "upload", ".java");
		}
		try (InputStream is = item.getInputStream()) {
			copyInputStreamAndClose(is, uploadedFile);
		}
		// extract defined package in java-files
		if (fileName.toLowerCase().endsWith(".java")) {
			NormalizerIf stripComments = new StripCommentsNormalizer();
			StringBuffer javaFileContents = stripComments.normalize(Util.loadFile(uploadedFile));
			Pattern packagePattern = Pattern.compile(".*\\bpackage\\s+([a-zA-Z$]([a-zA-Z0-9_$]|\\.[a-zA-Z0-9_$])*)\\s*;.*", Pattern.DOTALL);
			Matcher packageMatcher = packagePattern.matcher(javaFileContents);
			Path destFile = path.resolve(fileName);
			if (packageMatcher.matches()) {
				String packageName = packageMatcher.group(1).replace(".", FILE_SEPARATOR);
				Path packageDirectory = path.resolve(packageName);
				Files.createDirectories(packageDirectory);
				destFile = packageDirectory.resolve(fileName);
			}
			if (Files.isRegularFile(destFile)) {
				Files.deleteIfExists(destFile);
			}
			Files.move(uploadedFile, destFile);
		}
	}

	public static void lowerCaseExtension(StringBuffer filename) {
		if (filename.lastIndexOf(".") > 0) {
			int lastDot = filename.lastIndexOf(".");
			filename.replace(lastDot, filename.length(), filename.subSequence(lastDot, filename.length()).toString().toLowerCase());
		}
	}

	public static void ensurePathExists(final Path path) throws IOException {
		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}
	}

	public static Path buildPath(final Path basePath, final String relativePath) {
		if (relativePath.isBlank() || relativePath.charAt(0) == '/' || relativePath.charAt(0) == '\\') {
			return null;
		}
		if (!basePath.isAbsolute()) {
			throw new RuntimeException("Path not absolute!");
		}
		final Path combinedPath = Path.of(basePath.toString(), relativePath.replace('\\', '/')).normalize();
		if (!combinedPath.startsWith(basePath) || basePath.equals(combinedPath)) {
			return null;
		}
		return combinedPath;
	}

	public static Path constructPath(final Path basePath, final Lecture lecture) {
		return basePath.resolve("lectures" + FILE_SEPARATOR + String.valueOf(lecture.getId()));
	}

	public static Path constructPath(final Path basePath, final Task task) {
		return basePath.resolve("lectures" + FILE_SEPARATOR + task.getTaskGroup().getLecture().getId() + FILE_SEPARATOR + task.getTaskid());
	}

	public static Path constructPath(final Path basePath, final Submission submission) {
		final Task task = submission.getTask();
		return basePath.resolve("lectures" + FILE_SEPARATOR + task.getTaskGroup().getLecture().getId() + FILE_SEPARATOR + task.getTaskid() + FILE_SEPARATOR + submission.getSubmissionid());
	}

	public static Path constructPath(final Path basePath, final Task task, final TaskPath pathComponent) {
		return basePath.resolve("lectures" + FILE_SEPARATOR + task.getTaskGroup().getLecture().getId() + FILE_SEPARATOR + task.getTaskid() + FILE_SEPARATOR + pathComponent.getPathComponent());
	}
}
