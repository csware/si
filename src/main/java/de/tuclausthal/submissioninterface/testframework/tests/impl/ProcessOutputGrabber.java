/*
 * Copyright 2020, 2024 Sven Strickroth <email@cs-ware.de>
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
 * Helper for reading STDERR and STDOUT from a process
 * It is important to read both at the same time to prevent the executed program from blocking if a system buffer is full.
 * @author Sven Strickroth
 */
public final class ProcessOutputGrabber {
	private final ReadOutputThread readStdOutThread;
	private final ReadOutputThread readStdErrThread;

	/**
	 * Grabber for stdout and stderr of a process into a StringBuffer
	 * @param process the process to grab the stdout and stderr from
	 */
	public ProcessOutputGrabber(final Process process) {
		readStdOutThread = new ReadOutputThread(process.getInputStream());
		readStdErrThread = new ReadOutputThread(process.getErrorStream());
		readStdOutThread.start();
		readStdErrThread.start();
	}

	public StringBuffer getStdOutBuffer() {
		return readStdOutThread.getBuffer();
	}

	public StringBuffer getStdErrBuffer() {
		return readStdErrThread.getBuffer();
	}

	/**
	 * Wait for the threads to finish
	 * @throws InterruptedException 
	 */
	public void waitFor() throws InterruptedException {
		readStdOutThread.join();
		readStdErrThread.join();
	}
}
