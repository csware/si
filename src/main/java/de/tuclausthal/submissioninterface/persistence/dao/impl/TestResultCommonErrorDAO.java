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

package de.tuclausthal.submissioninterface.persistence.dao.impl;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.TestResultCommonErrorDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommonError;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;

public class TestResultCommonErrorDAO extends AbstractDAO implements TestResultCommonErrorDAOIf {

	public TestResultCommonErrorDAO(Session session) {
		super(session);
	}

	@Override
	public List<CommonError> getCommonError(TestResult testResult) {
		Session session = getSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<CommonError> criteria = builder.createQuery(CommonError.class);
		Root<CommonError> root = criteria.from(CommonError.class);
		criteria.select(root).where(builder.isMember(testResult, root.get("testResults")));
		return session.createQuery(criteria).getResultList();
	}
}
