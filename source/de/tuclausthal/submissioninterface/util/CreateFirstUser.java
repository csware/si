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

import java.io.IOException;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.UserDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;

/**
 * Tool for creating a first super user
 * @author Sven Strickroth
 */
public class CreateFirstUser {
	/**
	 * @param args loginname, firstname, lastname
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		UserDAOIf userDAOIf = DAOFactory.UserDAOIf();
		User user = userDAOIf.createUser(args[0], args[1], args[2]);
		user.setSuperUser(true);
		userDAOIf.saveUser(user);
		System.out.println("User created.");
	}
}
