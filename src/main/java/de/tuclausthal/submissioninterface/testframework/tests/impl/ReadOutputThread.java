/*
 * Copyright 2009-2010, 2020 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.tests.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.tuclausthal.submissioninterface.util.Util;

/**
 * Thread for reading output from the process
 * @author Sven Strickroth
 */
public class ReadOutputThread extends Thread {
	final private InputStreamReader streamReader;
	final private StringBuffer stringBuffer = new StringBuffer();

	final private static int BUFFER_LENGTH = 8 * 1024;
	final private static int MAX_LENGTH = 1024 * 1024;

	public ReadOutputThread(InputStream is) {
		streamReader = new InputStreamReader(is);
	}

	public StringBuffer getBuffer() {
		return stringBuffer;
	}

	@Override
	public void run() {
		boolean truncated = false;
		try {
			char buffer[] = new char[BUFFER_LENGTH];
			int read;
			while ((read = streamReader.read(buffer)) != -1) {
				if (stringBuffer.length() >= MAX_LENGTH) {
					truncated = true;
					streamReader.skip(Long.MAX_VALUE);
					continue;
				}
				if (stringBuffer.length() + read >= MAX_LENGTH) {
					truncated = true;
					read = MAX_LENGTH - stringBuffer.length();
				}
				stringBuffer.append(buffer, 0, read);
			}
		} catch (IOException e) {
		} finally {
			try {
				streamReader.close();
			} catch (IOException e) {
			}
		}
		if (truncated) {
			stringBuffer.append("\n\nOUTPUT TOO LONG: TRUNCATED HERE");
		}
		Util.cleanCrLf(stringBuffer);
		if (stringBuffer.length() != 0 && stringBuffer.charAt(stringBuffer.length() - 1) != '\n') {
			stringBuffer.append("\n");
		}
	}
}
