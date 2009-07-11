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

package de.tuclausthal.submissioninterface.executiontask.executer.impl;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;

import de.tuclausthal.submissioninterface.executiontask.executer.ExecutionTaskExecuterIf;
import de.tuclausthal.submissioninterface.executiontask.task.ExecutionTask;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Local threaded ExecutionTaskExecuter implementation
 * @author Sven Strickroth
 */
public class LocalExecuter implements ExecutionTaskExecuterIf, Runnable {
	private static LocalExecuter instance = null;
	private List<Thread> workers = null;
	private Queue<ExecutionTask> taskQueue = new LinkedList<ExecutionTask>();
	private Boolean monitor = true;
	static int CORES = 1;
	static File dataPath;

	private LocalExecuter() {}

	/**
	 * Returns the instance (Singleton)
	 * @return
	 */
	public static synchronized LocalExecuter getInstance() {
		if (instance == null) {
			instance = new LocalExecuter();
		}
		return instance;
	}

	@Override
	public synchronized void executeTask(ExecutionTask executionTask) {
		taskQueue.add(executionTask);
		if (workers == null) {
			workers = new LinkedList<Thread>();
			for (int i = 0; i< CORES; i++) {
				workers.add(new Thread(this));
				workers.get(i).setDaemon(true);
				workers.get(i).start();
			}
		}
		synchronized (monitor) {
			monitor.notify();
		}
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			ExecutionTask performTask = getHead();
			if (performTask != null) {
				performTask.performTask(dataPath);
			} else {
				try {
					synchronized (monitor) {
						monitor.wait();
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	/**
	 * Returns an executiontask to execute
	 * @return the first executionTask of the queue
	 */
	private synchronized ExecutionTask getHead() {
		return taskQueue.poll();
	}
}
