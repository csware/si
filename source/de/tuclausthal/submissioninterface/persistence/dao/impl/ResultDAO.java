/*
 * Copyright 2011 Sven Strickroth <email@cs-ware.de>
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

import java.util.LinkedList;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.ResultDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Result;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;

/**
 * Data Access Object implementation for the ResultDAOIf
 * @author Sven Strickroth
 */
public class ResultDAO extends AbstractDAO implements ResultDAOIf {
	public ResultDAO(Session session) {
		super(session);
	}

	@Override
	public List<String> getResultsForSubmission(Submission submission) {
		Session session = getSession();
		List<String> results = new LinkedList<String>();
		for (Result result : (List<Result>) session.createCriteria(Result.class).add(Restrictions.eq("submission", submission)).addOrder(Order.asc("resultid")).list()) {
			results.add(result.getResult());
		}
		return results;
	}

	@Override
	public void createResults(Submission submission, List<String> results) {
		session.lock(submission, LockMode.UPGRADE);
		for (Result result : (List<Result>) session.createCriteria(Result.class).add(Restrictions.eq("submission", submission)).setLockMode(LockMode.UPGRADE).list()) {
			session.delete(result);
		}
		for (String stringResult : results) {
			session.save(new Result(submission, stringResult));
		}
	}
}
