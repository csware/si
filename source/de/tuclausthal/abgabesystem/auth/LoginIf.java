package de.tuclausthal.abgabesystem.auth;

public interface LoginIf {
	public abstract boolean canLogout();

	public abstract boolean canLogin();

	public abstract boolean user_can_create_account();

	public abstract boolean requires_verification();

	public abstract LoginData get_login_info();

	public abstract void fail_nodata();

	public abstract void fail_nodata(String error);
	
	public abstract boolean redirectAfterLogin();
}
