package de.tuclausthal.abgabesystem.auth.verifyimpl;

import de.tuclausthal.abgabesystem.auth.LoginData;
import de.tuclausthal.abgabesystem.auth.VerifyIf;
import de.tuclausthal.abgabesystem.persistence.dao.DAOFactory;
import de.tuclausthal.abgabesystem.persistence.dao.UserDAOIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public class FakeVerify implements VerifyIf {
	@Override
	public User check_credentials(LoginData logindata) {
		UserDAOIf userdao = DAOFactory.UserDAOIf();
		User user = userdao.getUser(logindata.getUsername());
		return user;
	}
}
