/*
 * Copyright 2020, 2022-2023 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.MCOptionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption_;
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
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<MCOption> criteria = builder.createQuery(MCOption.class);
		Root<MCOption> root = criteria.from(MCOption.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get(MCOption_.task), task));
		criteria.orderBy(builder.asc(root.get(MCOption_.id)));
		return session.createQuery(criteria).list();
	}

	@Override
	public MCOption createMCOption(Task task, String option, boolean correct) {
		Session session = getSession();
		MCOption mcoption = new MCOption(task, option, correct);
		session.persist(mcoption);
		return mcoption;
	}

	@Override
	public void deleteMCOption(MCOption mcoption) {
		Session session = getSession();
		session.remove(mcoption);
	}
}
