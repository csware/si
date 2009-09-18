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

package de.tuclausthal.submissioninterface.servlets.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.util.ContextAdapter;

/**
 * Controller-Servlet which performs a redirect to the overview-page
 * @author Sven Strickroth
 *
 */
public class Redirect extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		ContextAdapter contextAdapter = new ContextAdapter(getServletContext());
		response.sendRedirect(request.getContextPath() + "/" + contextAdapter.getServletsPath() + "/Overview");
	}

	/*
		@Override
		public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			// don't want to have any special post-handling
			doGet(request, response);
		}*/
}
