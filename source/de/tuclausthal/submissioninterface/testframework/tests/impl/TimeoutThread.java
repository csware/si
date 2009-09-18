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

/**
 * Timeout thread for a process; kills a process after a given timeout
 * @author Sven Strickroth
 */
public class TimeoutThread extends Thread {
	private Process process;
	private int timeout;

	public TimeoutThread(Process process, int timeout) {
		this.process = process;
		this.timeout = timeout;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(timeout * 1000);
		} catch (InterruptedException e) {
			interrupt();
		}
		if (!interrupted()) {
			process.destroy();
		}
	}
}
