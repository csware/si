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
import java.util.ListIterator;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.TaskNumberDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.TaskNumber;

/**
 * Data Access Object implementation for the TaskNumberDAOIf
 * @author Giselle Rodriguez
 */
public class TaskNumberDAO extends AbstractDAO implements TaskNumberDAOIf {
	public TaskNumberDAO(Session session) {
		super(session);
	}

	@Override
	public TaskNumber getTaskNumber(int tasknumberid) {
		return (TaskNumber) getSession().get(TaskNumber.class, tasknumberid);
	}

	@Override
	public List<TaskNumber> getTaskNumber(int taskid, int userid, int submissionid, String number, char type) {
		return getSession().createCriteria(TaskNumber.class, "tn").add(Restrictions.eq("submissionid", submissionid)).add(Restrictions.eq("number", number)).add(Restrictions.eq("type", type)).list();
	}

	@Override
	public TaskNumber createTaskNumber(int taskid, int userid, String number, char type) {
		Session session = getSession();
		TaskNumber tasknumber = new TaskNumber(taskid, userid, number, type);
		session.save(tasknumber);
		return tasknumber;
	}

	@Override
	public List<TaskNumber> createTaskNumbers(int taskid, int userid, int submissionid, List<TaskNumber> taskNumberList) {
		/*if(taskNumberList.size() > 0){
			if(taskNumberList.get(0).getSubmissionid() == 0){
				this.saveTaskNumbers(taskid, userid, taskNumberList.get(0).getSubmissionid());
			}else{
				this.saveTaskNumbers(taskNumberList);
			}
		}*/
		for (TaskNumber tasknumber : taskNumberList) {
			TaskNumber number = new TaskNumber();
			if (submissionid > 0) {
				tasknumber.setSubmissionid(submissionid);
				getSession().save(tasknumber);
			} else {
				number = new TaskNumber(taskid, userid, tasknumber.getNumber(), tasknumber.getType());
				getSession().save(number);
			}
		}
		return getTaskNumbersforTask(taskid, userid);
	}

	@Override
	public void saveTaskNumber(TaskNumber tasknumber) {
		Session session = getSession();
		session.saveOrUpdate(tasknumber);
	}

	@Override
	public void saveTaskNumbers(List<TaskNumber> taskNumberList) {
		ListIterator<TaskNumber> it = taskNumberList.listIterator(taskNumberList.size());
		while (it.hasPrevious()) {
			saveTaskNumber(it.previous());
		}
	}

	@Override
	public void saveTaskNumbers(int taskid, int userid, int submissionid) {
		List<TaskNumber> taskNumberList = this.getTaskNumbersforTask(taskid, userid, 0);
		ListIterator<TaskNumber> it = taskNumberList.listIterator(taskNumberList.size());
		if (submissionid > 0) {
			while (it.hasPrevious()) {
				it.previous().setSubmissionid(submissionid);
				getSession().save(it.previous());
			}
		}
	}

	@Override
	public List<TaskNumber> getTaskNumbersforTask(int submissionid) {
		return getSession().createCriteria(TaskNumber.class, "tn").add(Restrictions.eq("submissionid", submissionid)).list();
	}

	@Override
	public List<TaskNumber> getTaskNumbersforTask(int taskid, int userid) {
		return getSession().createCriteria(TaskNumber.class, "tn").add(Restrictions.eq("taskid", taskid)).add(Restrictions.eq("userid", userid)).list();
	}

	@Override
	public List<TaskNumber> getTaskNumbersforTask(int taskid, int userid, int submissionid) {
		return getSession().createCriteria(TaskNumber.class, "tn").add(Restrictions.eq("taskid", taskid)).add(Restrictions.eq("userid", userid)).add(Restrictions.eq("submissionid", submissionid)).list();
	}

	@Override
	public List<TaskNumber> getTaskNumbersforTask(int taskid, int userid, int submissionid, char type) {
		return getSession().createCriteria(TaskNumber.class, "tn").add(Restrictions.eq("taskid", taskid)).add(Restrictions.eq("userid", userid)).add(Restrictions.eq("submissionid", submissionid)).add(Restrictions.eq("type", type)).list();
	}

	@Override
	public boolean deleteIfNoId() {
		Session session = getSession();
		List<TaskNumber> list = getTaskNumbersforTask(0);
		boolean result = false;
		if (list.size() > 0) {
			ListIterator<TaskNumber> it = list.listIterator(list.size());
			while (it.hasPrevious()) {
				session.delete(it.previous());
			}
			result = true;
		}
		return result;
	}

	@Override
	public boolean deleteTaskNumbers(int taskid, int userid) {
		List<TaskNumber> tasknumbers = this.getTaskNumbersforTask(taskid, userid);
		boolean result = false;
		for (TaskNumber tasknumber : tasknumbers) {
			getSession().delete(tasknumber);
			result = true;
		}
		return result;
	}

	@Override
	public boolean updateSubmissionToNull(int submissionid) {
		boolean result = false;
		if (submissionid != 0) {
			List<TaskNumber> tasknumbers = this.getTaskNumbersforTask(submissionid);
			for (TaskNumber tasknumber : tasknumbers) {
				tasknumber.setSubmissionid(0);
				getSession().save(tasknumber);
			}
		}
		return result;
	}
}
