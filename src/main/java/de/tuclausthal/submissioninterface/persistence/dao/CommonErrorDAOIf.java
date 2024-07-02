/*
 * Copyright 2021-2024 Sven Strickroth <email@cs-ware.de>
 * Copyright 2021 Florian Holzinger <f.holzinger@campus.lmu.de>
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

package de.tuclausthal.submissioninterface.persistence.dao;

import java.util.List;
import java.util.Map;

import de.tuclausthal.submissioninterface.persistence.datamodel.CommonError;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Test;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;

/**
 * Data Access Object Interface for the CommonError-class
 * @author Florian Holzinger
 */
public interface CommonErrorDAOIf {

	public void reset(Test test);

	public void reset(Task task);

	public CommonError newCommonError(String title, String commonErrorName, TestResult testResult, CommonError.Type errorType);

	public CommonError getCommonError(int id);

	public CommonError getCommonError(String title, Test test);

	public List<CommonError> getCommonErrors(Test test);

	Map<Submission, List<CommonError>> getErrorsForSubmissions(Test test, List<Submission> submissions);
}
