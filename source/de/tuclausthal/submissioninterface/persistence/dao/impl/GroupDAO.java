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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.GroupDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;

/**
 * Data Access Object implementation for the GroupDAOIf
 * @author Sven Strickroth
 */
public class GroupDAO extends AbstractDAO implements GroupDAOIf {
	public GroupDAO(Session session) {
		super(session);
	}

	@Override
	public Group createGroup(Lecture lecture, String name) {
		Session session = getSession();
		Group group = new Group();
		group.setName(name);
		group.setLecture(lecture);
		session.save(group);
		return group;
	}

	@Override
	public void deleteGroup(Group group) {
		Session session = getSession();
		for (Participation participation : group.getMembers()) {
			participation.setGroup(null);
			session.update(participation);
		}
		session.update(group);
		session.delete(group);
	}

	@Override
	public Group getGroup(int groupid) {
		return (Group) getSession().get(Group.class, groupid);
	}

	@Override
	public void saveGroup(Group group) {
		Session session = getSession();
		session.update(group);
	}

	@Override
	public List<Group> getJoinAbleGroups(Lecture lecture) {
		return (List<Group>) getSession().createCriteria(Group.class).add(Restrictions.eq("lecture", lecture)).add(Restrictions.eq("allowStudentsToSignup", true)).add(Restrictions.sqlRestriction("(select count(*) from participations where groupid=this_.gid) < this_.maxStudents")).list();
	}
}
