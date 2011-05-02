/*
 * Copyright 2009 - 2011 Sven Strickroth <email@cs-ware.de>
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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for clearing up the session and redirect to a user overview page
 * @author Sven Strickroth
 */
public class SwitchLogin extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		RequestAdapter.getSessionAdapter(request).setUser(null);
		request.getSession().invalidate();

		ContextAdapter contextAdapter = new ContextAdapter(getServletContext());
		int uid = Util.parseInteger(request.getParameter("uid"), -1);
		if (uid <= 0) {
			request.setAttribute("title", "Invalid Request");
			request.getRequestDispatcher("/" + contextAdapter.getServletsPath() + "/MessageView").forward(request, response);
		} else {
			response.addCookie(new Cookie("privacy", "1"));
			response.sendRedirect(request.getContextPath() + "/" + contextAdapter.getServletsPath() + "/ShowUser?uid=" + uid);
		}
	}
}
