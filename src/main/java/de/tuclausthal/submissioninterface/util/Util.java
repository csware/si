/*
 * Copyright 2009-2014, 2017, 2020 Sven Strickroth <email@cs-ware.de>
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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.ParameterParser;
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
	final private static Logger log = LoggerFactory.getLogger(Util.class);

	/**
	 * Escapes HTML sequences
	 * @param message
	 * @return the escaped string
	 */
	public static String escapeHTML(String message) {
		if (message == null) {
			return "";
		}
		char content[] = new char[message.length()];
		message.getChars(0, message.length(), content, 0);
		StringBuffer result = new StringBuffer(content.length + 50);
		for (char element : content) {
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
		String regexp = "(?is:<\\s*(!--.*?--|/?\\s*a(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*b(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*blockquote(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*br(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*sup(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*sub(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*center(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*div(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?dl\\s*/?|/?dd\\s*/?|/?dt\\s*/?|/?\\s*em(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*font(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?h1\\s*/?|/?h2\\s*/?|/?\\s*h3(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?h4\\s*/?|/?h5\\s*/?|/?h6\\s*/?|/?\\s*hr(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*i(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*img(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*li(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*ol(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*p(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*pre(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*span(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*strong(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*table(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?tbody\\s*/?|/?\\s*td(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*th(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*tr(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*tt(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?|/?\\s*ul(\\s+[\\w:]+\\s*=\\s*(\"[^\"]*\"|'[^']*'))*\\s*/?)\\s*>)";
		String string = escapeHTML(message.replaceAll(regexp, "\022$1\024"));
		Pattern pattern = Pattern.compile("\022([^\024]*)\024");
		Matcher matcher = pattern.matcher(string);
		StringBuffer returnString = new StringBuffer(string.length() + 50);
		while (matcher.find()) {
			matcher.appendReplacement(returnString, "<" + matcher.toMatchResult().group(1).replace("&gt;", ">").replace("&lt;", "<").replace("&quot;", "\"") + ">");
		}
		matcher.appendTail(returnString);
		return returnString.toString().replaceAll("(?i:&amp;([a-z#0-9]+);)", "&$1;");
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
		char content[] = new char[message.length()];
		message.getChars(0, message.length(), content, 0);
		StringBuffer result = new StringBuffer(content.length + 50);
		for (char element : content) {
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

	public static final void copyInputStreamAndClose(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}

		if (in instanceof ZipInputStream) {
			((ZipInputStream) in).closeEntry();
		} else
			in.close();
		if (out instanceof ZipOutputStream) {
			((ZipOutputStream) out).closeEntry();
		} else
			out.close();
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
		} else {
			BufferedReader in = new BufferedReader(new FileReader(fromFile));
			BufferedWriter out = new BufferedWriter(new FileWriter(toFile));
			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}
			in.close();
			out.close();
		}
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
					Util.copyInputStreamAndClose(new FileInputStream(file), out);
				} else {
					recursivelyZip(out, file, relativePath + file.getName() + System.getProperty("file.separator"));
				}
			} catch (Exception e) {
				// ignore ;)
				log.error("Error creating zip file", e);
			}
		}
	}

	/**
	 * Returns the current semester
	 * @return the semester encoded as integer
	 */
	public static int getCurrentSemester() {
		Date date = new Date();
		if (date.getMonth() > 7) {
			// winter lecture
			return date.getYear() * 10 + 19001;
		}
		return date.getYear() * 10 + 19000;
	}

	/**
	 * Opens the specified file and returns it's contents as string buffer
	 * @param file the file to open
	 * @return the file contents
	 * @throws IOException
	 */
	public static StringBuffer loadFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuffer sb = new StringBuffer((int) file.length());
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
		br.close();
		return sb;
	}

	/**
	 * Converts a boolean to a better readable html code
	 * @param bool
	 * @return a string representing the value of bool
	 */
	public static String boolToHTML(Boolean bool) {
		if (bool == null) {
			return "wird gerade getestet, bitte Seite neu laden";
		} else if (bool) {
			return "<span class=green>ja</span>";
		} else {
			return "<span class=red>nein</span>";
		}
	}

	/**
	 * Creates a temporaty directory
	 * @param prefix
	 * @param suffix
	 * @return the temporate directory or null on error
	 */
	public static File createTemporaryDirectory(String prefix, String suffix) {
		File temp;
		try {
			temp = File.createTempFile(prefix, suffix);
			if (temp.delete() != true || temp.mkdirs() != true) {
				temp = null;
			}
		} catch (IOException e) {
			temp = null;
		}
		return temp;
	}

	/**
	 * Corrects timezone if needed
	 * @param date
	 * @return a corrected date
	 */
	public static Date correctTimezone(Date date) {
		return date;
	}

	public static String showPoints(int maxPoints) {
		return NumberFormat.getInstance().format(maxPoints / 100.0);
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

	public static String csvQuote(String title) {
		if (title == null) {
			return null;
		}
		if (title.contains(";")) {
			title = "\"" + title + "\""; // .replace("\"", "\"\"")
		}
		return title;
	}

	// taken from Apache::Commons::FileUpload, FileUploadBase
	public static String getUploadFileName(Part part) {
		String pContentDisposition = part.getHeader(FileUploadBase.CONTENT_DISPOSITION);
		String fileName = null;
		if (pContentDisposition != null) {
			String cdl = pContentDisposition.toLowerCase(Locale.ENGLISH);
			if (cdl.startsWith(FileUploadBase.FORM_DATA) || cdl.startsWith(FileUploadBase.ATTACHMENT)) {
				ParameterParser parser = new ParameterParser();
				parser.setLowerCaseNames(true);
				// Parameter parser can handle null input
				Map<String, String> params = parser.parse(pContentDisposition, ';');
				if (params.containsKey("filename")) {
					fileName = params.get("filename");
					if (fileName != null) {
						fileName = fileName.trim();
					} else {
						// Even if there is no value, the parameter is present,
						// so we return an empty file name rather than no file
						// name.
						fileName = "";
					}
				}
			}
		}
		return fileName;
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
		copyInputStreamAndClose(item.getInputStream(), new BufferedOutputStream(new FileOutputStream(uploadedFile)));
		// extract defined package in java-files
		if (fileName.toLowerCase().endsWith(".java")) {
			NormalizerIf stripComments = new StripCommentsNormalizer();
			StringBuffer javaFileContents = stripComments.normalize(Util.loadFile(uploadedFile));
			Pattern packagePattern = Pattern.compile(".*package\\s+([a-zA-Z$]([a-zA-Z0-9_$]|\\.[a-zA-Z0-9_$])*)\\s*;.*", Pattern.DOTALL);
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

	public static void cleanCrLf(StringBuffer stringBuffer) {
		for (int i = 1; i < stringBuffer.length();) {
			if (stringBuffer.charAt(i - 1) == '\r' && stringBuffer.charAt(i) == '\n') {
				stringBuffer.deleteCharAt(i - 1);
				continue;
			}
			++i;
		}
	}
}
