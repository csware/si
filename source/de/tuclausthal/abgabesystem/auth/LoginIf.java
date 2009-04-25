package de.tuclausthal.abgabesystem.auth;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface LoginIf {
	public abstract boolean canLogout();

	public abstract boolean canLogin();

	public abstract boolean user_can_create_account();

	public abstract boolean requires_verification();

	public abstract LoginData get_login_info(HttpServletRequest request);

	public abstract void fail_nodata(HttpServletRequest request, HttpServletResponse response) throws IOException;

	public abstract void fail_nodata(String error,HttpServletRequest request, HttpServletResponse response) throws IOException;
	
	public abstract boolean redirectAfterLogin();
}
