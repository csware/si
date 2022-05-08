/*
 * Copyright 2009-2010, 2017, 2020-2022 Sven Strickroth <email@cs-ware.de>
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.query.Query;

import de.tuclausthal.submissioninterface.persistence.dao.GroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group_;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation_;

/**
 * Data Access Object implementation for the GroupDAOIf
 * @author Sven Strickroth
 */
public class GroupDAO extends AbstractDAO implements GroupDAOIf {
	public GroupDAO(Session session) {
		super(session);
	}

	@Override
	public Group createGroup(Lecture lecture, String name, boolean allowStudentsToSignup, boolean allowStudentsToQuit, int maxStudents, boolean membersVisibleToStudents) {
		Session session = getSession();
		Group group = new Group();
		group.setName(name);
		group.setLecture(lecture);
		group.setAllowStudentsToSignup(allowStudentsToSignup);
		group.setAllowStudentsToQuit(allowStudentsToQuit);
		group.setMaxStudents(maxStudents);
		group.setMembersVisibleToStudents(membersVisibleToStudents);
		session.save(group);
		return group;
	}

	@Override
	public void deleteGroup(Group group) {
		Session session = getSession();
		session.buildLockRequest(LockOptions.UPGRADE).lock(group);
		for (Participation participation : group.getMembers()) {
			participation.setGroup(null);
		}
		session.delete(group);
	}

	@Override
	public Group getGroup(int groupid) {
		return getSession().get(Group.class, groupid);
	}

	@Override
	public Group getGroupLocked(int groupid) {
		return getSession().get(Group.class, groupid, LockOptions.UPGRADE);
	}

	@Override
	public List<Group> getJoinAbleGroups(Lecture lecture, Group participationGroup) {
		if (participationGroup != null && !participationGroup.isAllowStudentsToQuit()) {
			return Collections.emptyList();
		}

		Session session = getSession();

		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Group> criteria = builder.createQuery(Group.class);
		Root<Group> root = criteria.from(Group.class);
		criteria.select(root);

		Subquery<Long> subQuery = criteria.subquery(Long.class);
		Root<Participation> groupMembersCount = subQuery.from(Participation.class);
		subQuery.select(builder.count(groupMembersCount.get(Participation_.id)));
		subQuery.where(builder.equal(groupMembersCount.get(Participation_.group), root.get(Group_.gid)));

		Predicate where = builder.and(builder.equal(root.get(Group_.lecture), lecture), builder.equal(root.get(Group_.allowStudentsToSignup), true), builder.lt(subQuery, root.get(Group_.maxStudents)));
		if (participationGroup != null) {
			where = builder.and(where, builder.notEqual(root, participationGroup));
		}
		criteria.where(where);
		return session.createQuery(criteria).list();
	}

	@Override
	public Map<Integer, Integer> getGroupSizes(List<Group> groupsToConsider, Group participationGroup) {
		if (groupsToConsider.isEmpty() && participationGroup == null) {
			return Collections.emptyMap();
		}

		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteria = builder.createTupleQuery();
		Root<Participation> root = criteria.from(Participation.class);
		criteria.groupBy(root.get(Participation_.group));
		criteria.select(builder.tuple(root.get(Participation_.group).get(Group_.gid), builder.count(root.get(Participation_.id))));
		if (participationGroup != null) {
			criteria.where(builder.or(root.get(Participation_.group).in(groupsToConsider), builder.equal(root.get(Participation_.group), participationGroup)));
		} else {
			criteria.where(root.get(Participation_.group).in(groupsToConsider));
		}
		Query<Tuple> query = session.createQuery(criteria);

		return query.list().stream().collect(Collectors.toMap(tupel -> tupel.get(0, Integer.class), tupel -> tupel.get(1, Long.class).intValue()));
	}
}
