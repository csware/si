/*
 * Copyright 2011 Giselle Rodriguez
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ResultDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Result;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

/**
 * Data Access Object implementation for the ResultDAOIf
 * @author Giselle Rodriguez
 */
public class ResultDAO extends AbstractDAO implements ResultDAOIf {
	public ResultDAO(Session session) {
		super(session);
	}

	@Override
	public Result getResult(int resultid) {
		return (Result) getSession().get(Result.class, resultid);
	}

	@Override
	public Result getResult(Task task, User user) {
		Session session = getSession();
		Submission submission = DAOFactory.SubmissionDAOIf(session).getSubmissionLocked(task, user);
		if (submission != null) {
			return (Result) session.createCriteria(Result.class).add(Restrictions.eq("resultid", submission.getResultid())).uniqueResult();
		} else {
			return null;
		}
	}

	@Override
	public Result createResult(Task task, Participation submitter, String theResult) {
		Session session = getSession();
		Result result = getResult(task, submitter.getUser());
		if (result == null) {
			result = new Result(theResult);
			session.save(result);
		} else {
			result.setResult(theResult);
		}
		return result;
	}

	@Override
	public void saveResult(Result result) {
		Session session = getSession();
		session.saveOrUpdate(result);
	}

	@Override
	public List<Result> getResultsForResult(String result) {
		return getSession().createCriteria(Result.class, "re").add(Restrictions.eq("result", result)).list();
	}
}
