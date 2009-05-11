package de.tuclausthal.abgabesystem.util;

import java.io.File;
import java.util.Date;

/**
 * Utility-class with various helpers
 * @author Sven Strickroth
 */
public final class Util {
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

	public static boolean isInteger(String integerString) {
		try {
			Integer.parseInt(integerString);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static int parseInteger(String integerString, int defaultValue) {
		try {
			return Integer.parseInt(integerString);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static void recursiveDelete(File toDelete) {
		if (toDelete.isDirectory()) {
			for (File subFile : toDelete.listFiles()) {
				recursiveDelete(subFile);
			}
		}
		toDelete.delete();
	}

	public static int getCurrentSemester() {
		Date date = new Date();
		if (date.getMonth() > 7) {
			// winter lecture
			return date.getYear() * 10 + 19001;
		} else {
			return date.getYear() * 10 + 19000;
		}
	}

	public static String boolToHTML(boolean bool) {
		if (bool) {
			return "<span class=green>ja</span>";
		} else {
			return "<span class=red>nein</span>";
		}
	}
}
