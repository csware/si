/*
 * Copyright 2009 - 2010,2012 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.testframework.tests.impl;

/**
 * Timeout thread for a process; kills a process after a given timeout
 * @author Sven Strickroth
 */
public class TimeoutThread extends Thread {
	private Process process;
	private int timeout;
	private boolean aborted = false;

	/**
	 * @param process
	 * @param timeout
	 */
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
			aborted = true;
		}
	}

	/**
	 * Returns if the process was terminated after the timeout
	 * @return the aborted
	 */
	public boolean wasAborted() {
		return aborted;
	}
}
