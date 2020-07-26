/*
 * Copyright 2020 Sven Strickroth <email@cs-ware.de>
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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.tuclausthal.submissioninterface.persistence.dao.MCOptionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

/**
 * Data Access Object implementation for the ResultDAOIf
 * @author Sven Strickroth
 */
public class MCOptionDAO extends AbstractDAO implements MCOptionDAOIf {
	public MCOptionDAO(Session session) {
		super(session);
	}

	@Override
	public List<MCOption> getMCOptionsForTask(Task task) {
		Session session = getSession();
		return (List<MCOption>) session.createCriteria(MCOption.class).add(Restrictions.eq("task", task)).addOrder(Order.asc("id")).list();
	}

	@Override
	public MCOption createMCOption(Task task, String option, boolean correct) {
		Session session = getSession();
		session.beginTransaction();
		MCOption mcoption = new MCOption(task, option, correct);
		session.saveOrUpdate(mcoption);
		session.getTransaction().commit();
		return mcoption;
	}

	@Override
	public void deleteMCOption(MCOption mcoption) {
		Session session = getSession();
		session.beginTransaction();
		session.delete(mcoption);
		session.getTransaction().commit();
	}
}
