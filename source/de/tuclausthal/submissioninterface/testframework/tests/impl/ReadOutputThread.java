/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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
import java.io.InputStreamReader;

/**
 * Thread for reading output from the process
 * @author Sven Strickroth
 */
public class ReadOutputThread extends Thread {
	private BufferedReader stdOutInputStream;
	private StringBuffer stdOutStringBuffer = new StringBuffer();
	private BufferedReader stdErrInputStream;
	private StringBuffer stdErrStringBuffer = new StringBuffer();

	public ReadOutputThread(Process process) {
		stdOutInputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
		stdErrInputStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	}

	public String getStdOut() {
		return stdOutStringBuffer.toString();
	}

	public String getStdErr() {
		return stdErrStringBuffer.toString();
	}

	@Override
	public void run() {
		while (!interrupted()) {
			String line;
			try {
				while ((line = stdOutInputStream.readLine()) != null) {
					stdOutStringBuffer.append(line + "\n");
				}
				while ((line = stdErrInputStream.readLine()) != null) {
					stdErrStringBuffer.append(line + "\n");
				}
			} catch (IOException e) {
			}
			try {
				// don't cycle too fast
				Thread.sleep(100);
			} catch (InterruptedException e) {
				interrupt();
			}
		}
	}
}
