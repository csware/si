/*
 * Copyright 2009-2010, 2013, 2020-2022, 2024 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.servlets.GATEController;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.view.MessageView;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for changeing user properties
 * @author Sven Strickroth
 */
@GATEController
public class AlterUser extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		UserDAOIf userDAO = DAOFactory.UserDAOIf(session);

		User user = userDAO.getUser(RequestAdapter.getUser(request).getUid());
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			request.setAttribute("title", "BenutzerIn nicht gefunden");
			getServletContext().getNamedDispatcher(MessageView.class.getSimpleName()).forward(request, response);
			return;
		}

		if (Configuration.getInstance().isMatrikelNumberMustBeEnteredManuallyIfMissing() && !(user instanceof Student)) {
			if (request.getParameter("matrikelno") != null && Util.parseInteger(request.getParameter("matrikelno"), 0) > 0) {
				Transaction tx = session.beginTransaction();
				userDAO.makeUserStudent(RequestAdapter.getUser(request).getUid(), Util.parseInteger(request.getParameter("matrikelno"), 0));
				tx.commit();
			}
		}
		if (user instanceof Student student) {
			if (request.getParameter("studiengang") != null && !"".equals(request.getParameter("studiengang").trim())) {
				Transaction tx = session.beginTransaction();
				student.setStudiengang(request.getParameter("studiengang"));
				tx.commit();
			}
		}

		response.sendRedirect(Util.generateRedirectURL(Overview.class.getSimpleName(), response));
	}
}
