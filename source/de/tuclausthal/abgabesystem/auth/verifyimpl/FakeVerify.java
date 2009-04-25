package de.tuclausthal.abgabesystem.auth.verifyimpl;

import de.tuclausthal.abgabesystem.auth.LoginData;
import de.tuclausthal.abgabesystem.auth.VerifyIf;
import de.tuclausthal.abgabesystem.persistence.dao.impl.UserDAO;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public class FakeVerify implements VerifyIf {
	@Override
	public User check_credentials(LoginData logindata) {
		UserDAO userdao = new UserDAO();
		User user = userdao.getUser("sstri");
		if (user == null) {
			user = userdao.createUser("sstri");
			user.setFirstName("Sven");
			user.setLastName("Strickroth");
		}
		return user;
	}
}
