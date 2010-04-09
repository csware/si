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

package de.tuclausthal.submissioninterface.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Utility-class with various helpers
 * @author Sven Strickroth
 */
public final class Util {
	/**
	 * Escapes HTML sequences
	 * @param message
	 * @return the escaped string
	 */
	public static String mknohtml(String message) {
		if (message == null) {
			return (null);
		}
		char content[] = new char[message.length()];
		message.getChars(0, message.length(), content, 0);
		StringBuffer result = new StringBuffer(content.length + 50);
		for (int i = 0; i < content.length; i++) {
			switch (content[i]) {
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
					result.append(content[i]);
			}
		}
		return (result.toString());
	}

	public static String mkTextToHTML(String message) {
		if (message == null) {
			return ("");
		}
		return mknohtml(message).replace("\n", "<br>");
	}

	/**
	 * Escapes command line parameters
	 * @param message
	 * @return
	 */
	public static String mksafecmdargs(String message) {
		if (message == null) {
			return (null);
		}
		char content[] = new char[message.length()];
		message.getChars(0, message.length(), content, 0);
		StringBuffer result = new StringBuffer(content.length + 50);
		for (int i = 0; i < content.length; i++) {
			switch (content[i]) {
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
					result.append(content[i]);
			}
		}
		return (result.toString());
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
	 * @return
	 */
	public static int parseInteger(String integerString, int defaultValue) {
		try {
			return Integer.parseInt(integerString);
		} catch (NumberFormatException e) {
			return defaultValue;
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
	 * Returns the current semester
	 * @return
	 */
	public static int getCurrentSemester() {
		Date date = new Date();
		if (date.getMonth() > 7) {
			// winter lecture
			return date.getYear() * 10 + 19001;
		} else {
			return date.getYear() * 10 + 19000;
		}
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
	 * @return
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
	 * @return
	 */
	public static Date correctTimezone(Date date) {
		return date;
	}
}
