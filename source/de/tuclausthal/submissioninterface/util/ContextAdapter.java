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

package de.tuclausthal.submissioninterface.util;

import java.io.File;

import javax.servlet.ServletContext;

/**
 * Context Adapter
 * @author Sven Strickroth
 */
public class ContextAdapter {
	private ServletContext context;

	public ContextAdapter(ServletContext context) {
		this.context = context;
	}

	/**
	 * Returns the path to the submissions
	 * @return the path
	 */
	public File getDataPath() {
		String datapath = context.getInitParameter("datapath");
		if (datapath == null) {
			throw new RuntimeException("datapath not specified");
		}
		File path = new File(datapath);
		if (path.isFile()) {
			throw new RuntimeException("datapath must not be a file");
		}
		if (path.exists() == false) {
			if (!path.mkdirs()) {
				throw new RuntimeException("could not create datapath");
			}
		}
		return new File(datapath);
	}

	/**
	 * Returns the path to the servlets
	 * @return the path
	 */
	public String getServletsPath() {
		String servletspath = context.getInitParameter("servletspath");
		if (servletspath == null) {
			throw new RuntimeException("servletspath not specified");
		}
		return servletspath;
	}
}
