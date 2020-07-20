/*
 * Copyright 2009-2010, 2013, 2020 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.util;

import java.io.File;

import javax.servlet.ServletContext;

/**
 * Context Adapter
 * @author Sven Strickroth
 */
public class ContextAdapter {
	public ContextAdapter(ServletContext context) {
	}

	/**
	 * Returns the path to the submissions
	 * @return the path
	 */
	public File getDataPath() {
		return Configuration.getInstance().getDataPath();
	}

	/**
	 * Returns the path to the servlets
	 * @return the path
	 */
	public String getServletsPath() {
		return Configuration.getInstance().getServletsPath();
	}

	/**
	 * Returns the path to the servlets
	 * @return the path
	 */
	public String getAdminMail() {
		return Configuration.getInstance().getAdminMail();
	}

	public boolean isMatrikelNoAvailableToTutors() {
		return Configuration.getInstance().isMatrikelNoAvailableToTutors();
	}

	/*
	 * Returns the absolute URI up to the servlets path starting with https://
	 * @return absolute URI up to the servlets path
	 */
	public String getFullServletsURI() {
		return Configuration.getInstance().getFullServletsURI();
	}
}
