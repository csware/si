/*
 * Copyright 2009-2012, 2017, 2020-2022 Sven Strickroth <email@cs-ware.de>
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

import org.hibernate.LockOptions;
import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points.PointStatus;
import de.tuclausthal.submissioninterface.persistence.datamodel.Points_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task_;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult_;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Data Access Object implementation for the SubmissionDAOIf
 * @author Sven Strickroth
 */
public class SubmissionDAO extends AbstractDAO implements SubmissionDAOIf {
	public SubmissionDAO(Session session) {
		super(session);
	}

	@Override
	public Submission getSubmission(int submissionid) {
		return getSession().get(Submission.class, submissionid);
	}

	@Override
	public Submission getSubmissionLocked(int submissionid) {
		return getSession().get(Submission.class, submissionid, LockOptions.UPGRADE);
	}

	@Override
	public Submission getSubmission(Task task, User user) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Submission> criteria = builder.createQuery(Submission.class);
		Root<Submission> root = criteria.from(Submission.class);
		criteria.select(root);
		criteria.where(builder.and(builder.equal(root.get(Submission_.task), task), builder.equal(root.join(Submission_.submitters).get(Participation_.user), user)));
		return session.createQuery(criteria).uniqueResult();
	}

	@Override
	public Submission createSubmission(Task task, Participation submitter) {
		Session session = getSession();
		// lock participation, because locking of not-existing entries in InnoDB might lock the whole table (submissions AND tasks) causing a strict serialization of ALL requests
		session.buildLockRequest(LockOptions.UPGRADE).lock(submitter);
		Submission submission = getSubmission(task, submitter.getUser());
		if (submission == null) {
			submission = new Submission(task, submitter);
			session.save(submission);
		} else {
			session.refresh(submission, LockModeType.PESSIMISTIC_WRITE);
		}
		return submission;
	}

	@Override
	public List<Submission> getSubmissionsForTaskOrdered(Task task) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Submission> criteria = builder.createQuery(Submission.class);
		Root<Submission> root = criteria.from(Submission.class);
		criteria.select(root);
		@SuppressWarnings("unchecked")
		Join<Submission, Participation> joinSubmitters = (Join<Submission, Participation>) root.fetch(Submission_.submitters, JoinType.LEFT);
		joinSubmitters.fetch(Participation_.user);
		criteria.where(builder.equal(root.get(Submission_.task), task));
		criteria.orderBy(builder.asc(joinSubmitters.get(Participation_.group)), builder.asc(root.get(Submission_.submissionid)));
		return session.createQuery(criteria).list();
	}

	@Override
	public boolean deleteIfNoFiles(Submission submission, File submissionPath) {
		Session session = getSession();
		session.buildLockRequest(LockOptions.UPGRADE).lock(submission);
		boolean result = false;
		Util.recursiveDeleteEmptySubDirectories(submissionPath);
		if (submissionPath.listFiles().length == 0 && submissionPath.delete()) {
			session.delete(submission);
			result = true;
		}
		return result;
	}

	@Override
	public List<Submission> getSubmissionsForTaskOfGroupOrdered(Task task, Group group) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Submission> criteria = builder.createQuery(Submission.class);
		Root<Submission> root = criteria.from(Submission.class);
		criteria.select(root);
		Predicate where = builder.equal(root.get(Submission_.task), task);
		SetJoin<Submission, Participation> submittersJoin = root.join(Submission_.submitters);
		if (group == null) {
			where = builder.and(where, builder.isNull(submittersJoin.get(Participation_.group)));
		} else {
			where = builder.and(where, builder.equal(submittersJoin.get(Participation_.group), group));
		}
		criteria.orderBy(builder.asc(submittersJoin.get(Participation_.group)), builder.asc(root.get(Submission_.submissionid)));
		criteria.where(where);
		return session.createQuery(criteria).list();
	}

	@Override
	public Submission getUngradedSubmission(Task task, int lastSubmissionID, boolean reverse) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Submission> criteria = builder.createQuery(Submission.class);
		Root<Submission> root = criteria.from(Submission.class);
		criteria.select(root);
		Predicate mainPred;
		if (reverse) {
			mainPred = builder.lt(root.get(Submission_.submissionid), lastSubmissionID);
			criteria.orderBy(builder.desc(root.get(Submission_.submissionid)));
		} else {
			mainPred = builder.gt(root.get(Submission_.submissionid), lastSubmissionID);
			criteria.orderBy(builder.asc(root.get(Submission_.submissionid)));
		}
		criteria.where(builder.and(mainPred, builder.equal(root.get(Submission_.task), task), builder.or(builder.isNull(root.get(Submission_.points)), builder.equal(root.get(Submission_.points).get(Points_.pointStatus), PointStatus.NICHT_BEWERTET.ordinal()))));
		return session.createQuery(criteria).setMaxResults(1).uniqueResult();
	}

	@Override
	public Submission getUngradedSubmission(Task task, int lastSubmissionID, Group group, boolean reverse) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Submission> criteria = builder.createQuery(Submission.class);
		Root<Submission> root = criteria.from(Submission.class);
		criteria.select(root);

		Predicate mainPred;
		Order mainOrder;
		if (reverse) {
			mainPred = builder.lt(root.get(Submission_.submissionid), lastSubmissionID);
			mainOrder = builder.desc(root.get(Submission_.submissionid));
		} else {
			mainPred = builder.gt(root.get(Submission_.submissionid), lastSubmissionID);
			mainOrder = builder.asc(root.get(Submission_.submissionid));
		}

		Predicate where = builder.and(mainPred, builder.equal(root.get(Submission_.task), task), builder.or(builder.isNull(root.get(Submission_.points)), builder.equal(root.get(Submission_.points).get(Points_.pointStatus), PointStatus.NICHT_BEWERTET.ordinal())));
		SetJoin<Submission, Participation> submittersJoin = root.join(Submission_.submitters);
		if (group == null) {
			where = builder.and(where, builder.isNull(submittersJoin.get(Participation_.group)));
		} else {
			where = builder.and(where, builder.equal(submittersJoin.get(Participation_.group), group));
		}
		criteria.where(where);
		criteria.orderBy(builder.asc(submittersJoin.get(Participation_.group)), mainOrder);
		return session.createQuery(criteria).setMaxResults(1).uniqueResult();
	}

	@Override
	public List<Submission> getSubmissionsForSearch(Task task, String searchString, boolean publicComment, boolean privateComment, boolean testResults) {
		if (!(publicComment || privateComment || testResults)) {
			return Collections.emptyList();
		}

		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Submission> criteria = builder.createQuery(Submission.class);
		Root<Submission> root = criteria.from(Submission.class);
		criteria.select(root);
		Predicate where = builder.equal(root.get(Submission_.task), task);

		ArrayList<Predicate> ors = new ArrayList<>();
		if (publicComment) {
			ors.add(builder.like(root.get(Submission_.points).get(Points_.publicComment), "%" + searchString + "%"));
		}
		if (privateComment) {
			ors.add(builder.like(root.get(Submission_.points).get(Points_.internalComment), "%" + searchString + "%"));
		}
		if (testResults) {
			Subquery<Submission> subQuery = criteria.subquery(Submission.class);
			Root<TestResult> testResultsRoot = subQuery.from(TestResult.class);
			subQuery.select(testResultsRoot.get(TestResult_.submission));
			subQuery.where(builder.like(testResultsRoot.get(TestResult_.testOutput), "%" + searchString + "%"));
			ors.add(root.in(subQuery));
		}
		criteria.where(builder.and(where, builder.or(ors.toArray(new Predicate[1]))));
		return session.createQuery(criteria).list();
	}

	@Override
	public List<Submission> getAllSubmissions(Participation submitter) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Submission> criteria = builder.createQuery(Submission.class);
		Root<Submission> root = criteria.from(Submission.class);
		criteria.select(root);
		criteria.where(root.join(Submission_.submitters).in(submitter));
		criteria.orderBy(builder.asc(root.get(Submission_.task).get(Task_.taskGroup)), builder.asc(root.get(Submission_.task)));
		return session.createQuery(criteria).list();
	}
}
