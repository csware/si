package de.tuclausthal.abgabesystem.auth.verifyimpl;

import de.tuclausthal.abgabesystem.auth.LoginData;
import de.tuclausthal.abgabesystem.auth.VerifyIf;
import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public class SessionVerify implements VerifyIf {

	@Override
	public boolean can_change_password() {
		return false;
	}

	@Override
	public boolean can_create_user() {
		return false;
	}

	@Override
	public User check_credentials(LoginData logindata) {
		// TODO Auto-generated method stub
		return null;
	}

}
