package de.tuclausthal.abgabesystem.auth;

import de.tuclausthal.abgabesystem.persistence.datamodel.User;

public interface VerifyIf {
	public User check_credentials(LoginData logindata);

	public boolean can_change_password();

	public boolean can_create_user();
}
