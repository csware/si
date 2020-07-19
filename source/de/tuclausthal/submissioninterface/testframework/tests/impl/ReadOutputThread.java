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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Thread for reading output from the process
 * @author Sven Strickroth
 */
public class ReadOutputThread extends Thread {
	final private BufferedReader streamReader;
	final private StringBuffer stringBuffer = new StringBuffer();

	public ReadOutputThread(InputStream is) {
		streamReader = new BufferedReader(new InputStreamReader(is));
	}

	public StringBuffer getBuffer() {
		return stringBuffer;
	}

	@Override
	public void run() {
		try {
			String line;
			while ((line = streamReader.readLine()) != null) {
				stringBuffer.append(line);
			}
		} catch (IOException e) {
		} finally {
			try {
				streamReader.close();
			} catch (IOException e) {
			}
		}
	}
}
