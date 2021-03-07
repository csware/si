/*
 * Copyright 2009-2010, 2013, 2020-2021 Sven Strickroth <email@cs-ware.de>
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
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Configuration;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for changeing user properties
 * @author Sven Strickroth
 */
public class AlterUser extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = RequestAdapter.getSession(request);

		UserDAOIf userDAO = DAOFactory.UserDAOIf(session);

		User user = userDAO.getUser(RequestAdapter.getUser(request).getUid());
		if (user == null) {
			request.setAttribute("title", "BenutzerIn nicht gefunden");
			getServletContext().getNamedDispatcher("MessageView").forward(request, response);
			return;
		}

		if (Configuration.getInstance().isMatrikelNumberMustBeEnteredManuallyIfMissing() && !(user instanceof Student)) {
			if (request.getParameter("matrikelno") != null && Util.parseInteger(request.getParameter("matrikelno"), 0) > 0) {
				userDAO.makeUserStudent(RequestAdapter.getUser(request).getUid(), Util.parseInteger(request.getParameter("matrikelno"), 0));
			}
		}
		if (user instanceof Student) {
			Student student = (Student) user;
			if (request.getParameter("studiengang") != null && !"".equals(request.getParameter("studiengang").trim())) {
				student.setStudiengang(request.getParameter("studiengang"));
			}
			Transaction tx = session.beginTransaction();
			userDAO.saveUser(student);
			tx.commit();
		}

		response.sendRedirect(Util.generateRedirectURL("Overview", response));
	}
}
