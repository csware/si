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

package de.tuclausthal.submissioninterface.executiontask;

import de.tuclausthal.submissioninterface.executiontask.executer.impl.LocalExecuter;
import de.tuclausthal.submissioninterface.executiontask.task.impl.CompileTestTask;
import de.tuclausthal.submissioninterface.executiontask.task.impl.FunctionTestTask;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

/**
 * ExecutionTask factory/distributor
 * @author Sven Strickroth
 */
public class ExecutionTaskExecute {
	public static void compileTestTask(Submission submission) {
		LocalExecuter.getInstance().executeTask(new CompileTestTask(submission));
	}
	public static void functionTestTask(Submission submission) {
		LocalExecuter.getInstance().executeTask(new FunctionTestTask(submission));
	}
}
