package de.tuclausthal.abgabesystem.auth;

public class LoginData {
	private String username;
	private String password;

	/**
	 * @param username
	 * @param password
	 */
	public LoginData(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
}
